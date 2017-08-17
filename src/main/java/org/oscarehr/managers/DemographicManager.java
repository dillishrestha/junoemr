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

package org.oscarehr.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.oscarehr.common.Gender;
import org.oscarehr.common.dao.AdmissionDao;
import org.oscarehr.common.dao.DemographicArchiveDao;
import org.oscarehr.common.dao.DemographicContactDao;
import org.oscarehr.common.dao.DemographicCustArchiveDao;
import org.oscarehr.common.dao.DemographicCustDao;
import org.oscarehr.common.dao.DemographicDao;
import org.oscarehr.common.dao.DemographicExtArchiveDao;
import org.oscarehr.common.dao.DemographicExtDao;
import org.oscarehr.common.dao.DemographicMergedDao;
import org.oscarehr.common.dao.PHRVerificationDao;
import org.oscarehr.common.exception.PatientDirectiveException;
import org.oscarehr.common.model.Admission;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Demographic.PatientStatus;
import org.oscarehr.common.model.DemographicContact;
import org.oscarehr.common.model.DemographicCust;
import org.oscarehr.common.model.DemographicExt;
import org.oscarehr.common.model.DemographicMerged;
import org.oscarehr.common.model.PHRVerification;
import org.oscarehr.common.model.Provider;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.rest.to.model.DemographicSearchRequest;
import org.oscarehr.ws.rest.to.model.DemographicSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oscar.util.StringUtils;

/**
 * Will provide access to demographic data, as well as closely related data such as 
 * extensions (DemographicExt), merge data, archive data, etc.
 * 
 * Future Use: Add privacy, security, and consent profiles
 * 
 *
 */
@Service
public class DemographicManager {
	public static final String PHR_VERIFICATION_LEVEL_3 = "+3";
	public static final String PHR_VERIFICATION_LEVEL_2 = "+2";
	public static final String PHR_VERIFICATION_LEVEL_1 = "+1";

	private static Logger logger = MiscUtils.getLogger();

	@Autowired
	private DemographicDao demographicDao;
	@Autowired
	private DemographicExtDao demographicExtDao;
	@Autowired
	private DemographicCustDao demographicCustDao;
	@Autowired
	private DemographicContactDao demographicContactDao;

	@Autowired
	private DemographicArchiveDao demographicArchiveDao;
	@Autowired
	private DemographicExtArchiveDao demographicExtArchiveDao;
	@Autowired
	private DemographicCustArchiveDao demographicCustArchiveDao;

	@Autowired
	private DemographicMergedDao demographicMergedDao;

	@Autowired
	private PHRVerificationDao phrVerificationDao;

	@Autowired
	private AdmissionDao admissionDao;
	
	@Autowired
	private SecurityInfoManager securityInfoManager;
	

	public Demographic getDemographic(LoggedInInfo loggedInInfo, Integer demographicId) throws PatientDirectiveException {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ, (demographicId!=null)?demographicId:null );
		
