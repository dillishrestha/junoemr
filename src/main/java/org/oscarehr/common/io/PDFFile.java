/**
 * Copyright (c) 2012-2018. CloudPractice Inc. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for
 * CloudPractice Inc.
 * Victoria, British Columbia
 * Canada
 */

package org.oscarehr.common.io;

import org.apache.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.oscarehr.util.MiscUtils;
import oscar.OscarProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PDFFile extends GenericFile
{
	private static final Logger logger = MiscUtils.getLogger();
	private static final Set<String> allowedErrors = new HashSet<>();
	private static final Pattern[] allowedWarningsGS = new Pattern[1];

	static
	{
		allowedWarningsGS[0] = Pattern.compile(".*Missing glyph .* in the font .*", Pattern.CASE_INSENSITIVE);
	}

	private OscarProperties oscarProperties = OscarProperties.getInstance();
	private long maxMemoryUsage = oscarProperties.getPDFMaxMemUsage();

	public PDFFile(File file) throws IOException
	{
		super(file);
	}

	private boolean isAllowedWarning(String line)
	{
		for (Pattern pattern : allowedWarningsGS)
		{
			if (pattern.matcher(line).matches())
				return true;
		}
		return false;
	}

	@Override
	public boolean validate() throws IOException, InterruptedException
	{
		this.isValid = pdfinfoValidation(javaFile);
		if(!this.isValid)
		{
			logger.error("Pdf Encoding Error: " + getReasonInvalid());
		}
		this.hasBeenValidated = true;
		return this.isValid;
	}
	@Override
	public void process() throws IOException, InterruptedException
	{
		javaFile = ghostscriptReEncode();
	}
	/**
	 * Counts the number of pages in a local pdf file.
	 *
	 * @return the number of pages in the file
	 */
	@Override
	public int getPageCount() throws IOException
	{
		int numOfPage = 0;

		try
		{
			PDDocument document = PDDocument.load(javaFile, MemoryUsageSetting.setupMainMemoryOnly(maxMemoryUsage));
			numOfPage = document.getNumberOfPages();
			document.close();
		}
		catch(InvalidPasswordException e)
		{
			logger.warn("Encrypted PDF. Can't get page count");
		}

		return numOfPage;
	}

	private boolean pdfinfoValidation(File file) throws IOException,InterruptedException
	{
		logger.info("BEGIN PDF VALIDATION");
		boolean isValid = true;
		boolean isEncrypted = false;

		String pdfInfo = props.getProperty("document.pdfinfo_path", "/usr/bin/pdfinfo");

		String[] command = {pdfInfo, file.getPath()};
		Process process = Runtime.getRuntime().exec(command);

		BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;

		while((line = in.readLine()) != null)
		{
			logger.warn("validator error line: " + line);
			// if error is allowed and flag not already set to fail
			isValid = (isValid && allowedErrors.contains(line.toLowerCase()));
			isEncrypted = (line.toLowerCase().equals("command line error: incorrect password"));
			this.reasonInvalid = (this.reasonInvalid == null)? line : this.reasonInvalid + ", " + line;
		}
		process.waitFor();
		in.close();

		int exitValue = process.exitValue();
		if (exitValue != 0)
		{
			if (isEncrypted)
			{
				isValid = true;
			} else
			{
				isValid = false;
			}
		}

		logger.info("Passed PDF Validation: " + isValid);
		return isValid;
	}

	private File ghostscriptReEncode() throws IOException,InterruptedException
	{
		logger.info("BEGIN PDF RE-ENCODING");

		String gs = props.getProperty("document.ghostscript_path", "/usr/bin/gs");

		File currentDir = javaFile.getParentFile();
		this.moveToOriginal();

		File newPdf = new File(currentDir, javaFile.getName());

		/*
		This has to de done through the command line and not the java library,
		due to a bug in how the java libraries read pdf files. (libraries lowagie, pdfbox, and itext all have this error)
		The error has caused an infinite loop during the initial parse and crashed tomcat.
		 */
		String[] command = {gs,
				"-sDEVICE=pdfwrite",
				"-dCompatibilityLevel=1.4",
				"-dPDFSETTINGS=/printer",
				"-dNOPAUSE",
				"-dQUIET",      // Suppresses routine information comments on standard output
				"-q",           // Quiet startup: suppress normal startup messages, and also do the equivalent of dQUIET
//				"-sstdout=%stderr", // Redirect PostScript %stdout to a file or stderr, to avoid it being mixed with device stdout.
				"-dBATCH",
				"-sOutputFile="+ newPdf.getPath(),
				javaFile.getPath()};
		
		logger.info(Arrays.toString(command));
		Process process = Runtime.getRuntime().exec(command);

		BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		String line;
		String reasonInvalid = null;
		String warnings = null;
		while((line = stdout.readLine()) != null)
		{
			logger.warn("gs stdout: " + line);
			warnings = (warnings == null)? line : warnings + ", " + line;
		}

		while((line = stderr.readLine()) != null)
		{
			logger.warn("gs stderr: " + line);

			if (isAllowedWarning(line))
				continue;

			reasonInvalid = (reasonInvalid == null)? line : reasonInvalid + ", " + line;
		}
		process.waitFor();
		stdout.close();
		stderr.close();

		int exitValue = process.exitValue();
		if(exitValue == 0 && reasonInvalid == null && warnings != null)
		{
			// append the original file location to the log if there is unexpected output
			logger.warn("File conversion allowed with unexpected output!");
			logger.warn("Original file:  " + javaFile.getPath());
			logger.warn("Converted file: " + newPdf.getPath());
			logger.warn("---------------------------------------------");
		}

		if(exitValue != 0 || reasonInvalid != null)
		{
			try
			{
				PDDocument document = PDDocument.load(javaFile, MemoryUsageSetting.setupMainMemoryOnly(maxMemoryUsage));
				document.close();
			} catch(InvalidPasswordException e)
			{
				logger.warn("Encrypted PDF. Cannot re-encode");
				this.moveFile(currentDir);
				newPdf = javaFile;
				return newPdf;
			}

			this.moveToCorrupt();
			throw new RuntimeException("Ghost-script Error: " + reasonInvalid + ". ExitValue: " + exitValue);
		}

		try
		{
			PDDocument document = PDDocument.load(newPdf, MemoryUsageSetting.setupMainMemoryOnly(maxMemoryUsage));
			document.close();
		} catch(Exception e)
		{
			throw new RuntimeException("Failed to load re-encoded PDF: ", e);
		}

		logger.info("END PDF RE-ENCODING");
		return newPdf;
	}
}
