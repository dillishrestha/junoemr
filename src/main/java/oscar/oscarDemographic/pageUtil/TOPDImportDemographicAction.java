/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package oscar.oscarDemographic.pageUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.oscarehr.common.io.FileFactory;
import org.oscarehr.common.io.GenericFile;
import org.oscarehr.demographicImport.service.DemographicImportService;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import oscar.OscarProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TOPDImportDemographicAction extends Action
{
	private static final Logger logger = MiscUtils.getLogger();
	private static final OscarProperties oscarProperties = OscarProperties.getInstance();

	private DemographicImportService demographicImportService = SpringUtils.getBean(DemographicImportService.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	{

		logger.info("BEGIN DEMOGRAPHIC IMPORT PROCESS ...");

		try
		{
			ImportDemographicDataForm frm = (ImportDemographicDataForm) form;
			FormFile imp = frm.getImportFile();
			GenericFile tempFile = FileFactory.createTempFile(imp.getInputStream());

			demographicImportService.importDemographicData(tempFile);
		}
		catch(Exception e)
		{
			logger.error("Import Error", e);
		}
		logger.info("IMPORT PROCESS COMPLETE");
		return mapping.findForward("success");
	}
}