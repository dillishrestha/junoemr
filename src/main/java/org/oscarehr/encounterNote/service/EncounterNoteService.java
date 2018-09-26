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
package org.oscarehr.encounterNote.service;

import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.allergy.model.Allergy;
import org.oscarehr.common.dao.SecRoleDao;
import org.oscarehr.common.model.SecRole;
import org.oscarehr.demographic.dao.DemographicDao;
import org.oscarehr.demographic.model.Demographic;
import org.oscarehr.encounterNote.dao.CaseManagementIssueDao;
import org.oscarehr.encounterNote.dao.CaseManagementIssueNoteDao;
import org.oscarehr.encounterNote.dao.CaseManagementNoteDao;
import org.oscarehr.encounterNote.dao.CaseManagementNoteLinkDao;
import org.oscarehr.encounterNote.dao.CaseManagementTmpSaveDao;
import org.oscarehr.encounterNote.dao.IssueDao;
import org.oscarehr.encounterNote.model.CaseManagementIssue;
import org.oscarehr.encounterNote.model.CaseManagementIssueNote;
import org.oscarehr.encounterNote.model.CaseManagementIssueNotePK;
import org.oscarehr.encounterNote.model.CaseManagementNote;
import org.oscarehr.encounterNote.model.CaseManagementNoteLink;
import org.oscarehr.encounterNote.model.Issue;
import org.oscarehr.provider.dao.ProviderDataDao;
import org.oscarehr.provider.model.ProviderData;
import org.oscarehr.rx.model.Drug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class EncounterNoteService
{
	@Autowired
	CaseManagementNoteDao caseManagementNoteDao;

	@Autowired
	CaseManagementIssueNoteDao caseManagementIssueNoteDao;

	@Autowired
	CaseManagementNoteLinkDao caseManagementNoteLinkDao;

	@Autowired
	CaseManagementIssueDao caseManagementIssueDao;

	@Autowired
	CaseManagementTmpSaveDao caseManagementTmpSaveDao;

	@Autowired
	IssueDao issueDao;

	@Autowired
	ProgramManager programManager;

	@Autowired
	SecRoleDao secRoleDao;

	@Autowired
	ProviderDataDao providerDataDao;

	@Autowired
	DemographicDao demographicDao;

	public CaseManagementNote saveChartNote(CaseManagementNote note)
	{
		return saveChartNote(note, null);
	}
	public CaseManagementNote saveChartNote(CaseManagementNote note, List<Issue> issueList)
	{
		note.setIncludeIssueInNote(true);
		note = saveNote(note);

		if(issueList != null && !issueList.isEmpty())
		{
			//TODO merge/save issues as CaseManagementIssue models
		}

		return note;
	}

	public CaseManagementNote saveAllergyNote(CaseManagementNote note, Allergy allergy)
	{
		note.setIncludeIssueInNote(true);
		note.setSigned(true);
		note.setArchived(false);

		note = saveNote(note);

		CaseManagementNoteLink link = new CaseManagementNoteLink();
		link.setNote(note);
		link.setAllergy(allergy.getAllergyId());
		caseManagementNoteLinkDao.persist(link);

		return note;
	}
	public CaseManagementNote saveDrugNote(CaseManagementNote note, Drug drug)
	{
		note.setIncludeIssueInNote(true);
		note.setSigned(true);
		note.setArchived(false);

		note = saveNote(note);

		CaseManagementNoteLink link = new CaseManagementNoteLink();
		link.setNote(note);
		link.setDrug(drug.getId());
		caseManagementNoteLinkDao.persist(link);

		return note;
	}


	public CaseManagementNote saveMedicalHistoryNote(CaseManagementNote note)
	{
		return saveHistoryNote(note, Issue.SUMMARY_CODE_MEDICAL_HISTORY);
	}

	public CaseManagementNote saveSocialHistoryNote(CaseManagementNote note)
	{
		return saveHistoryNote(note, Issue.SUMMARY_CODE_SOCIAL_HISTORY);
	}

	public CaseManagementNote saveFamilyHistoryNote(CaseManagementNote note)
	{
		return saveHistoryNote(note, Issue.SUMMARY_CODE_FAMILY_HISTORY);
	}

	private CaseManagementNote saveHistoryNote(CaseManagementNote note, String summaryCode)
	{
		CaseManagementIssue caseManagementIssue = caseManagementIssueDao.findByIssueCode(
				note.getDemographic().getDemographicId(), summaryCode);

		// save the base note
		note.setSigned(true);
		note.setIncludeIssueInNote(true);
		note.setPosition(1);
		note = saveNote(note);

		// create the demographic specific issue if it does not exist
		if(caseManagementIssue == null)
		{
			// grab the master issue for reference/link
			Issue issue = issueDao.findByCode(summaryCode);

			caseManagementIssue = new CaseManagementIssue();
			caseManagementIssue.setAcute(false);
			caseManagementIssue.setCertain(false);
			caseManagementIssue.setMajor(false);
			caseManagementIssue.setProgramId(programManager.getDefaultProgramId());
			caseManagementIssue.setResolved(false);
			caseManagementIssue.setIssue(issue);
			caseManagementIssue.setType(issue.getRole());
			caseManagementIssue.setDemographic(note.getDemographic());
			caseManagementIssue.setUpdateDate(note.getUpdateDate());

			caseManagementIssueDao.persist(caseManagementIssue);
		}

		// link the note and the issue
		CaseManagementIssueNotePK caseManagementIssueNotePK = new CaseManagementIssueNotePK();
		caseManagementIssueNotePK.setCaseManagementIssue(caseManagementIssue);
		caseManagementIssueNotePK.setCaseManagementNote(note);

		CaseManagementIssueNote caseManagementIssueNote = new CaseManagementIssueNote();
		caseManagementIssueNote.setId(caseManagementIssueNotePK);

		caseManagementIssueNoteDao.persist(caseManagementIssueNote);
		return note;
	}

	private CaseManagementNote saveNote(CaseManagementNote note)
	{
		if(note.getUpdateDate() == null)
		{
			note.setUpdateDate(new Date());
		}
		if(note.getObservationDate() == null)
		{
			note.setObservationDate(new Date());
		}
		if(note.getBillingCode() == null)
		{
			note.setBillingCode("");
		}
		if(note.getEncounterType() == null)
		{
			note.setEncounterType("");
		}
		if(note.getProgramNo() == null)
		{
			note.setProgramNo(String.valueOf(programManager.getDefaultProgramId()));
		}
		if(note.getHistory() == null)
		{
			note.setHistory(note.getNote());
		}
		if(note.getReporterCaisiRole() == null)
		{
			note.setReporterCaisiRole(getCaisiRole());
		}
		if(note.getReporterProgramTeam() == null)
		{
			note.setReporterProgramTeam("0");
		}
		if(note.getPosition() == null)
		{
			note.setPosition(0);
		}

		if(note.getUuid() == null)
		{
			note.setUuid(UUID.randomUUID().toString());
		}

		caseManagementNoteDao.persist(note);

		return note;
	}

	/**
	 * one day we will get rid of this
	 */
	private String getCaisiRole()
	{
		SecRole secRole = secRoleDao.findByName("doctor");
		if(secRole != null)
		{
			return String.valueOf(secRole.getId());
		}
		return "0";
	}


	private CaseManagementNote getOpenNoteCopy(ProviderData provider, Demographic demographic)
	{
		CaseManagementNote openNoteCopy = new CaseManagementNote();
		openNoteCopy.setProvider(provider);
		openNoteCopy.setDemographic(demographic);

		CaseManagementNote unsignedNote = caseManagementNoteDao.getNewestUnsignedNote(provider.getId(), demographic.getDemographicId());
		if(unsignedNote != null) //copy of existing note
		{
			openNoteCopy.setNote(unsignedNote.getNote());
			openNoteCopy.setHistory(unsignedNote.getHistory());
			openNoteCopy.setUuid(unsignedNote.getUuid());
			openNoteCopy.setObservationDate(unsignedNote.getObservationDate());
			openNoteCopy.setAppointment(unsignedNote.getAppointment());
			openNoteCopy.setArchived(unsignedNote.getArchived());
			openNoteCopy.setIncludeIssueInNote(unsignedNote.getIncludeIssueInNote());
		}
		else // new note
		{
			SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			Date date = new Date();
			String formattedDate = "[" + df.format(date) + " .: ]";

			openNoteCopy.setNote(formattedDate + "\n");
			openNoteCopy.setHistory(formattedDate + "\n");
		}
		return openNoteCopy;
	}

	/**
	 * Append the given text to the open chart note.
	 * Use the following logic:
	 *      if a tempNote exists, get the tempNote
	 *      else if an unsigned note exists, get a copy of the unsigned note (will have matching uuid so a revision will be saved)
	 *      else get a new empty note
	 * @return the note object
	 */
	public CaseManagementNote appendToOpenNoteAndPersist(ProviderData provider, Demographic demographic, String textToAppend)
	{
		CaseManagementNote openNoteCopy = getOpenNoteCopy(provider, demographic);
		openNoteCopy.setNote(openNoteCopy.getNote() + textToAppend);
		openNoteCopy.setHistory(openNoteCopy.getHistory() + "\n" + openNoteCopy.getNote());
		saveChartNote(openNoteCopy);
		return openNoteCopy;
	}

	/**
	 * Append the given text to the open chart note.
	 * Use the following logic:
	 *      if a tempNote exists, get the tempNote
	 *      else if an unsigned note exists, get a copy of the unsigned note (will have matching uuid so a revision will be saved)
	 *      else get a new empty note
	 * @return the note object
	 */
	public CaseManagementNote appendToOpenNoteAndPersist(String providerNo, Integer demographicId, String textToAppend)
	{
		Demographic demographic = demographicDao.find(demographicId);
		ProviderData provider = providerDataDao.find(providerNo);
		return appendToOpenNoteAndPersist(provider, demographic, textToAppend);
	}
}