		return demographicDao.getDemographicById(demographicId);
	}
		
	public Demographic getDemographic(LoggedInInfo loggedInInfo, String demographicNo) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		Integer demographicId = null;
		try {
			demographicId = Integer.parseInt(demographicNo);
		} catch(NumberFormatException e) {
			return null;
		}
		return getDemographic(loggedInInfo,demographicId);
		
	}
	
	
	public Demographic getDemographicWithExt(LoggedInInfo loggedInInfo, Integer demographicId) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		Demographic result = getDemographic(loggedInInfo, demographicId);
		if (result!=null) {
			List<DemographicExt> demoExts = getDemographicExts(loggedInInfo,demographicId);
			if (demoExts!=null && !demoExts.isEmpty()) {
				DemographicExt[] demoExtArray = demoExts.toArray(new DemographicExt[demoExts.size()]);
				result.setExtras(demoExtArray);
			}
		}
		return result;
	}

	public String getDemographicFormattedName(LoggedInInfo loggedInInfo, Integer demographicId) {
		Demographic result = getDemographic(loggedInInfo, demographicId);
		String name = null;
		if (result != null) {
			name = result.getLastName() + ", " + result.getFirstName();
		}
		return (name);
	}

	public Demographic getDemographicByMyOscarUserName(LoggedInInfo loggedInInfo, String myOscarUserName) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		Demographic result = demographicDao.getDemographicByMyOscarUserName(myOscarUserName);

		return (result);
	}

	public List<Demographic> searchDemographicByName(LoggedInInfo loggedInInfo, String searchString, int startIndex, int itemsToReturn) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		
		List<Demographic> results = demographicDao.searchDemographicByNameString(searchString, startIndex, itemsToReturn);

		if (logger.isDebugEnabled()) {
			logger.debug("searchDemographicByName, searchString=" + searchString + ", result.size=" + results.size());
		}

		return (results);
	}

	public List<DemographicExt> getDemographicExts(LoggedInInfo loggedInInfo, Integer id) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		List<DemographicExt> result = null;

		result = demographicExtDao.getDemographicExtByDemographicNo(id);

		return result;
	}

	public DemographicExt getDemographicExt(LoggedInInfo loggedInInfo, Integer demographicNo, String key) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		DemographicExt result = null;
		result = demographicExtDao.getDemographicExt(demographicNo, key);

		return result;
	}

	public DemographicCust getDemographicCust(LoggedInInfo loggedInInfo, Integer id) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		DemographicCust result = demographicCustDao.find(id);

		return result;
	}

	public void createUpdateDemographicCust(LoggedInInfo loggedInInfo, DemographicCust demoCust) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		if (demoCust != null) {
			//Archive previous demoCust
			DemographicCust prevCust = demographicCustDao.find(demoCust.getId());
			if (prevCust != null) {
				if (!(StringUtils.nullSafeEquals(prevCust.getAlert(), demoCust.getAlert()) && StringUtils.nullSafeEquals(prevCust.getMidwife(), demoCust.getMidwife()) && StringUtils.nullSafeEquals(prevCust.getNurse(), demoCust.getNurse()) && StringUtils.nullSafeEquals(prevCust.getResident(), demoCust.getResident()) && StringUtils.nullSafeEquals(prevCust.getNotes(), demoCust.getNotes()))) {
					demographicCustArchiveDao.archiveDemographicCust(prevCust);
				}
			}

			demographicCustDao.merge(demoCust);
		}
	}

	public List<DemographicContact> getDemographicContacts(LoggedInInfo loggedInInfo, Integer id) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		List<DemographicContact> result = null;
		result = demographicContactDao.findActiveByDemographicNo(id);

		return result;
	}

	public List<Demographic> getDemographicsByProvider(LoggedInInfo loggedInInfo, Provider provider) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		List<Demographic> result = demographicDao.getDemographicByProvider(provider.getProviderNo(), true);

		return result;
	}

	public void createDemographic(LoggedInInfo loggedInInfo, Demographic demographic, Integer admissionProgramId) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		try {
			demographic.getBirthDay();
		} catch (Exception e) {
			throw new IllegalArgumentException("Birth date was specified for " + demographic.getFullName() + ": " + demographic.getBirthDayAsString());
		}

		demographic.setPatientStatus(PatientStatus.AC.name());
		demographic.setFamilyDoctor("<rdohip></rdohip><rd></rd>");
		demographic.setLastUpdateUser(loggedInInfo.getLoggedInProviderNo());
		demographicDao.save(demographic);

		Admission admission = new Admission();
		admission.setClientId(demographic.getDemographicNo());
		admission.setProgramId(admissionProgramId);
		admission.setProviderNo(loggedInInfo.getLoggedInProviderNo());
		admission.setAdmissionDate(new Date());
		admission.setAdmissionStatus(Admission.STATUS_CURRENT);
		admission.setAdmissionNotes("");

		admissionDao.saveAdmission(admission);

		if (demographic.getExtras() != null) {
			for (DemographicExt ext : demographic.getExtras()) {
				createExtension(loggedInInfo, ext);
			}
		}
	}

	public void updateDemographic(LoggedInInfo loggedInInfo, Demographic demographic) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.UPDATE);
		try {
			demographic.getBirthDay();
		} catch (Exception e) {
			throw new IllegalArgumentException("Birth date was specified for " + demographic.getFullName() + ": " + demographic.getBirthDayAsString());
		}

		//Archive previous demo
		Demographic prevDemo = demographicDao.getDemographicById(demographic.getDemographicNo());
		demographicArchiveDao.archiveRecord(prevDemo);

		//retain merge info
		demographic.setSubRecord(prevDemo.getSubRecord());
		
		//save current demo
		demographic.setLastUpdateUser(loggedInInfo.getLoggedInProviderNo());
		demographicDao.save(demographic);

		if (demographic.getExtras() != null) {
			for (DemographicExt ext : demographic.getExtras()) {
				updateExtension(loggedInInfo, ext);
			}
		}
	}
	
	public void addDemographic(LoggedInInfo loggedInInfo, Demographic demographic) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		try {
			demographic.getBirthDay();
		} catch (Exception e) {
			throw new IllegalArgumentException("Birth date was specified for " + demographic.getFullName() + ": " + demographic.getBirthDayAsString());
		}

		//save current demo
		demographic.setLastUpdateUser(loggedInInfo.getLoggedInProviderNo());
		demographicDao.save(demographic);

		if (demographic.getExtras() != null) {
			for (DemographicExt ext : demographic.getExtras()) {
				updateExtension(loggedInInfo, ext);
			}
		}
	}
	

	public void createExtension(LoggedInInfo loggedInInfo, DemographicExt ext) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		demographicExtDao.saveEntity(ext);
	}

	public void updateExtension(LoggedInInfo loggedInInfo, DemographicExt ext) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.UPDATE);
		archiveExtension(ext);
		demographicExtDao.saveEntity(ext);
	}

	public void archiveExtension(DemographicExt ext) {
		//TODO: this needs a loggedInInfo
		if (ext != null && ext.getId() != null) {
			DemographicExt prevExt = demographicExtDao.find(ext.getId());
			if (!(ext.getKey().equals(prevExt.getKey()) && ext.getValue().equals(prevExt.getValue()))) {
				demographicExtArchiveDao.archiveDemographicExt(prevExt);
			}
		}
	}

	public void createUpdateDemographicContact(LoggedInInfo loggedInInfo, DemographicContact demoContact) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		
		demographicContactDao.merge(demoContact);
	}

	public void deleteDemographic(LoggedInInfo loggedInInfo, Demographic demographic) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		
		demographicArchiveDao.archiveRecord(demographic);
		demographic.setPatientStatus(Demographic.PatientStatus.DE.name());
		demographic.setLastUpdateUser(loggedInInfo.getLoggedInProviderNo());
		demographicDao.save(demographic);

		for (DemographicExt ext : getDemographicExts(loggedInInfo, demographic.getDemographicNo())) {
			deleteExtension(loggedInInfo, ext);
		}
	}

	public void deleteExtension(LoggedInInfo loggedInInfo, DemographicExt ext) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
		archiveExtension(ext);
		demographicExtDao.removeDemographicExt(ext.getId());
	}

	public void mergeDemographics(LoggedInInfo loggedInInfo, Integer parentId, List<Integer> children) {
		for (Integer child : children) {
			DemographicMerged dm = new DemographicMerged();
			dm.setDemographicNo(child);
			dm.setMergedTo(parentId);
			demographicMergedDao.persist(dm);
		}

	}

	public void unmergeDemographics(LoggedInInfo loggedInInfo, Integer parentId, List<Integer> children) {
		for (Integer childId : children) {
			List<DemographicMerged> dms = demographicMergedDao.findByParentAndChildIds(parentId, childId);
			if (dms.isEmpty()) {
				throw new IllegalArgumentException("Unable to find merge record for parent " + parentId + " and child " + childId);
			}
			for (DemographicMerged dm : demographicMergedDao.findByParentAndChildIds(parentId, childId)) {
				dm.setDeleted(1);
				demographicMergedDao.merge(dm);
			}
		}
	}

	public Long getActiveDemographicCount(LoggedInInfo loggedInInfo) {
		Long count = demographicDao.getActiveDemographicCount();

		return count;
	}

	public List<Demographic> getActiveDemographics(LoggedInInfo loggedInInfo, int offset, int limit) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		List<Demographic> result = demographicDao.getActiveDemographics(offset, limit);

		return result;
	}

	/**
	 * Gets all merged demographic for the specified parent record ID 
	 * 
	 * @param parentId
	 * 		ID of the parent demographic record 
	 * @return
	 * 		Returns all merged demographic records for the specified parent id.
	 */
	public List<DemographicMerged> getMergedDemographics(LoggedInInfo loggedInInfo, Integer parentId) {
		List<DemographicMerged> result = demographicMergedDao.findCurrentByMergedTo(parentId);

		return result;
	}

	public PHRVerification getLatestPhrVerificationByDemographicId(LoggedInInfo loggedInInfo, Integer demographicId) {
		PHRVerification result = phrVerificationDao.findLatestByDemographicId(demographicId);

		return (result);
	}

	public String getPhrVerificationLevelByDemographicId(LoggedInInfo loggedInInfo, Integer demographicId) {
		PHRVerification phrVerification = getLatestPhrVerificationByDemographicId(loggedInInfo, demographicId);

		if (phrVerification != null) {
			String authLevel = phrVerification.getVerificationLevel();
			if (PHRVerification.VERIFICATION_METHOD_FAX.equals(authLevel) || PHRVerification.VERIFICATION_METHOD_MAIL.equals(authLevel) || PHRVerification.VERIFICATION_METHOD_EMAIL.equals(authLevel)) {
				return PHR_VERIFICATION_LEVEL_1;
			} else if (PHRVerification.VERIFICATION_METHOD_TEL.equals(authLevel) || PHRVerification.VERIFICATION_METHOD_VIDEOPHONE.equals(authLevel)) {
				return PHR_VERIFICATION_LEVEL_2;
			} else if (PHRVerification.VERIFICATION_METHOD_INPERSON.equals(authLevel)) {
				return PHR_VERIFICATION_LEVEL_3;
			}
		}

		// blank string because preserving existing behaviour moved from PHRVerificationDao, I would have preferred returnning null on a new method...
		return ("");
	}

	/**
	 * This method should only return true if the demographic passed in is "phr verified" to a sufficient level to allow a provider to send this phr account messages.
	 */
	public boolean isPhrVerifiedToSendMessages(LoggedInInfo loggedInInfo, Integer demographicId) {
		String level = getPhrVerificationLevelByDemographicId(loggedInInfo, demographicId);
		// hard coded to 3 until some one tells me how to configure/check this
		if (PHR_VERIFICATION_LEVEL_3.equals(level)) return (true);
		else return (false);
	}

	/**
	 * This method should only return true if the demographic passed in is "phr verified" to a sufficient level to allow a provider to send this phr account medicalData.
	 */
	public boolean isPhrVerifiedToSendMedicalData(LoggedInInfo loggedInInfo, Integer demographicId) {
		String level = getPhrVerificationLevelByDemographicId(loggedInInfo, demographicId);
		// hard coded to 3 until some one tells me how to configure/check this
		if (PHR_VERIFICATION_LEVEL_3.equals(level)) return (true);
		else return (false);
	}

	/**
	 * @deprecated there should be a generic call for getDemographicExt(Integer demoId, String key) instead. Then the caller should assemble what it needs from the demographic and ext call itself.
	 */
	public String getDemographicWorkPhoneAndExtension(LoggedInInfo loggedInInfo, Integer demographicNo) {
		
		Demographic result = demographicDao.getDemographicById(demographicNo);
		String workPhone = result.getPhone2();
		if (workPhone != null && workPhone.length() > 0) {
			String value = demographicExtDao.getValueForDemoKey(demographicNo, "wPhoneExt");
			if (value != null && value.length() > 0) {
				workPhone += "x" + value;
			}
		}

		return (workPhone);
	}

	/**
	 * see DemographicDao.findByAttributes for parameter details
	 */
	public List<Demographic> searchDemographicsByAttributes(LoggedInInfo loggedInInfo, String hin, String firstName, String lastName, Gender gender, Calendar dateOfBirth, String city, String province, String phone, String email, String alias, int startIndex, int itemsToReturn) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		List<Demographic> results = demographicDao.findByAttributes(hin, firstName, lastName, gender, dateOfBirth, city, province, phone, email, alias, startIndex, itemsToReturn);

		return (results);
	}

	public List<String> getPatientStatusList() {
		return demographicDao.search_ptstatus();
	}

	public List<String> getRosterStatusList() {
		return demographicDao.getRosterStatuses();
	}

	public List<DemographicSearchResult> searchPatients(LoggedInInfo loggedInInfo, DemographicSearchRequest searchRequest, int startIndex, int itemsToReturn) {
		List<DemographicSearchResult> results = demographicDao.searchPatients(loggedInInfo, searchRequest, startIndex, itemsToReturn);

		return results;
	}

	public int searchPatientsCount(LoggedInInfo loggedInInfo, DemographicSearchRequest searchRequest) {
		return demographicDao.searchPatientCount(loggedInInfo, searchRequest);
	}

	/**
	 * programId can be null for all/any program
	 */
	public List<Integer> getAdmittedDemographicIdsByProgramAndProvider(LoggedInInfo loggedInInfo, Integer programId, String providerNo) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Integer> demographicIds = admissionDao.getAdmittedDemographicIdByProgramAndProvider(programId, providerNo);

		return (demographicIds);
	}
	
	public List<Integer> getDemographicIdsWithMyOscarAccounts(LoggedInInfo loggedInInfo, Integer startDemographicIdExclusive, int itemsToReturn) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Integer> demographicIds = demographicDao.getDemographicIdsWithMyOscarAccounts(startDemographicIdExclusive, itemsToReturn);

		return (demographicIds);
	}

	public List<Demographic> getDemographics(LoggedInInfo loggedInInfo, List<Integer> demographicIds) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Demographic> demographics = demographicDao.getDemographics(demographicIds);

		return (demographics);
	}
	
	public List<Demographic> searchDemographic(LoggedInInfo loggedInInfo, String searchStr) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Demographic> demographics = demographicDao.searchDemographic(searchStr);

		return (demographics);
	}
	
	public List<Demographic> getActiveDemosByHealthCardNo(LoggedInInfo loggedInInfo, String hcn, String hcnType) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));
		
		List<Demographic> demographics = demographicDao.getActiveDemosByHealthCardNo(hcn, hcnType);
		
		return (demographics);
	}

	public List<Integer> getMergedDemographicIds(LoggedInInfo loggedInInfo, Integer demographicNo) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Integer> ids = demographicDao.getMergedDemographics(demographicNo);

		return ids;
	}

	public List<Demographic> getDemosByChartNo(LoggedInInfo loggedInInfo, String chartNo) {
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));

		List<Demographic> demographics = demographicDao.getClientsByChartNo(chartNo);

		return (demographics);
	}

	public List<Demographic> searchByHealthCard(LoggedInInfo loggedInInfo, String hin) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);

		List<Demographic> demographics = demographicDao.searchByHealthCard(hin);

		return (demographics);
	}

	public Demographic getDemographicByNamePhoneEmail(LoggedInInfo loggedInInfo, String firstName, String lastName,
			String hPhone, String wPhone, String email) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);

		Demographic demographic = demographicDao.getDemographicByNamePhoneEmail(firstName, lastName, hPhone, wPhone, email);

		return (demographic);
	}

	public List<Demographic> getDemographicWithLastFirstDOB(LoggedInInfo loggedInInfo, String lastname,
			String firstname, String year_of_birth, String month_of_birth, String date_of_birth) {
		if (loggedInInfo == null) throw (new SecurityException("user not logged in?"));
		checkPrivilege(loggedInInfo, SecurityInfoManager.READ);

		List<Demographic> results = demographicDao.getDemographicWithLastFirstDOB(lastname, firstname, year_of_birth, month_of_birth, date_of_birth);

		return (results);
	}

	private void checkPrivilege(LoggedInInfo loggedInInfo, String privilege) {
		if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", privilege, null)) {
			throw new RuntimeException("missing required security object (_demographic)");
		}
	}

	private void checkPrivilege(LoggedInInfo loggedInInfo, String privilege, int demographicNo) {
		if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", privilege, demographicNo)) {
			throw new RuntimeException("missing required security object (_demographic)");
		}
	}
}
