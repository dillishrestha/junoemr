/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved. *
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. *
 *
 * Jason Gallagher
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada   Creates a new instance of ImportDemographicDataAction
 *
 *
 * * ImportDemographicDataAction4.java
 *
 * Created on Oct 2, 2007
 */

package oscar.oscarDemographic.pageUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.xmlbeans.XmlException;
import org.oscarehr.PMmodule.dao.AdmissionDao;
import org.oscarehr.PMmodule.model.Admission;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.casemgmt.model.CaseManagementIssue;
import org.oscarehr.casemgmt.model.CaseManagementNote;
import org.oscarehr.casemgmt.model.CaseManagementNoteExt;
import org.oscarehr.casemgmt.model.CaseManagementNoteLink;
import org.oscarehr.casemgmt.model.Issue;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.oscarehr.common.dao.DemographicContactDao;
import org.oscarehr.common.dao.DrugDao;
import org.oscarehr.common.dao.DrugReasonDao;
import org.oscarehr.common.model.DemographicContact;
import org.oscarehr.common.model.Drug;
import org.oscarehr.common.model.Provider;
import org.oscarehr.hospitalReportManager.dao.HRMDocumentDao;
import org.oscarehr.hospitalReportManager.dao.HRMDocumentSubClassDao;
import org.oscarehr.hospitalReportManager.dao.HRMDocumentToDemographicDao;
import org.oscarehr.hospitalReportManager.model.HRMDocument;
import org.oscarehr.hospitalReportManager.model.HRMDocumentToDemographic;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.appt.ApptStatusData;
import oscar.dms.EDocUtil;
import oscar.oscarDemographic.data.DemographicData;
import oscar.oscarDemographic.data.DemographicData.DemographicAddResult;
import oscar.oscarDemographic.data.DemographicExt;
import oscar.oscarEncounter.data.EctProgram;
import oscar.oscarEncounter.oscarMeasurements.data.ImportExportMeasurements;
import oscar.oscarEncounter.oscarMeasurements.data.Measurements;
import oscar.oscarEncounter.oscarMeasurements.model.MeasurementsExt;
import oscar.oscarLab.LabRequestReportLink;
import oscar.oscarLab.ca.on.LabResultImport;
import oscar.oscarPrevention.PreventionData;
import oscar.oscarProvider.data.ProviderData;
import oscar.oscarRx.data.RxAllergyImport;
import oscar.service.OscarSuperManager;
import oscar.util.StringUtils;
import oscar.util.UtilDateUtilities;
import cds.AlertsAndSpecialNeedsDocument.AlertsAndSpecialNeeds;
import cds.AllergiesAndAdverseReactionsDocument.AllergiesAndAdverseReactions;
import cds.AppointmentsDocument.Appointments;
import cds.CareElementsDocument.CareElements;
import cds.ClinicalNotesDocument.ClinicalNotes;
import cds.DemographicsDocument.Demographics;
import cds.FamilyHistoryDocument.FamilyHistory;
import cds.ImmunizationsDocument.Immunizations;
import cds.LaboratoryResultsDocument.LaboratoryResults;
import cds.MedicationsAndTreatmentsDocument.MedicationsAndTreatments;
import cds.OmdCdsDocument;
import cds.PastHealthDocument.PastHealth;
import cds.PatientRecordDocument.PatientRecord;
import cds.PersonalHistoryDocument.PersonalHistory;
import cds.ProblemListDocument.ProblemList;
import cds.ReportsReceivedDocument.ReportsReceived;
import cds.RiskFactorsDocument.RiskFactors;
import cdsDt.PersonNameStandard.LegalName.OtherName;
import org.oscarehr.hospitalReportManager.dao.HRMDocumentCommentDao;
import org.oscarehr.hospitalReportManager.model.HRMDocumentComment;
import org.oscarehr.hospitalReportManager.model.HRMDocumentSubClass;

/**
 *
 * @author Ronnie Cheng
 */
    public class ImportDemographicDataAction4 extends Action {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String PATIENTID = "Patient";
    private static final String ALERT = "Alert";
    private static final String ALLERGY = "Allergy";
    private static final String APPOINTMENT = "Appointment";
    private static final String CAREELEMENTS = "Care";
    private static final String CLINICALNOTE = "Clinical";
    private static final String FAMILYHISTORY = "Family";
    private static final String IMMUNIZATION = "Immunization";
    private static final String LABS = "Labs";
    private static final String MEDICATION = "Medication";
    private static final String PASTHEALTH = "Past";
    private static final String PERSONALHISTORY = "Personal";
    private static final String PROBLEMLIST = "Problem";
    private static final String REPORTBINARY = "Binary";
    private static final String REPORTTEXT = "Text";
    private static final String RISKFACTOR = "Risk";


    boolean matchProviderNames = true;
    String admProviderNo = null;
    String demographicNo = null;
    String patientName = null;
    String programId = null;
    HashMap<String, Integer> entries = new HashMap<String, Integer>();
    Integer importNo = 0;

    ProgramManager programManager = (ProgramManager) SpringUtils.getBean("programManager");
    AdmissionManager admissionManager = (AdmissionManager) SpringUtils.getBean("admissionManager");
    AdmissionDao admissionDao = (AdmissionDao) SpringUtils.getBean("admissionDao");
    CaseManagementManager caseManagementManager = (CaseManagementManager) SpringUtils.getBean("caseManagementManager");
    DrugDao drugDao = (DrugDao) SpringUtils.getBean("drugDao");
    DrugReasonDao drugReasonDao = (DrugReasonDao) SpringUtils.getBean("drugReasonDao");
    OscarSuperManager oscarSuperManager = (OscarSuperManager) SpringUtils.getBean("oscarSuperManager");

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception  {
        admProviderNo = (String) request.getSession().getAttribute("user");
        programId = new EctProgram(request.getSession()).getProgram(admProviderNo);
        String tmpDir = oscar.OscarProperties.getInstance().getProperty("TMP_DIR");
        tmpDir = Util.fixDirName(tmpDir);
        if (!Util.checkDir(tmpDir)) {
            logger.debug("Error! Cannot write to TMP_DIR - Check oscar.properties or dir permissions.");
        }

        ImportDemographicDataForm frm = (ImportDemographicDataForm) form;
        logger.info("import to course id "  + frm.getCourseId() + " using timeshift value " + frm.getTimeshiftInDays());
        List<Provider> students = new ArrayList<Provider>();
        int courseId = 0;
        if(frm.getCourseId()!=null && frm.getCourseId().length()>0) {
            courseId = Integer.valueOf(frm.getCourseId());
            if(courseId>0) {
                logger.info("need to apply this import to a learning environment");
                //get all the students from this course
                List<ProgramProvider> courseProviders = programManager.getProgramProviders(String.valueOf(courseId));
                for(ProgramProvider pp:courseProviders) {
                    if(pp.getRole().getName().equalsIgnoreCase("student")) {
                        students.add(pp.getProvider());
                    }
                }
            }
        }
        logger.info("apply this patient to " + students.size() + " students");
        matchProviderNames = frm.getMatchProviderNames();
        FormFile imp = frm.getImportFile();
        String ifile = tmpDir + imp.getFileName();
        ArrayList<String> warnings = new ArrayList<String>();
        ArrayList<String[]> logs = new ArrayList<String[]>();
        File importLog = null;

	try {
            InputStream is = imp.getInputStream();
            OutputStream os = new FileOutputStream(ifile);
            byte[] buf = new byte[1024];
            int len;
            while ((len=is.read(buf)) > 0) os.write(buf,0,len);
            is.close();
            os.close();

            if (matchFileExt(ifile, "zip")) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(ifile));
                boolean noXML = true;
                ZipEntry entry = in.getNextEntry();
                String entryDir = "";

                while (entry!=null) {
                    String entryName = entry.getName();
                    if (entry.isDirectory()) entryDir = entryName;
                    if (entryName.startsWith(entryDir)) entryName = entryName.substring(entryDir.length());

                    String ofile = tmpDir + entryName;
                    if (matchFileExt(ofile, "xml")) {
                        noXML = false;
                        OutputStream out = new FileOutputStream(ofile);
                        while ((len=in.read(buf)) > 0) out.write(buf,0,len);
                        out.close();
                        logs.add(importXML(ofile, warnings, request,frm.getTimeshiftInDays(),students,courseId));
                        importNo++;
                        demographicNo=null;
                    }
                    entry = in.getNextEntry();
                }
                if (noXML) {
                    Util.cleanFile(ifile);
                        throw new Exception ("Error! No XML file in zip");
                } else {
                    importLog = makeImportLog(logs, tmpDir);
                }
                in.close();
                Util.cleanFile(ifile);

            } else if (matchFileExt(ifile, "xml")) {
                logs.add(importXML(ifile, warnings, request,frm.getTimeshiftInDays(),students,courseId));
                demographicNo=null;
                importLog = makeImportLog(logs, tmpDir);
            } else {
                Util.cleanFile(ifile);
                throw new Exception ("Error! Import file must be XML or ZIP");
            }
	} catch (Exception e) {
            warnings.add("Error processing file: " + imp.getFileName());
            logger.error("Error", e);
	}

        //channel warnings and importlog to browser
        request.setAttribute("warnings",warnings);
        if (importLog!=null) request.setAttribute("importlog",importLog.getPath());

        return mapping.findForward("success");
    }

    String[] importXML(String xmlFile, ArrayList warnings, HttpServletRequest request, int timeShiftInDays,List<Provider> students, int courseId) throws SQLException, Exception {
        if(students == null || students.isEmpty()) {
            return importXML(xmlFile,warnings,request,timeShiftInDays,null,null,0);
        }
        
        List<String> logs = new ArrayList<String>();
        
        for(Provider student:students) {
            logger.info("importing patient for student " +  student.getFormattedName());
            //need that student's personal program
            Integer pid = programManager.getProgramIdByProgramName("program"+student.getProviderNo());
            if(pid == null) {
                logger.warn("student's program not found");
                continue;
            }
            Program p = programManager.getProgram(pid);

            String[] result = importXML(xmlFile,warnings,request,timeShiftInDays,student,p,courseId);
            logs.addAll(convertLog(result));
        }
        return logs.toArray(new String[logs.size()]);
    }
    
    private List<String> convertLog(String[] logs) {
    	List<String> tmp = new ArrayList<String>();
        tmp.addAll(Arrays.asList(logs));
    	return tmp;
    }



    String[] importXML(String xmlFile, ArrayList warnings, HttpServletRequest request, int timeShiftInDays, Provider student, Program admitTo, int courseId) throws SQLException, Exception {
        ArrayList<String> err_demo = new ArrayList<String>(); //errors: duplicate demographics
        ArrayList<String> err_data = new ArrayList<String>(); //errors: discrete data
        ArrayList<String> err_summ = new ArrayList<String>(); //errors: summary
        ArrayList<String> err_othe = new ArrayList<String>(); //errors: other categories
        ArrayList<String> err_note = new ArrayList<String>(); //non-errors: notes

        String defaultProvider = getDefaultProvider();
        String docDir = oscar.OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
        docDir = Util.fixDirName(docDir);
        if (!Util.checkDir(docDir)) {
                logger.debug("Error! Cannot write to DOCUMENT_DIR - Check oscar.properties or dir permissions.");
        }

        File xmlF = new File(xmlFile);
        OmdCdsDocument.OmdCds omdCds=null;
        try {
            omdCds = OmdCdsDocument.Factory.parse(xmlF).getOmdCds();
        } catch (IOException ex) {logger.error("Error", ex);
        } catch (XmlException ex) {logger.error("Error", ex);
        }
        PatientRecord patientRec = omdCds.getPatientRecord();

        //DEMOGRAPHICS
        Demographics demo = patientRec.getDemographics();
        cdsDt.PersonNameStandard.LegalName legalName = demo.getNames().getLegalName();
        String lastName="", firstName="";
        if (legalName!=null) {
            if (legalName.getLastName()!=null) lastName = StringUtils.noNull(legalName.getLastName().getPart());
            if (legalName.getFirstName()!=null) firstName = StringUtils.noNull(legalName.getFirstName().getPart());
            patientName = lastName+","+firstName;
        } else {
            err_data.add("Error! No Legal Name");
        }
        OtherName[] otherNames = legalName.getOtherNameArray();
        String otherNameTxt = null;
        for (OtherName otherName : otherNames) {
            if (otherNameTxt==null) otherNameTxt = otherName.getPart();
            else otherNameTxt += " "+otherName.getPart();
        }

        String title = demo.getNames().getNamePrefix()!=null ? demo.getNames().getNamePrefix().toString() : "";
        String suffix = demo.getNames().getLastNameSuffix()!=null ? demo.getNames().getLastNameSuffix().toString() : "";
        String sex = demo.getGender()!=null ? demo.getGender().toString() : "";
        if (StringUtils.empty(sex)) {
            err_data.add("Error! No Gender");
        }
        String birthDate = getCalDate(demo.getDateOfBirth(), timeShiftInDays);
        if (StringUtils.empty(birthDate)) {
            birthDate = null;
            err_data.add("Error! No Date Of Birth");
        }
        String versionCode="", hin="", hc_type="", hc_renew_date="";
        cdsDt.HealthCard healthCard = demo.getHealthCard();
        if (healthCard!=null) {
            hin = StringUtils.noNull(healthCard.getNumber());
            if (hin.equals("")) {
                err_data.add("Error! No health card number");
            }
            hc_type = getProvinceCode(healthCard.getProvinceCode());
            if (hc_type.equals("")) {
                err_data.add("Error! No Province Code for health card");
            }
            versionCode = StringUtils.noNull(healthCard.getVersion());
            hc_renew_date = getCalDate(healthCard.getExpirydate());
            if (StringUtils.empty(hc_renew_date)) err_data.add("Error! No health card expiry date");
        }

        //Check duplicate
        DemographicData dd = new DemographicData();
        ArrayList demodup = null;
        if (StringUtils.filled(hin)) demodup = dd.getDemographicWithHIN(hin);
        else demodup = dd.getDemographicWithLastFirstDOB(lastName, firstName, birthDate);
        if (demodup.size()>0) {
            err_data.clear();
            err_demo.add("Error! Patient "+patientName+" already exist! Not imported.");
            return packMsgs(err_demo, err_data, err_summ, err_othe, err_note, warnings);
        }


        String patient_status = null;
        Demographics.PersonStatusCode personStatusCode = demo.getPersonStatusCode();
        if (personStatusCode!=null) {
            if (personStatusCode.getPersonStatusAsEnum()!=null) {
                if (personStatusCode.getPersonStatusAsEnum().equals(cdsDt.PersonStatus.A)) patient_status = "AC";
                if (personStatusCode.getPersonStatusAsEnum().equals(cdsDt.PersonStatus.I)) patient_status = "IN";
                if (personStatusCode.getPersonStatusAsEnum().equals(cdsDt.PersonStatus.D)) patient_status = "DE";
            } else if (personStatusCode.getPersonStatusAsPlainText()!=null) {
                patient_status = personStatusCode.getPersonStatusAsPlainText();
            } else {
                err_data.add("Error! No Person Status Code");
            }
        } else {
            err_data.add("Error! No Person Status Code");
        }

        String roster_status=null, roster_date=null, term_date=null;
        Demographics.Enrolment[] enrolments = demo.getEnrolmentArray();
        if (enrolments.length>0) {
            roster_status = enrolments[0].getEnrollmentStatus()!=null ? enrolments[0].getEnrollmentStatus().toString() : "";
            if	(roster_status.equals("1")) roster_status = "RO";
            else if (roster_status.equals("0")) roster_status = "NR";
            roster_date = getCalDate(enrolments[0].getEnrollmentDate(), timeShiftInDays);
            term_date = getCalDate(enrolments[0].getEnrollmentTerminationDate(), timeShiftInDays);
        }
        String sin = StringUtils.noNull(demo.getSIN());

        String chart_no = StringUtils.noNull(demo.getChartNumber());
        String official_lang = "";
        if (demo.getPreferredOfficialLanguage()!=null) {
            official_lang = demo.getPreferredOfficialLanguage().toString();
            official_lang = official_lang.equals("ENG") ? "English" : official_lang;
            official_lang = official_lang.equals("FRE") ? "French" : official_lang;
        }
        String spoken_lang = StringUtils.noNull(Util.convertCodeToLanguage(demo.getPreferredSpokenLanguage()));
        String dNote = StringUtils.noNull(demo.getNoteAboutPatient());
        String uvID = demo.getUniqueVendorIdSequence();
        String psDate = getCalDate(demo.getPersonStatusDate(), timeShiftInDays);

        if (StringUtils.filled(otherNameTxt)) {
            dNote = Util.addLine(dNote, "Other name: ", otherNameTxt);
            err_note.add("Other name(s) imported to Patient Note");
        }

        if (StringUtils.filled(suffix)) {
            dNote = Util.addLine(dNote, "Lastname suffix: ", suffix);
            err_note.add("Lastname suffix imported to Patient Note");
        }

        if (StringUtils.filled(uvID)) {
            if (StringUtils.empty(chart_no)) {
                chart_no = uvID;
                err_note.add("Unique Vendor Id imported as Chart No");
            } else {
                dNote = Util.addLine(dNote, "Unique Vendor ID: ", uvID);
                err_note.add("Unique Vendor Id imported to Patient Note");
            }
        } else {
            err_data.add("Error! No Unique Vendor ID Sequence");
        }

        String address="", city="", province="", postalCode="";
        if (demo.getAddressArray().length>0) {
            cdsDt.Address addr = demo.getAddressArray(0);	//only get 1st address, other ignored
            if (addr!=null) {
                if (StringUtils.filled(addr.getFormatted())) {
                    address = addr.getFormatted();
                } else {
                    cdsDt.AddressStructured addrStr = addr.getStructured();
                    if (addrStr!=null) {
                        address = StringUtils.noNull(addrStr.getLine1()) + StringUtils.noNull(addrStr.getLine2()) + StringUtils.noNull(addrStr.getLine3());
                        city = StringUtils.noNull(addrStr.getCity());
                        province = getCountrySubDivCode(addrStr.getCountrySubdivisionCode());
                        cdsDt.PostalZipCode postalZip = addrStr.getPostalZipCode();
                        if (postalZip!=null) postalCode = StringUtils.noNull(postalZip.getPostalCode());
                    }
                }
            }
        }
        cdsDt.PhoneNumber[] pn = demo.getPhoneNumberArray();
        String workPhone="", workExt="", homePhone="", homeExt="", cellPhone="", ext="", patientPhone="";
        for (int i=0; i<pn.length; i++) {
            String phone = pn[i].getPhoneNumber();
            if (StringUtils.empty(phone)) {
                if (StringUtils.filled(pn[i].getNumber())) {
                    String areaCode = StringUtils.filled(pn[i].getAreaCode()) ? "("+pn[i].getAreaCode()+")" : "";
                    phone = areaCode + pn[i].getNumber();
                }
            }
            if (StringUtils.filled(phone)) {
                if (StringUtils.filled(pn[i].getExtension())) ext = pn[i].getExtension();
                else if (StringUtils.filled(pn[i].getExchange())) ext = pn[i].getExchange();

                if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.W) {
                    workPhone = phone;
                    workExt   = ext;
                } else if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.R) {
                    homePhone = phone;
                    homeExt   = ext;
                } else if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.C) {
                    cellPhone = phone;
                }
            }
        }
        if      (StringUtils.filled(homePhone)) patientPhone = homePhone+" "+homeExt;
        else if (StringUtils.filled(workPhone)) patientPhone = workPhone+" "+workExt;
        else if (StringUtils.filled(cellPhone)) patientPhone = cellPhone;
        String email = StringUtils.noNull(demo.getEmail());

        String primaryPhysician = "";
        if(student == null){
            Demographics.PrimaryPhysician demoPrimaryPhysician = demo.getPrimaryPhysician();
            if (demoPrimaryPhysician!=null) {
                HashMap<String,String> personName = getPersonName(demoPrimaryPhysician.getName());
                String personOHIP = demoPrimaryPhysician.getOHIPPhysicianId();
                if (StringUtils.empty(personName.get("firstname"))) err_data.add("Error! No Primary Physician first name");
                if (StringUtils.empty(personName.get("lastname"))) err_data.add("Error! No Primary Physician last name");
                if (StringUtils.empty(personOHIP)) err_data.add("Error! No Primary Physician OHIP billing number");
                String personCPSO = demoPrimaryPhysician.getPrimaryPhysicianCPSO();
                primaryPhysician = writeProviderData(personName.get("firstname"), personName.get("lastname"), personOHIP, personCPSO);
            }
            if (StringUtils.empty(primaryPhysician)) {
                primaryPhysician = defaultProvider;
                err_data.add("Error! No Primary Physician; patient assigned to \"doctor oscardoc\"");
            }
        } else {
            primaryPhysician = student.getProviderNo();
        }

        String year_of_birth = null;
        String month_of_birth = null;
        String date_of_birth = null;
        if (birthDate!=null)
        {
            Date bDate = UtilDateUtilities.StringToDate(birthDate,"yyyy-MM-dd");
            year_of_birth = UtilDateUtilities.DateToString(bDate,"yyyy");
            month_of_birth = UtilDateUtilities.DateToString(bDate,"MM");
            date_of_birth = UtilDateUtilities.DateToString(bDate,"dd");
        }

        DemographicExt dExt = new DemographicExt();
        DemographicAddResult demoRes = null;

        //Check if Contact-only demographic exists
        DemographicData.Demographic demographic = null;

        if(courseId == 0) {
            demographicNo = dd.getDemoNoByNamePhoneEmail(firstName, lastName, homePhone, workPhone, email);
            demographic = dd.getDemographic(demographicNo);
        }
        if (demographic==null) { //demo not found, add patient
            demoRes = dd.addDemographic(title, lastName, firstName, address, city, province, postalCode, homePhone, workPhone, year_of_birth, month_of_birth, date_of_birth, hin, versionCode, roster_status, roster_date, term_date, patient_status, psDate, ""/*date_joined*/, chart_no, official_lang, spoken_lang, primaryPhysician, sex, ""/*end_date*/, ""/*eff_date*/, ""/*pcn_indicator*/, hc_type, hc_renew_date, ""/*family_doctor*/, email, ""/*pin*/, ""/*alias*/, ""/*previousAddress*/, ""/*children*/, ""/*sourceOfIncome*/, ""/*citizenship*/, sin);
            demographicNo = demoRes.getId();
            entries.put(PATIENTID+importNo, Integer.valueOf(demographicNo));
        } else if (StringUtils.nullSafeEqualsIgnoreCase(demographic.getPatientStatus(), "Contact-only")) { //replace contact
            demographic.setTitle(title);
            demographic.setAddress(address);
            demographic.setCity(city);
            demographic.setProvince(province);
            demographic.setPostal(postalCode);
            demographic.setYearOfBirth(year_of_birth);
            demographic.setMonthOfBirth(month_of_birth);
            demographic.setDateOfBirth(date_of_birth);
            demographic.setJustHIN(hin);
            demographic.setVersionCode(versionCode);
            demographic.setRosterStatus(roster_status);
            demographic.setRosterDate(roster_date);
            demographic.setRosterTerminationDate(term_date);
            demographic.setPatientStatus(patient_status);
            demographic.setPatientStatusDate(psDate);
            demographic.setChartNo(chart_no);
            demographic.setOfficialLang(official_lang);
            demographic.setSpokenLang(spoken_lang);
            demographic.setFamilyDoctor(primaryPhysician);
            demographic.setSex(sex);
            demographic.setHCType(hc_type);
            demographic.setHCRenewDate(hc_renew_date);
            demographic.setSin(sin);
            dd.setDemographic(demographic);
            err_note.add("Replaced Contact-only patient "+patientName+" (Demo no="+demographicNo+")");
            demoRes = dd.addDemographic(title, lastName, firstName, address, city, province, postalCode, homePhone, workPhone, year_of_birth, month_of_birth, date_of_birth, hin, versionCode, roster_status, roster_date, term_date, patient_status, psDate, ""/*date_joined*/, chart_no, official_lang, spoken_lang, primaryPhysician, sex, ""/*end_date*/, ""/*eff_date*/, ""/*pcn_indicator*/, hc_type, hc_renew_date, ""/*family_doctor*/, email, ""/*pin*/, ""/*alias*/, ""/*previousAddress*/, ""/*children*/, ""/*sourceOfIncome*/, ""/*citizenship*/, sin);
        }

        if (StringUtils.filled(demographicNo))
        {
            //TODO: Course - Admit to student program
            if(admitTo == null) {
                insertIntoAdmission();
            } else {
                admissionManager.processAdmission(Integer.valueOf(demographicNo), student.getProviderNo(), admitTo, "", "batch import");
            }

            if (StringUtils.filled(dNote)) dd.addDemographiccust(demographicNo, dNote);

            if (!workExt.equals("")) dExt.addKey(primaryPhysician, demographicNo, "wPhoneExt", workExt);
            if (!homeExt.equals("")) dExt.addKey(primaryPhysician, demographicNo, "hPhoneExt", homeExt);
            if (!cellPhone.equals("")) dExt.addKey(primaryPhysician, demographicNo, "demo_cell", cellPhone);
            if(courseId>0) dExt.addKey(primaryPhysician, demographicNo, "course", String.valueOf(courseId));


            //Demographic Contacts
            DemographicContactDao contactDao = (DemographicContactDao) SpringUtils.getBean("demographicContactDao");
            DemographicContact demoContact = new DemographicContact();

            Demographics.Contact[] contt = demo.getContactArray();
            for (int i=0; i<contt.length; i++) {
                HashMap<String,String> contactName = getPersonName(contt[i].getName());
                String cFirstName = StringUtils.noNull(contactName.get("firstname"));
                String cLastName  = StringUtils.noNull(contactName.get("lastname"));
                String cEmail = StringUtils.noNull(contt[i].getEmailAddress());

                pn = contt[i].getPhoneNumberArray();
                workPhone=""; workExt=""; homePhone=""; homeExt=""; cellPhone=""; ext="";
                for (int j=0; j<pn.length; j++) {
                    String phone = pn[j].getPhoneNumber();
                    if (phone==null) {
                        if (pn[j].getNumber()!=null) {
                            if (pn[j].getAreaCode()!=null) phone = pn[j].getAreaCode()+"-"+pn[j].getNumber();
                            else phone = pn[j].getNumber();
                        }
                    }
                    if (phone!=null) {
                        if (pn[j].getExtension()!=null) ext = pn[j].getExtension();
                        else if (pn[j].getExchange()!=null) ext = pn[j].getExchange();

                        if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.W) {
                            workPhone = phone;
                            workExt   = ext;
                        } else if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.R) {
                            homePhone = phone;
                            homeExt   = ext;
                        } else if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.C) {
                            cellPhone = phone;
                        }
                    }
                }

                String sdm = null, emc = null, rel = null;
                Integer type = 2;
                cdsDt.PurposeEnumOrPlainText[] contactPurposes = contt[i].getContactPurposeArray();
                for (cdsDt.PurposeEnumOrPlainText contactPurpose : contactPurposes) {
                    String cPurpose = null;
                    if (contactPurpose.getPurposeAsEnum()!=null) cPurpose = contactPurpose.getPurposeAsEnum().toString();
                    else cPurpose = contactPurpose.getPurposeAsPlainText();
                    cPurpose = cPurpose.trim();

                    if (emc==null)
                        emc = cPurpose.equals("EC") || cPurpose.equalsIgnoreCase("emergency contact") ? "true" : "";

                    if (sdm==null)
                        sdm = cPurpose.equals("SDM") || cPurpose.equalsIgnoreCase("substitute decision maker") ? "true" : "";
                    
                    if (cPurpose.equals("NK")) rel = "Next of Kin";
                    else if (cPurpose.equals("AS")) rel = "Administrative Staff";
                    else if (cPurpose.equals("CG")) rel = "Care Giver";
                    else if (cPurpose.equals("PA")) rel = "Power of Attorney";
                    else if (cPurpose.equals("IN")) rel = "Insurance";
                    else if (cPurpose.equals("GT")) rel = "Guarantor";
                    else {
                        rel = cPurpose;
                        type = 1;
                    }
                }

                String contactNote = contt[i].getNote();
                String cDemoNo = dd.getDemoNoByNamePhoneEmail(cFirstName, cLastName, homePhone, workPhone, cEmail);
                String cPatient = cLastName+","+cFirstName;
                if (StringUtils.empty(cDemoNo)) {   //add new demographic as contact
                    psDate = UtilDateUtilities.DateToString(new Date(),"yyyy-MM-dd");
                    demoRes = dd.addDemographic("", cLastName, cFirstName, "", "", "", "", homePhone, workPhone, null, null,
                    null, "", "", "", "Contact-only", psDate, "", "", "", "", "", "", "", "F", "", "", "", "", "", "", cEmail, "", "", "", "", "", "", "");
                    cDemoNo = demoRes.getId();
                    err_note.add("Contact-only patient "+cPatient+" (Demo no="+cDemoNo+") created");

                    if (StringUtils.filled(contactNote)) dd.addDemographiccust(cDemoNo, contactNote);
                    if (!workExt.equals("")) dExt.addKey("", cDemoNo, "wPhoneExt", workExt);
                    if (!homeExt.equals("")) dExt.addKey("", cDemoNo, "hPhoneExt", homeExt);
                    if (!cellPhone.equals("")) dExt.addKey("", cDemoNo, "demo_cell", cellPhone);
                }
                if (StringUtils.filled(cDemoNo)) {
                    demoContact.setCreated(new Date());
                    demoContact.setUpdateDate(new Date());
                    demoContact.setDemographicNo(Integer.valueOf(demographicNo));
                    demoContact.setContactId(cDemoNo);
                    demoContact.setEc(emc);
                    demoContact.setSdm(sdm);
                    demoContact.setRole(rel);
                    demoContact.setType(1); //should be "type" - display problem
                    demoContact.setCategory("personal");
                    contactDao.merge(demoContact);

//                  DemographicRelationship demoRel = new DemographicRelationship();
//                  demoRel.addDemographicRelationship(demographicNo, cDemoNo, rel, sdm, emc, ""/*notes*/, primaryPhysician, null);
//                  err_note.add("Added relationship with patient "+cPatient+" (Demo no="+cDemoNo+")");
                }
            }

            Set<CaseManagementIssue> scmi = null;	//Declare a set for CaseManagementIssues
            //PERSONAL HISTORY
            PersonalHistory[] pHist = patientRec.getPersonalHistoryArray();
            for (int i=0; i<pHist.length; i++) {
                if (i==0) scmi = getCMIssue("SocHistory");
                CaseManagementNote cmNote = prepareCMNote("1",null);
                cmNote.setIssues(scmi);

                //main field
                String socialHist = "Imported Personal History";
                String summary = pHist[i].getCategorySummaryLine();
                summary = Util.addLine(summary, getResidual(pHist[i].getResidualInfo()));
                if (StringUtils.empty(summary)) continue;

                cmNote.setNote(socialHist);
                caseManagementManager.saveNoteSimple(cmNote);
                addOneEntry(PERSONALHISTORY);

                //to dumpsite
                summary = Util.addLine("imported.cms4.2011.06", summary);
                Long hostNoteId = cmNote.getId();
                cmNote = prepareCMNote("2",null);
                cmNote.setNote(summary);
                saveLinkNote(hostNoteId, cmNote);
            }

            //FAMILY HISTORY
            FamilyHistory[] fHist = patientRec.getFamilyHistoryArray();
            for (int i=0; i<fHist.length; i++) {
                if (i==0) scmi = getCMIssue("FamHistory");
                CaseManagementNote cmNote = prepareCMNote("1",null);

                //diagnosis code
                if (fHist[i].getDiagnosisProcedureCode()==null) {
                    cmNote.setIssues(scmi);
                } else {
                    cmNote.setIssues(getCMIssue("FamHistory", fHist[i].getDiagnosisProcedureCode()));
                }

                //main field
                String familyHist = fHist[i].getProblemDiagnosisProcedureDescription();
                if (StringUtils.empty(familyHist)) familyHist = "Imported Family History";
                cmNote.setNote(familyHist);
                caseManagementManager.saveNoteSimple(cmNote);
                addOneEntry(FAMILYHISTORY);

                //annotation
                Long hostNoteId = cmNote.getId();
                cmNote = prepareCMNote("2",null);
                String note = StringUtils.noNull(fHist[i].getNotes());
                cmNote.setNote(note);
                saveLinkNote(hostNoteId, cmNote);

                //to dumpsite
                String dump = "imported.cms4.2011.06";
                String summary = fHist[i].getCategorySummaryLine();
                if (StringUtils.empty(summary)) {
                        err_summ.add("No Summary for Family History ("+(i+1)+")");
                }
                String diagCode = isICD9(fHist[i].getDiagnosisProcedureCode()) ? null : getCode(fHist[i].getDiagnosisProcedureCode(),"Diagnosis/Procedure");
                dump = Util.addLine(dump, summary);
                dump = Util.addLine(dump, diagCode);
                dump = Util.addLine(dump, getResidual(fHist[i].getResidualInfo()));
                cmNote = prepareCMNote("2",null);
                cmNote.setNote(dump);
                saveLinkNote(hostNoteId, cmNote);

                //extra fields
                CaseManagementNoteExt cme = new CaseManagementNoteExt();
                cme.setNoteId(hostNoteId);
                if (fHist[i].getStartDate()!=null) {
                    cme.setKeyVal(CaseManagementNoteExt.STARTDATE);
                    cme.setDateValue(dateFPtoDate(fHist[i].getStartDate(), timeShiftInDays));
                    cme.setValue(dateFPGetPartial(fHist[i].getStartDate()));
                    caseManagementManager.saveNoteExt(cme);
                }
                if (fHist[i].getAgeAtOnset()!=null) {
                    cme.setKeyVal(CaseManagementNoteExt.AGEATONSET);
                    cme.setValue(fHist[i].getAgeAtOnset().toString());
                    caseManagementManager.saveNoteExt(cme);
                }
                if (StringUtils.filled(fHist[i].getRelationship())) {
                    cme.setKeyVal(CaseManagementNoteExt.RELATIONSHIP);
                    cme.setValue(fHist[i].getRelationship());
                    caseManagementManager.saveNoteExt(cme);
                }
                if (StringUtils.filled(fHist[i].getTreatment())) {
                    cme.setKeyVal(CaseManagementNoteExt.TREATMENT);
                    cme.setValue(fHist[i].getTreatment());
                    caseManagementManager.saveNoteExt(cme);
                }
                if (fHist[i].getLifeStage()!=null) {
                    cme.setKeyVal(CaseManagementNoteExt.LIFESTAGE);
                    cme.setValue(fHist[i].getLifeStage().toString());
                    caseManagementManager.saveNoteExt(cme);
                }
            }

            //PAST HEALTH
            PastHealth[] pHealth = patientRec.getPastHealthArray();
            for (int i=0; i< pHealth.length; i++) {
                if (i==0) scmi = getCMIssue("MedHistory");
                CaseManagementNote cmNote = prepareCMNote("1",null);

                //diagnosis code
                if (pHealth[i].getDiagnosisProcedureCode()==null) {
                    cmNote.setIssues(scmi);
                } else {
                    cmNote.setIssues(getCMIssue("MedHistory", pHealth[i].getDiagnosisProcedureCode()));
                }

                //main field
                String medicalHist = pHealth[i].getPastHealthProblemDescriptionOrProcedures();
                if (StringUtils.empty(medicalHist)) medicalHist = "Imported Medical History";
                cmNote.setNote(medicalHist);
                caseManagementManager.saveNoteSimple(cmNote);
                addOneEntry(FAMILYHISTORY);

                //annotation
                Long hostNoteId = cmNote.getId();
                cmNote = prepareCMNote("2",null);
                String note = pHealth[i].getNotes();
                cmNote.setNote(note);
                saveLinkNote(hostNoteId, cmNote);


                //to dumpsite
                String dump = "imported.cms4.2011.06";
                String summary = pHealth[i].getCategorySummaryLine();
                if (StringUtils.empty(summary)) {
                    err_summ.add("No Summary for Past Health ("+(i+1)+")");
                }
                String diagCode = isICD9(pHealth[i].getDiagnosisProcedureCode()) ? null : getCode(pHealth[i].getDiagnosisProcedureCode(),"Diagnosis/Procedure");
                dump = Util.addLine(dump, summary);
                dump = Util.addLine(dump, diagCode);
                dump = Util.addLine(dump, getResidual(pHealth[i].getResidualInfo()));
                cmNote = prepareCMNote("2",null);
                cmNote.setNote(dump);
                saveLinkNote(hostNoteId, cmNote);

                //extra fields
                CaseManagementNoteExt cme = new CaseManagementNoteExt();
                cme.setNoteId(hostNoteId);
                if (pHealth[i].getOnsetOrEventDate()!=null) {
                    cme.setKeyVal(CaseManagementNoteExt.STARTDATE);
                    cme.setDateValue(dateFPtoDate(pHealth[i].getOnsetOrEventDate(), timeShiftInDays));
                    cme.setValue(dateFPGetPartial(pHealth[i].getOnsetOrEventDate()));
                    caseManagementManager.saveNoteExt(cme);
                }
                    if (pHealth[i].getProcedureDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.PROCEDUREDATE);
                        cme.setDateValue(dateFPtoDate(pHealth[i].getProcedureDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(pHealth[i].getProcedureDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (pHealth[i].getResolvedDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.RESOLUTIONDATE);
                        cme.setDateValue(dateFPtoDate(pHealth[i].getResolvedDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(pHealth[i].getResolvedDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (pHealth[i].getLifeStage()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.LIFESTAGE);
                        cme.setValue(pHealth[i].getLifeStage().toString());
                        caseManagementManager.saveNoteExt(cme);
                    }
                }

                //PROBLEM LIST
                ProblemList[] probList = patientRec.getProblemListArray();
                for (int i=0; i<probList.length; i++) {
                    if (i==0) scmi = getCMIssue("Concerns");
                    CaseManagementNote cmNote = prepareCMNote("1",null);

                    //diagnosis code
                    if (probList[i].getDiagnosisCode()==null) {
                        cmNote.setIssues(scmi);
                    } else {
                        cmNote.setIssues(getCMIssue("Concerns", probList[i].getDiagnosisCode()));
                    }

                    //main field
                    String ongConcerns = probList[i].getProblemDiagnosisDescription();
                    if (StringUtils.empty(ongConcerns)) ongConcerns = "Imported Concern";
                    cmNote.setNote(ongConcerns);
                    caseManagementManager.saveNoteSimple(cmNote);
                    addOneEntry(PROBLEMLIST);

                    //annotation
                    Long hostNoteId = cmNote.getId();
                    cmNote = prepareCMNote("2",null);
                    String note = probList[i].getNotes();
                    cmNote.setNote(note);
                    saveLinkNote(hostNoteId, cmNote);


                    //to dumpsite
                    String dump = "imported.cms4.2011.06";
                    String summary = probList[i].getCategorySummaryLine();
                    if (StringUtils.empty(summary)) {
                            err_summ.add("No Summary for Problem List ("+(i+1)+")");
                    }
                    String diagCode = isICD9(probList[i].getDiagnosisCode()) ? null : getCode(probList[i].getDiagnosisCode(),"Diagnosis");
                    dump = Util.addLine(dump, summary);
                    dump = Util.addLine(dump, diagCode);
                    dump = Util.addLine(dump, getResidual(probList[i].getResidualInfo()));
                    cmNote = prepareCMNote("2",null);
                    cmNote.setNote(dump);
                    saveLinkNote(hostNoteId, cmNote);

                    //extra fields
                    CaseManagementNoteExt cme = new CaseManagementNoteExt();
                    cme.setNoteId(hostNoteId);
                    if (StringUtils.filled(probList[i].getProblemDescription())) {
                        cme.setKeyVal(CaseManagementNoteExt.PROBLEMDESC);
                        cme.setValue(probList[i].getProblemDescription());
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (probList[i].getOnsetDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.STARTDATE);
                        cme.setDateValue(dateFPtoDate(probList[i].getOnsetDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(probList[i].getOnsetDate()));
                        caseManagementManager.saveNoteExt(cme);
                    } else {
                        err_data.add("Error! No Onset Date for Problem List ("+(i+1)+")");
                    }
                    if (probList[i].getResolutionDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.RESOLUTIONDATE);
                        cme.setDateValue(dateFPtoDate(probList[i].getResolutionDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(probList[i].getResolutionDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (StringUtils.filled(probList[i].getProblemStatus())) {
                        cme.setKeyVal(CaseManagementNoteExt.PROBLEMSTATUS);
                        cme.setValue(probList[i].getProblemStatus());
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (probList[i].getLifeStage()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.LIFESTAGE);
                        cme.setValue(probList[i].getLifeStage().toString());
                        caseManagementManager.saveNoteExt(cme);
                    }
                }

                //RISK FACTORS
                RiskFactors[] rFactors = patientRec.getRiskFactorsArray();
                for (int i=0; i<rFactors.length; i++) {
                    if (i==0) scmi = getCMIssue("RiskFactors");
                    CaseManagementNote cmNote = prepareCMNote("1",null);
                    cmNote.setIssues(scmi);

                    //main field
                    String riskFactors = rFactors[i].getRiskFactor();
                    if (StringUtils.empty(riskFactors)) riskFactors = "Imported Risk Factor";
                    cmNote.setNote(riskFactors);
                    caseManagementManager.saveNoteSimple(cmNote);
                    addOneEntry(RISKFACTOR);

                    //annotation
                    Long hostNoteId = cmNote.getId();
                    cmNote = prepareCMNote("2",null);
                    String note = rFactors[i].getNotes();
                    cmNote.setNote(note);
                    saveLinkNote(hostNoteId, cmNote);

                    //to dumpsite
                    String dump = "imported.cms4.2011.06";
                    String summary = rFactors[i].getCategorySummaryLine();
                    if (StringUtils.empty(summary)) {
                        err_summ.add("No Summary for Risk Factors ("+(i+1)+")");
                    }
                    dump = Util.addLine(dump, summary);
                    dump = Util.addLine(dump, getResidual(rFactors[i].getResidualInfo()));
                    cmNote = prepareCMNote("2",null);
                    cmNote.setNote(dump);
                    saveLinkNote(hostNoteId, cmNote);

                    //extra fields
                    CaseManagementNoteExt cme = new CaseManagementNoteExt();
                    cme.setNoteId(hostNoteId);
                    if (rFactors[i].getStartDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.STARTDATE);
                        cme.setDateValue(dateFPtoDate(rFactors[i].getStartDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(rFactors[i].getStartDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (rFactors[i].getEndDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.RESOLUTIONDATE);
                        cme.setDateValue(dateFPtoDate(rFactors[i].getEndDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(rFactors[i].getEndDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (rFactors[i].getAgeOfOnset()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.AGEATONSET);
                        cme.setValue(rFactors[i].getAgeOfOnset().toString());
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (StringUtils.filled(rFactors[i].getExposureDetails())) {
                        cme.setKeyVal(CaseManagementNoteExt.EXPOSUREDETAIL);
                        cme.setValue(rFactors[i].getExposureDetails());
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (rFactors[i].getLifeStage()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.LIFESTAGE);
                        cme.setValue(rFactors[i].getLifeStage().toString());
                        caseManagementManager.saveNoteExt(cme);
                    }
                }

                //ALERTS & SPECIAL NEEDS
                AlertsAndSpecialNeeds[] alerts = patientRec.getAlertsAndSpecialNeedsArray();
                for (int i=0; i<alerts.length; i++) {
                    if (i==0) scmi = getCMIssue("Reminders");
                    CaseManagementNote cmNote = prepareCMNote("1",null);
                    cmNote.setIssues(scmi);

                    //main field
                    String reminders = alerts[i].getAlertDescription();
                    if (StringUtils.empty(reminders)) {
                        err_data.add("Error! No Alert Description ("+(i+1)+")");
                        reminders = "Imported Alert";
                    }
                    cmNote.setNote(reminders);
                    caseManagementManager.saveNoteSimple(cmNote);
                    addOneEntry(ALERT);

                    //annotation
                    Long hostNoteId = cmNote.getId();
                    cmNote = prepareCMNote("2",null);
                    String note = alerts[i].getNotes();
                    cmNote.setNote(note);
                    saveLinkNote(hostNoteId, cmNote);

                    //to dumpsite
                    String dump = "imported.cms4.2011.06";
                    String summary = alerts[i].getCategorySummaryLine();
                    if (StringUtils.empty(summary)) {
                            err_summ.add("No Summary for Alerts & Special Needs ("+(i+1)+")");
                    }
                    dump = Util.addLine(dump, summary);
                    dump = Util.addLine(dump, getResidual(alerts[i].getResidualInfo()));
                    cmNote = prepareCMNote("2",null);
                    cmNote.setNote(dump);
                    saveLinkNote(hostNoteId, cmNote);

                    //extra fields
                    CaseManagementNoteExt cme = new CaseManagementNoteExt();
                    cme.setNoteId(hostNoteId);
                    if (alerts[i].getDateActive()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.STARTDATE);
                        cme.setDateValue(dateFPtoDate(alerts[i].getDateActive(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(alerts[i].getDateActive()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                    if (alerts[i].getEndDate()!=null) {
                        cme.setKeyVal(CaseManagementNoteExt.RESOLUTIONDATE);
                        cme.setDateValue(dateFPtoDate(alerts[i].getEndDate(), timeShiftInDays));
                        cme.setValue(dateFPGetPartial(alerts[i].getEndDate()));
                        caseManagementManager.saveNoteExt(cme);
                    }
                }

                //CLINICAL NOTES
                ClinicalNotes[] cNotes = patientRec.getClinicalNotesArray();
                for (int i=0; i<cNotes.length; i++) {
                    CaseManagementNote cmNote = prepareCMNote("1",null);

                    //observation date
                    cdsDt.DateTimeFullOrPartial eventDateTime = cNotes[i].getEventDateTime();
                    if (eventDateTime==null) cmNote.setObservation_date(new Date());
                    else cmNote.setObservation_date(dateTimeFPtoDate(eventDateTime, timeShiftInDays));

                    //encounter note
                    String encounter = cNotes[i].getMyClinicalNotesContent();
                    encounter = Util.addLine(encounter,"Note Type: ",cNotes[i].getNoteType());
                    if (StringUtils.filled(encounter)) cmNote.setNote(encounter);

                    String uuid = null;
                    ClinicalNotes.ParticipatingProviders[] participatingProviders = cNotes[i].getParticipatingProvidersArray();
                    ClinicalNotes.NoteReviewer[] noteReviewers = cNotes[i].getNoteReviewerArray();

                    int p_total = participatingProviders.length>noteReviewers.length ? participatingProviders.length : noteReviewers.length;
                    for (int p=0; p<p_total; p++) {
                        if (p>0) {
                            cmNote = prepareCMNote("1",uuid);
                            if (eventDateTime==null) cmNote.setObservation_date(new Date());
                            else cmNote.setObservation_date(dateTimeFPtoDate(eventDateTime, timeShiftInDays));
                            if (StringUtils.filled(encounter)) cmNote.setNote(encounter);
                        }

                        //participating providers
                        if (p<participatingProviders.length) {
                            if (participatingProviders[p].getDateTimeNoteCreated()==null) cmNote.setUpdate_date(new Date());
                            else cmNote.setUpdate_date(dateTimeFPtoDate(participatingProviders[p].getDateTimeNoteCreated(), timeShiftInDays));

                            if (participatingProviders[p].getName()!=null) {
                                HashMap<String,String> authorName = getPersonName(participatingProviders[p].getName());
                                String authorOHIP = participatingProviders[p].getOHIPPhysicianId();
                                String authorProvider = writeProviderData(authorName.get("firstname"), authorName.get("lastname"), authorOHIP);
                                if (StringUtils.empty(authorProvider)) {
                                    authorProvider = defaultProvider;
                                    err_note.add("Clinical notes have no author; assigned to \"doctor oscardoc\" ("+(i+1)+")");
                                }
                                cmNote.setProviderNo(authorProvider);
                            }
                            if (participatingProviders[p].getDateTimeNoteCreated()!=null) {
                                cmNote.setCreate_date(dateTimeFPtoDate(participatingProviders[p].getDateTimeNoteCreated(), timeShiftInDays));
                            }
                        }

                        //note reviewers
                        if (p<noteReviewers.length) {
                            if (noteReviewers[p].getName()!=null) {
                                HashMap<String,String> authorName = getPersonName(noteReviewers[p].getName());
                                String reviewerOHIP = noteReviewers[p].getOHIPPhysicianId();
                                String reviewer = writeProviderData(authorName.get("firstname"), authorName.get("lastname"), reviewerOHIP);
                                cmNote.setSigning_provider_no(reviewer);
                            }
                            if (noteReviewers[p].getDateTimeNoteReviewed()!=null) {
                                Util.addLine(encounter, "Review Date: ", dateTimeFPtoString(noteReviewers[p].getDateTimeNoteReviewed(), timeShiftInDays));
                            }
                        }

                        caseManagementManager.saveNoteSimple(cmNote);

                        //prepare for extra notes
                        if (p==0) {
                            addOneEntry(CLINICALNOTE);
                            uuid = cmNote.getUuid();
                        }
                    }
                    if (p_total==0) caseManagementManager.saveNoteSimple(cmNote);
                }

                //ALLERGIES & ADVERSE REACTIONS
                AllergiesAndAdverseReactions[] aaReactArray = patientRec.getAllergiesAndAdverseReactionsArray();
                for (int i=0; i<aaReactArray.length; i++) {
                    String description="", regionalId="", reaction="", severity="", entryDate="", startDate="", typeCode="", lifeStage="";

                    reaction = StringUtils.noNull(aaReactArray[i].getReaction());
                    description = StringUtils.noNull(aaReactArray[i].getOffendingAgentDescription());
                    entryDate = dateTimeFPtoString(aaReactArray[i].getRecordedDate(), timeShiftInDays);
                    startDate = dateFPtoString(aaReactArray[i].getStartDate(), timeShiftInDays);
                    if (aaReactArray[i].getLifeStage()!=null) lifeStage = aaReactArray[i].getLifeStage().toString();

                    if (StringUtils.empty(entryDate)) entryDate = null;
                    if (StringUtils.empty(startDate)) startDate = null;

                    if (aaReactArray[i].getCode()!=null) regionalId = StringUtils.noNull(aaReactArray[i].getCode().getCodeValue());
                    reaction = Util.addLine(reaction,"Offending Agent Description: ",aaReactArray[i].getOffendingAgentDescription());
                    if (aaReactArray[i].getReactionType()!=null) reaction = Util.addLine(reaction,"Reaction Type: ",aaReactArray[i].getReactionType().toString());

                    if (typeCode.equals("") && aaReactArray[i].getPropertyOfOffendingAgent()!=null) {
                        if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.DR) typeCode="13"; //drug
                        else if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.ND) typeCode="0"; //non-drug
                    else if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.UK) typeCode="0"; //unknown
                    }
                    if (aaReactArray[i].getSeverity()!=null) {
                        if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.MI) severity="1"; //mild
                        else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.MO) severity="2"; //moderate
                        else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.LT) severity="3"; //severe
                        else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.NO) severity="4"; //unknown
                    }
                    Long allergyId = RxAllergyImport.save(demographicNo, entryDate, description, typeCode, reaction, startDate, severity, regionalId, lifeStage);
                    addOneEntry(ALLERGY);

                    //annotation
                    String note = StringUtils.noNull(aaReactArray[i].getNotes());
                    CaseManagementNote cmNote = prepareCMNote("2",null);
                    cmNote.setNote(note);
                    saveLinkNote(cmNote, CaseManagementNoteLink.ALLERGIES, allergyId);

                    //to dumpsite
                    String dump = "imported.cms4.2011.06";
                    String summary = aaReactArray[i].getCategorySummaryLine();
                    if (StringUtils.empty(summary)) {
                        err_summ.add("No Summary for Allergies & Adverse Reactions ("+(i+1)+")");
                    }
                    dump = Util.addLine(dump, summary);
                    dump = Util.addLine(dump, getResidual(aaReactArray[i].getResidualInfo()));

                    cmNote = prepareCMNote("2",null);
                    cmNote.setNote(note);
                    saveLinkNote(cmNote, CaseManagementNoteLink.ALLERGIES, allergyId);
                }


                //MEDICATIONS & TREATMENTS
                MedicationsAndTreatments[] medArray = patientRec.getMedicationsAndTreatmentsArray();
                for (int i=0; i<medArray.length; i++) {
                    Drug drug = new Drug();
                    //no need: DrugReason drugReason = new DrugReason();
                    drug.setCreateDate(new Date());
                    drug.setRxDate(dateFPtoDate(medArray[i].getStartDate(), timeShiftInDays));
                    drug.setWrittenDate(dateTimeFPtoDate(medArray[i].getPrescriptionWrittenDate(), timeShiftInDays));

                    if (medArray[i].getStartDate()==null) drug.setRxDate(new Date());
                    String duration = medArray[i].getDuration();
                    drug.setDuration(duration);
                    drug.setDurUnit("D");
                    if (duration==null || !NumberUtils.isDigits(duration)) duration = "0";

                    Calendar endDate = Calendar.getInstance();
                    endDate.setTime(drug.getRxDate());
                    endDate.add(Calendar.DAY_OF_YEAR, Integer.valueOf(duration)+timeShiftInDays);
                    drug.setEndDate(endDate.getTime());
                    drug.setRegionalIdentifier(medArray[i].getDrugIdentificationNumber());
                    drug.setQuantity(medArray[i].getQuantity());
                    drug.setFreqCode(medArray[i].getFrequency());
                    if (medArray[i].getFrequency()!=null && medArray[i].getFrequency().contains("PRN")) drug.setPrn(true);
                    else drug.setPrn(false);

                    drug.setRoute(medArray[i].getRoute());
                    drug.setDrugForm(medArray[i].getForm());
                    drug.setLongTerm(getYN(medArray[i].getLongTermMedication()).equals("Yes"));
                    drug.setPastMed(getYN(medArray[i].getPastMedications()).equals("Yes"));
                    drug.setPatientCompliance(getYN(medArray[i].getPatientCompliance()));

                    if (NumberUtils.isDigits(medArray[i].getNumberOfRefills())) drug.setRepeat(Integer.valueOf(medArray[i].getNumberOfRefills()));
                    drug.setETreatmentType(medArray[i].getTreatmentType());
                    //no need: drug.setRxStatus(medArray[i].getPrescriptionStatus());

                    //no need: String nosub = medArray[i].getSubstitutionNotAllowed();
                    //no need: if (nosub!=null) drug.setNoSubs(nosub.equalsIgnoreCase("Y"));

                    //no need: String non_auth = medArray[i].getNonAuthoritativeIndicator();
                    //no need: if (non_auth!=null) drug.setNonAuthoritative(non_auth.equalsIgnoreCase("Y"));
                    //no need: else  err_data.add("Error! No non-authoritative indicator for Medications & Treatments ("+(i+1)+")");

                    //no need: if (NumberUtils.isDigits(medArray[i].getDispenseInterval())) drug.setDispenseInterval(Integer.parseInt(medArray[i].getDispenseInterval()));
                    //no need: else err_data.add("Error! Invalid Dispense Interval for Medications & Treatments ("+(i+1)+")");

                    if (NumberUtils.isDigits(medArray[i].getRefillDuration())) drug.setRefillDuration(Integer.parseInt(medArray[i].getRefillDuration()));
                    else err_data.add("Error! Invalid Refill Duration for Medications & Treatments ("+(i+1)+")");

                    if (NumberUtils.isDigits(medArray[i].getRefillQuantity())) drug.setRefillQuantity(Integer.parseInt(medArray[i].getRefillQuantity()));
                    else err_data.add("Error! Invalid Refill Quantity for Medications & Treatments ("+(i+1)+")");

                    String take = StringUtils.noNull(medArray[i].getDosage()).trim();
                    drug.setTakeMin(Util.leadingNum(take));
                    int sep = take.indexOf("-");
                    if (sep>0) drug.setTakeMax(Util.leadingNum(take.substring(sep+1)));
                    else drug.setTakeMax(drug.getTakeMin());
                    drug.setUnit(medArray[i].getDosageUnitOfMeasure());
                    drug.setDemographicId(Integer.valueOf(demographicNo));
                    drug.setArchived(false);

                    drug.setBrandName(medArray[i].getDrugName());
                    drug.setCustomName(medArray[i].getDrugDescription());

                    String special = StringUtils.noNull(drug.getBrandName());
                    if (special.equals("")) {
                        special = StringUtils.noNull(drug.getCustomName());
                        drug.setCustomInstructions(true);
                    }

                    cdsDt.DrugMeasure strength = medArray[i].getStrength();
                    if (strength!=null) {
                        String dosage = StringUtils.noNull(strength.getAmount())+" "+StringUtils.noNull(strength.getUnitOfMeasure());
                        drug.setDosage(dosage);
                    }

                    special = Util.addLine(special, "Take ", StringUtils.noNull(medArray[i].getDosage()));
                    special += StringUtils.noNull(drug.getRoute());
                    special += StringUtils.noNull(drug.getFreqCode());
                    special += StringUtils.noNull(drug.getDuration())+" days";

                    //no need: special = Util.addLine(special, medArray[i].getPrescriptionInstructions());
                    //no need: special = Util.addLine(special, "Prescription Status: ", medArray[i].getPrescriptionStatus());
                    //no need: special = Util.addLine(special, "Dispense Interval: ", medArray[i].getDispenseInterval());
                    //no need: special = Util.addLine(special, "Protocol Id: ", medArray[i].getProtocolIdentifier());
                    //no need: special = Util.addLine(special, "Prescription Id: ", medArray[i].getPrescriptionIdentifier());
                    //no need: special = Util.addLine(special, "Prior Prescription Id: ", medArray[i].getPriorPrescriptionReferenceIdentifier());
                    drug.setSpecial(special);

                    if (medArray[i].getPrescribedBy()!=null) {
                        HashMap<String,String> personName = getPersonName(medArray[i].getPrescribedBy().getName());
                        String personOHIP = medArray[i].getPrescribedBy().getOHIPPhysicianId();
                        ProviderData pd = getProviderByOhip(personOHIP);
                        if (pd!=null && Integer.valueOf(pd.getProviderNo())>-1000) drug.setProviderNo(pd.getProviderNo());
                        else { //outside provider
                            drug.setOutsideProviderName(StringUtils.noNull(personName.get("lastname"))+", "+StringUtils.noNull(personName.get("firstname")));
                            drug.setOutsideProviderOhip(personOHIP);
                            drug.setProviderNo(writeProviderData(personName.get("firstname"), personName.get("lastname"), personOHIP));
                        }
                    } else {
                        drug.setProviderNo(admProviderNo);
                    }
                    drug.setPosition(0);
                    drugDao.persist(drug);
                    addOneEntry(MEDICATION);

                    /* no need:
                    if (medArray[i].getProblemCode()!=null) {
                        drugReason.setCode(medArray[i].getProblemCode());
                        drugReason.setDemographicNo(Integer.valueOf(demographicNo));
                        drugReason.setDrugId(drug.getId());
                        drugReason.setProviderNo(drug.getProviderNo());
                        drugReason.setPrimaryReasonFlag(true);
                        drugReason.setArchivedFlag(false);
                        drugReasonDao.persist(drugReason);
                    }
                     *
                     */

                    //annotation
                    CaseManagementNote cmNote = prepareCMNote("2",null);
                    String note = StringUtils.noNull(medArray[i].getNotes());
                    cmNote.setNote(note);
                    saveLinkNote(cmNote, CaseManagementNoteLink.DRUGS, (long)drug.getId());

                    //to dumpsite
                    String dump = "imported.cms4.2011.06";
                    String summary = medArray[i].getCategorySummaryLine();
                    if (StringUtils.empty(summary)) {
                        err_summ.add("No Summary for Medications & Treatments ("+(i+1)+")");
                    }
                    dump = Util.addLine(dump, summary);
                    dump = Util.addLine(dump, getResidual(medArray[i].getResidualInfo()));

                    cmNote = prepareCMNote("2",null);
                    cmNote.setNote(dump);
                    saveLinkNote(cmNote, CaseManagementNoteLink.DRUGS, (long)drug.getId());
                }


                //IMMUNIZATIONS
                Immunizations[] immuArray = patientRec.getImmunizationsArray();
                if (immuArray.length>0) {
                    err_note.add("All Immunization info assigned to doctor oscardoc");
                }
                for (int i=0; i<immuArray.length; i++) {
                    String preventionDate="", preventionType="", refused="0", comments="";
                    ArrayList<Map<String,String>> preventionExt = new ArrayList<Map<String,String>>();

                    if (StringUtils.filled(immuArray[i].getImmunizationName())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("name", immuArray[i].getImmunizationName());
                        preventionExt.add(ht);

                        preventionType = mapPreventionType(immuArray[i].getImmunizationCode());
                        if (preventionType.equals("")) {
                            preventionType = "OtherA";
                            err_note.add("Immunization "+immuArray[i].getImmunizationName()+" mapped to Other Layout A");
                        }
                    } else {
                        err_data.add("Error! No Immunization Name ("+(i+1)+")");
                    }

                    if (StringUtils.filled(immuArray[i].getManufacturer())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("manufacture", immuArray[i].getManufacturer());
                        preventionExt.add(ht);
                    }
                    if (StringUtils.filled(immuArray[i].getLotNumber())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("lot", immuArray[i].getLotNumber());
                        preventionExt.add(ht);
                    }
                    if (StringUtils.filled(immuArray[i].getRoute())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("route", immuArray[i].getRoute());
                        preventionExt.add(ht);
                    }
                    if (StringUtils.filled(immuArray[i].getSite())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("location", immuArray[i].getSite());
                        preventionExt.add(ht);
                    }
                    if (StringUtils.filled(immuArray[i].getDose())) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("dose", immuArray[i].getDose());
                        preventionExt.add(ht);
                    }
                    
                    if (StringUtils.filled(immuArray[i].getNotes())) {
                        comments = Util.addLine(comments, immuArray[i].getNotes());
                    }

                    if (immuArray[i].getImmunizationType()!=null) {
                        comments = Util.addLine(comments, "Immunization Type: ", immuArray[i].getImmunizationType().toString());;;
                    }

                    preventionDate = dateTimeFPtoString(immuArray[i].getDate(), timeShiftInDays);
                    refused = getYN(immuArray[i].getRefusedFlag()).equals("Yes") ? "1" : "0";
                    if (immuArray[i].getRefusedFlag()==null) err_data.add("Error! No Refused Flag for Immunizations ("+(i+1)+")");

/*
                    String iSummary="";
                    if (immuArray[i].getCategorySummaryLine()!=null) {
                        iSummary = immuArray[i].getCategorySummaryLine().trim();
                    } else {
                        err_summ.add("No Summary for Immunizations ("+(i+1)+")");
                    }


//                    if (StringUtils.filled(iSummary)) {
//                        comments = Util.addLine(comments, "Summary: ", iSummary);
//                        err_note.add("Immunization Summary imported in [comments] ("+(i+1)+")");
 *
 */
//                    }
                    comments = Util.addLine(comments, getCode(immuArray[i].getImmunizationCode(),"Immunization Code"));
                    comments = Util.addLine(comments, "Instructions: ", immuArray[i].getInstructions());
                    comments = Util.addLine(comments, getResidual(immuArray[i].getResidualInfo()));
                    if (StringUtils.filled(comments)) {
                        Map<String,String> ht = new HashMap<String,String>();
                        ht.put("comments", comments);
                        preventionExt.add(ht);
                    }
                    PreventionData.insertPreventionData(admProviderNo, demographicNo, preventionDate, defaultProvider, "", preventionType, refused, "", "", preventionExt);
                    addOneEntry(IMMUNIZATION);
                }

                //LABORATORY RESULTS
                LaboratoryResults[] labResultArr = patientRec.getLaboratoryResultsArray();
                String[] _accession = new String[labResultArr.length];
                String[] _coll_date = new String[labResultArr.length];
                String[] _title	    = new String[labResultArr.length];
                String[] _testName  = new String[labResultArr.length];
                String[] _abn	    = new String[labResultArr.length];
                String[] _minimum   = new String[labResultArr.length];
                String[] _maximum   = new String[labResultArr.length];
                String[] _result    = new String[labResultArr.length];
                String[] _unit	    = new String[labResultArr.length];
                String[] _labnotes  = new String[labResultArr.length];
                String[] _location  = new String[labResultArr.length];
                String[] _reviewer  = new String[labResultArr.length];
                String[] _lab_no    = new String[labResultArr.length];
                String[] _rev_date  = new String[labResultArr.length];
                String[] _req_date  = new String[labResultArr.length];

                // Save to labPatientPhysicianInfo, labTestResults, patientLabRouting
                for (int i=0; i<labResultArr.length; i++) {
                    _testName[i] = StringUtils.noNull(labResultArr[i].getLabTestCode());
                    _location[i] = StringUtils.noNull(labResultArr[i].getLaboratoryName());
                    _accession[i] = StringUtils.noNull(labResultArr[i].getAccessionNumber());
                    _coll_date[i] = dateOnly(dateTimeFPtoString(labResultArr[i].getCollectionDateTime(), timeShiftInDays));
                    _req_date[i] = dateTimeFPtoString(labResultArr[i].getLabRequisitionDateTime(), timeShiftInDays);
                    if (StringUtils.empty(_req_date[i])) _req_date[i] = _coll_date[i];

                    _title[i] = StringUtils.noNull(labResultArr[i].getTestName());
                    if (StringUtils.filled(labResultArr[i].getTestNameReportedByLab())) {
                        _title[i] += StringUtils.filled(_title[i]) ? "/" : "";
                        _title[i] += labResultArr[i].getTestNameReportedByLab();
                    }
                    if (StringUtils.filled(labResultArr[i].getNotesFromLab()))
                        _labnotes[i] = "Notes: "+labResultArr[i].getNotesFromLab();

                    if (labResultArr[i].getResultNormalAbnormalFlag()!=null) {
                        cdsDt.ResultNormalAbnormalFlag.Enum flag = labResultArr[i].getResultNormalAbnormalFlag();
                        if (flag.equals(cdsDt.ResultNormalAbnormalFlag.Y)) _abn[i] = "A";
                        if (flag.equals(cdsDt.ResultNormalAbnormalFlag.N)) _abn[i] = "N";
                    }

                    if (labResultArr[i].getResult()!=null) {
                        _result[i] = StringUtils.noNull(labResultArr[i].getResult().getValue());
                        _unit[i] = StringUtils.noNull(labResultArr[i].getResult().getUnitOfMeasure());
                    }

                    if (labResultArr[i].getReferenceRange()!=null) {
                        LaboratoryResults.ReferenceRange ref = labResultArr[i].getReferenceRange();
                        if (StringUtils.filled(ref.getReferenceRangeText())) {
                            _minimum[i] = ref.getReferenceRangeText();
                        } else {
                            _maximum[i] = StringUtils.noNull(ref.getHighLimit());
                            _minimum[i] = StringUtils.noNull(ref.getLowLimit());
                        }
                    }

                    LaboratoryResults.ResultReviewer[] resultReviewers = labResultArr[i].getResultReviewerArray();
                    if (resultReviewers.length>0) {
                        HashMap<String,String> revName = getPersonName(resultReviewers[0].getName());
                        String revOhip = StringUtils.noNull(resultReviewers[0].getOHIPPhysicianId());
                        _reviewer[i] = writeProviderData(revName.get("firstname"), revName.get("lastname"), revOhip);
                        _rev_date[i] = dateTimeFPtoString(resultReviewers[0].getDateTimeResultReviewed(), timeShiftInDays);
                    }
                }

                ArrayList<String> uniq_labs = getUniques(_location);
                ArrayList<String> uniq_accs = getUniques(_accession);
                for (String lab : uniq_labs) {
                    boolean labNew = true;
                    String rptInfoId="";
                    for (String acc : uniq_accs) {
                        boolean accNew = true;
                        String paPhysId="";
                        for (int i=0; i<_location.length; i++) {
                            if (!(_location[i].equals(lab) && _accession[i].equals(acc))) continue;

                            if (labNew) {
                                rptInfoId = LabResultImport.saveLabReportInfo(_location[i]);
                                labNew = false;
                            }
                            if (accNew) {
                                paPhysId = LabResultImport.saveLabPatientPhysicianInfo(rptInfoId, _accession[i], _coll_date[i], firstName, lastName, sex, hin, birthDate, patientPhone);

                                LabResultImport.savePatientLabRouting(demographicNo, paPhysId);
                                LabRequestReportLink.save(null,null,_req_date[i],"labPatientPhysicianInfo",Long.valueOf(paPhysId));

                                String status = StringUtils.filled(_reviewer[i]) ? "A" : "N";
                                _reviewer[i] = status.equals("A") ? _reviewer[i] : "0";
                                LabResultImport.saveProviderLabRouting(_reviewer[i], paPhysId, status, "", _rev_date[i]);

                                accNew = false;
                            }
                            LabResultImport.saveLabTestResults(_title[i], _testName[i], _abn[i], _minimum[i], _maximum[i], _result[i], _unit[i], "", _location[i], paPhysId, "C", "N");
                            LabResultImport.saveLabTestResults(_title[i], _testName[i], "", "", "", "", "", _labnotes[i], _location[i], paPhysId, "D", "Y");
                            addOneEntry(LABS);

                            _lab_no[i] = paPhysId;
                        }
                    }
                }
                /*
                String labEverything = getLabDline(labResultArr[i]);
                if (StringUtils.filled(labEverything)){
                    LabResultImport.SaveLabDesc(labEverything,patiPhysId);
                }
                */

                // Save to measurements, measurementsExt
                for (LaboratoryResults labResults : labResultArr) {
                    Measurements meas = new Measurements(Long.valueOf(demographicNo), admProviderNo);
                    LaboratoryResults.Result result = labResults.getResult();

                    //save lab result & get unit
                    String unit = null;
                    if (result!=null) {
                        meas.setDataField(StringUtils.noNull(result.getValue()));
                        unit = StringUtils.noNull(result.getUnitOfMeasure());
                    } else {
                        meas.setDataField("");
                    }

                    //save lab result unit
                    meas.setDateEntered(new Date());
                    ImportExportMeasurements.saveMeasurements(meas);
                    Long measId = meas.getId();
                    saveMeasurementsExt(measId, "unit", unit);

                    //save lab tes code, test name
                    String testCode = StringUtils.filled(labResults.getLabTestCode()) ? labResults.getLabTestCode() : "";
                    String testName = StringUtils.noNull(labResults.getTestName());
                    if (StringUtils.filled(labResults.getTestNameReportedByLab())) {
                        testName += StringUtils.filled(testName) ? "/" : "";
                        testName += labResults.getTestNameReportedByLab();
                    }
                    saveMeasurementsExt(measId, "identifier", testCode);
                    saveMeasurementsExt(measId, "name", testName);

                    //save lab collection datetime
                    cdsDt.DateTimeFullOrPartial collDate = labResults.getCollectionDateTime();
                    if (collDate!=null) {
                        saveMeasurementsExt(measId, "datetime", dateTimeFPtoString(collDate, timeShiftInDays));
                    } else {
                        err_data.add("Error! No Collection DateTime for Lab Test "+testCode+" for Patient "+demographicNo);
                    }
                    //save laboratory name
                    String labname = StringUtils.noNull(labResults.getLaboratoryName());
                    if (StringUtils.filled(labname)) {
                        saveMeasurementsExt(measId, "labname", labname);
                    } else {
                        err_data.add("Error! No Laboratory Name for Lab Test "+testCode+" for Patient "+demographicNo);
                    }

                    //save lab normal/abnormal flag
                    cdsDt.ResultNormalAbnormalFlag.Enum abnFlag = labResults.getResultNormalAbnormalFlag();
                    if (abnFlag!=null) {
                        String abn = abnFlag.toString();
                        if (!abn.equals("U")) {
                            saveMeasurementsExt(measId, "abnormal", (abn.equals("Y")?"A":abn));
                        }
                    }

                    //save lab accession number
                    String accnum = labResults.getAccessionNumber();
                    saveMeasurementsExt(measId, "accession", accnum);

                    //save lab reference range
                    LaboratoryResults.ReferenceRange refRange = labResults.getReferenceRange();
                    if (refRange!=null) {
                        if (StringUtils.filled(refRange.getReferenceRangeText())) {
                            saveMeasurementsExt(measId, "range", refRange.getReferenceRangeText());
                        } else {
                            saveMeasurementsExt(measId, "maximum", refRange.getHighLimit());
                            saveMeasurementsExt(measId, "minimum", refRange.getLowLimit());
                        }
                    }

                    //save OLIS test result status
                    String olis_status = labResults.getOLISTestResultStatus();
                    if (StringUtils.filled(olis_status)) saveMeasurementsExt(measId, "olis_status", olis_status);

                    //save notes from lab
                    String labNotes = labResults.getNotesFromLab();
                    if (StringUtils.filled(labNotes)) saveMeasurementsExt(measId, "comments", labNotes);

                    //retrieve lab_no
                    String lab_no = null;
                    for (int i=0; i<labResultArr.length; i++) {
                        if (!(_location[i].equals(labname) && _accession[i].equals(accnum))) continue;
                        saveMeasurementsExt(measId, "lab_no", _lab_no[i]);
                        lab_no = _lab_no[i];
                        break;
                    }

                    if (lab_no!=null) {

                        //save lab physician notes (annotation)
                        String annotation = labResults.getPhysiciansNotes();
                        if (StringUtils.filled(annotation)) {
                            saveMeasurementsExt(measId, "other_id", "0-0");
                            CaseManagementNote cmNote = prepareCMNote("2",null);
                            cmNote.setNote(annotation);
                            saveLinkNote(cmNote, CaseManagementNoteLink.LABTEST, Long.valueOf(lab_no), "0-0");
                        }

                        //to dumpsite
                        String testResultsInfo = labResults.getTestResultsInformationReportedByTheLab();
                        if (StringUtils.filled(testResultsInfo)) {
                            String dump = Util.addLine("imported.cms4.2011.06", "Test Results Info: ", testResultsInfo);
                            CaseManagementNote cmNote = prepareCMNote("2",null);
                            cmNote.setNote(dump);
                            saveLinkNote(cmNote, CaseManagementNoteLink.LABTEST, Long.valueOf(lab_no));
                        }
                    } else {
                        logger.error("No lab no! (demo="+demographicNo+")");
                    }
                }

                //APPOINTMENTS
                Appointments[] appArray = patientRec.getAppointmentsArray();
                Date appointmentDate = null;
                String name="", notes="", reason="", status="", startTime="", endTime="", apptProvider="";

                Properties p = (Properties) request.getSession().getAttribute("oscarVariables");

                for (int i=0; i<appArray.length; i++) {
                    String apptDateStr = dateFPtoString(appArray[i].getAppointmentDate(), timeShiftInDays);
                    if (StringUtils.filled(apptDateStr)) {
                        appointmentDate = UtilDateUtilities.StringToDate(apptDateStr);
                    } else {
                        err_data.add("Error! No Appointment Date ("+(i+1)+")");
                    }
                    if (appArray[i].getAppointmentTime()!=null) {
                        startTime = getCalTime(appArray[i].getAppointmentTime());
                        if (appArray[i].getDuration()!=null) {
                            Date d_startTime = appArray[i].getAppointmentTime().getTime();
                            Date d_endTime = new Date();
                            d_endTime.setTime(d_startTime.getTime() + (appArray[i].getDuration().longValue()-1)*60000);
                            endTime = UtilDateUtilities.DateToString(d_endTime,"HH:mm:ss");
                        } else {
                            Date d_startTime = appArray[i].getAppointmentTime().getTime();
                            Date d_endTime = new Date();
                            d_endTime.setTime(d_startTime.getTime() + 14*60000);
                            endTime = UtilDateUtilities.DateToString(d_endTime,"HH:mm:ss");
                        }
                    } else {
                        err_data.add("Error! No Appointment Time ("+(i+1)+")");
                    }
                    if (StringUtils.filled(appArray[i].getAppointmentNotes())) {
                        notes = appArray[i].getAppointmentNotes();
                    }
                    if (appArray[i].getAppointmentStatus()!=null) {
                        ApptStatusData asd = new ApptStatusData();
                        String[] allStatus = asd.getAllStatus();
                        String[] allTitle = asd.getAllTitle();
                        status = allStatus[0];
                        for (int j=1; j<allStatus.length; j++) {
                            String msg = getResources(request).getMessage(allTitle[j]);
                            if (appArray[i].getAppointmentStatus().trim().equalsIgnoreCase(msg)) {
                                status = allStatus[j];
                                break;
                            }
                        }
                    }

                    reason = StringUtils.noNull(appArray[i].getAppointmentPurpose());
                    if (appArray[i].getProvider()!=null) {
                        HashMap<String,String> providerName = getPersonName(appArray[i].getProvider().getName());
                        String personOHIP = appArray[i].getProvider().getOHIPPhysicianId();
                        apptProvider = writeProviderData(providerName.get("firstname"), providerName.get("lastname"), personOHIP);
                        if (StringUtils.empty(apptProvider)) {
                            apptProvider = defaultProvider;
                            err_note.add("Appointment has no provider; assigned to \"doctor oscardoc\" ("+(i+1)+")");
                        }
                    }
                    oscarSuperManager.update("appointmentDao", "import_appt", new Object [] {apptProvider,
                    appointmentDate, startTime, endTime, patientName, demographicNo, notes, reason, status});
                    addOneEntry(APPOINTMENT);
                }

                //REPORTS RECEIVED

                HRMDocumentDao hrmDocDao = (HRMDocumentDao) SpringUtils.getBean("HRMDocumentDao");
                HRMDocumentCommentDao hrmDocCommentDao = (HRMDocumentCommentDao) SpringUtils.getBean("HRMDocumentCommentDao");
                HRMDocumentSubClassDao hrmDocSubClassDao = (HRMDocumentSubClassDao) SpringUtils.getBean("HRMDocumentSubClassDao");
                HRMDocumentToDemographicDao hrmDocToDemoDao = (HRMDocumentToDemographicDao) SpringUtils.getBean("HRMDocumentToDemographicDao");

                ReportsReceived[] repR = patientRec.getReportsReceivedArray();
                List<ReportsReceived> HRMreports = new ArrayList<ReportsReceived>();
                String HRMfile = docDir + "HRM_"+UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss");
                for (int i=0; i<repR.length; i++) {

                    if (repR[i].getHRMResultStatus()!=null || repR[i].getOBRContentArray().length>0) { //HRM reports
                        HRMDocument hrmDoc = new HRMDocument();
                        HRMDocumentComment hrmDocComment = new HRMDocumentComment();
                        HRMDocumentToDemographic hrmDocToDemo = new HRMDocumentToDemographic();

                        hrmDoc.setReportFile(HRMfile);
                        if (repR[i].getSourceFacility()!=null) hrmDoc.setSourceFacility(repR[i].getSourceFacility());
                        if (repR[i].getReceivedDateTime()!=null) {
                            hrmDoc.setTimeReceived(dateTimeFPtoDate(repR[i].getReceivedDateTime(), timeShiftInDays));
                        } else {
                            hrmDoc.setTimeReceived(new Date());
                        }
                        if (repR[i].getHRMResultStatus()!=null) hrmDoc.setReportStatus(repR[i].getHRMResultStatus());
                        if (repR[i].getClass1()!=null) hrmDoc.setReportType(repR[i].getClass1().toString());
                        if (repR[i].getEventDateTime()!=null) hrmDoc.setReportDate(dateTimeFPtoDate(repR[i].getEventDateTime(), timeShiftInDays));
                        hrmDocDao.persist(hrmDoc);

                        if (repR[i].getNotes()!=null) {
                            hrmDocComment.setHrmDocumentId(hrmDoc.getId());
                            hrmDocComment.setComment(repR[i].getNotes());
                            hrmDocCommentDao.persist(hrmDocComment);
                        }

                        hrmDocToDemo.setDemographicNo(demographicNo);
                        hrmDocToDemo.setHrmDocumentId(hrmDoc.getId().toString());
                        hrmDocToDemoDao.persist(hrmDocToDemo);

                        ReportsReceived.OBRContent[] obr = repR[i].getOBRContentArray();
                        for (int j=0; j<obr.length; j++) {
                            HRMDocumentSubClass hrmDocSc = new HRMDocumentSubClass();
                            if (obr[j].getAccompanyingSubClass()!=null) hrmDocSc.setSubClass(obr[j].getAccompanyingSubClass());
                            if (obr[j].getAccompanyingDescription()!=null) hrmDocSc.setSubClassDescription(obr[j].getAccompanyingDescription());
                            if (obr[j].getAccompanyingMnemonic()!=null) hrmDocSc.setSubClassMnemonic(obr[j].getAccompanyingMnemonic());
                            if (obr[j].getObservationDateTime()!=null) hrmDocSc.setSubClassDateTime(dateTimeFPtoDate(obr[j].getObservationDateTime(), timeShiftInDays));
                            hrmDocSc.setHrmDocumentId(hrmDoc.getId());
                            hrmDocSc.setActive(true);
                            hrmDocSubClassDao.persist(hrmDocSc);
                        }
                        HRMreports.add(repR[i]);

                    } else { //non-HRM reports
                        boolean binaryFormat = false;
                        if (repR[i].getFormat()!=null) {
                            if (repR[i].getFormat().equals(cdsDt.ReportFormat.BINARY)) binaryFormat = true;
                        } else {
                            err_data.add("Error! No Report Format for Report ("+(i+1)+")");
                        }
                        cdsDt.ReportContent repCt = repR[i].getContent();
                        if (repCt!=null) {
                            byte[] b = null;
                            if (repCt.getMedia()!=null) b = repCt.getMedia();
                            else if (repCt.getTextContent()!=null) b = repCt.getTextContent().getBytes();
                            if (b==null) {
                                err_othe.add("Error! No report file in xml ("+(i+1)+")");
                            } else {
                                String docFileName = "ImportReport"+(i+1)+"-"+UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss");
                                String docClass=null, docSubClass=null, contentType="", observationDate=null, updateDateTime=null, docCreator=admProviderNo;
                                String reviewer=null, reviewDateTime=null, source=null;

                                if (StringUtils.filled(repR[i].getFileExtensionAndVersion())) {
                                    contentType = repR[i].getFileExtensionAndVersion();
                                    docFileName += Util.mimeToExt(contentType);
                                } else {
                                    if (binaryFormat) err_data.add("Error! No File Extension for Report ("+(i+1)+")");
                                }
                                String docDesc = repR[i].getSubClass();
                                if (StringUtils.empty(docDesc)) docDesc = "ImportReport"+(i+1);
                                FileOutputStream f = new FileOutputStream(docDir + docFileName);
                                f.write(b);
                                f.close();

                                if (repR[i].getClass1()!=null) {
                                    docClass = repR[i].getClass1().toString();
                                } else {
                                    err_data.add("Error! No Class Type for Report ("+(i+1)+")");
                                }
                                if (repR[i].getSubClass()!=null) {
                                    docSubClass = repR[i].getSubClass();
                                }

                                ReportsReceived.SourceAuthorPhysician authorPhysician = repR[i].getSourceAuthorPhysician();
                                if (authorPhysician!=null) {
                                    if (authorPhysician.getAuthorName()!=null) {
                                        HashMap<String,String> author = getPersonName(authorPhysician.getAuthorName());
                                        source = StringUtils.noNull(author.get("firstname")) + " " + StringUtils.noNull(author.get("lastname"));
                                    } else if (authorPhysician.getAuthorFreeText()!=null) {
                                        source = authorPhysician.getAuthorFreeText();
                                    }
                                }

                                ReportsReceived.ReportReviewed[] reportReviewed = repR[i].getReportReviewedArray();
                                if (reportReviewed.length>0) {
                                    HashMap<String,String> reviewerName = getPersonName(reportReviewed[0].getName());
                                    reviewer = writeProviderData(reviewerName.get("firstname"), reviewerName.get("lastname"), reportReviewed[0].getReviewingOHIPPhysicianId());
                                    reviewDateTime = dateFPtoString(reportReviewed[0].getDateTimeReportReviewed(), timeShiftInDays);
                                }

                                observationDate = dateTimeFPtoString(repR[i].getEventDateTime(), timeShiftInDays);
                                updateDateTime = dateTimeFPtoString(repR[i].getReceivedDateTime(), timeShiftInDays);

                                EDocUtil.addDocument(demographicNo,docFileName,docDesc,"",docClass,docSubClass,contentType,observationDate,updateDateTime,docCreator,admProviderNo,reviewer,reviewDateTime, source);
                                if (binaryFormat) addOneEntry(REPORTBINARY);
                                else addOneEntry(REPORTTEXT);
                            }
                        }
                    }
                }
                CreateHRMFile.create(demo, HRMreports, HRMfile);

                //CARE ELEMENTS
                CareElements[] careElems = patientRec.getCareElementsArray();
                for (int i=0; i<careElems.length; i++) {
                    CareElements ce = careElems[i];
                    cdsDt.Height[] heights = ce.getHeightArray();
                    for (cdsDt.Height ht : heights) {
                        Date dateObserved = ht.getDate()!=null ? ht.getDate().getTime() : null;
                        String dataField = StringUtils.noNull(ht.getHeight());
                        String dataUnit = ht.getHeightUnit()!=null ? "in "+ht.getHeightUnit().toString() : "";

                        if (ht.getDate()==null) err_data.add("Error! No Date for Height in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(ht.getHeight())) err_data.add("Error! No value for Height in Care Element ("+(i+1)+")");
                        if (ht.getHeightUnit()==null) err_data.add("Error! No unit for Height in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("HT", demographicNo, admProviderNo, dataField, dataUnit, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.Weight[] weights = ce.getWeightArray();
                    for (cdsDt.Weight wt : weights) {
                        Date dateObserved = wt.getDate()!=null ? wt.getDate().getTime() : null;
                        String dataField = StringUtils.noNull(wt.getWeight());
                        String dataUnit = wt.getWeightUnit()!=null ? "in "+wt.getWeightUnit().toString() : "";

                        if (wt.getDate()==null) err_data.add("Error! No Date for Weight in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(wt.getWeight())) err_data.add("Error! No value for Weight in Care Element ("+(i+1)+")");
                        if (wt.getWeightUnit()==null) err_data.add("Error! No unit for Weight in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("WT", demographicNo, admProviderNo, dataField, dataUnit, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.WaistCircumference[] waists = ce.getWaistCircumferenceArray();
                    for (cdsDt.WaistCircumference wc : waists) {
                        Date dateObserved = wc.getDate()!=null ? wc.getDate().getTime() : null;
                        String dataField = StringUtils.noNull(wc.getWaistCircumference());
                        String dataUnit = wc.getWaistCircumferenceUnit()!=null ? "in "+wc.getWaistCircumferenceUnit().toString() : "";

                        if (wc.getDate()==null) err_data.add("Error! No Date for Waist Circumference in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(wc.getWaistCircumference())) err_data.add("Error! No value for Waist Circumference in Care Element ("+(i+1)+")");
                        if (wc.getWaistCircumferenceUnit()==null) err_data.add("Error! No unit for Waist Circumference in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("WC", demographicNo, admProviderNo, dataField, dataUnit, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.BloodPressure[] bloodp = ce.getBloodPressureArray();
                    for (cdsDt.BloodPressure bp : bloodp) {
                        Date dateObserved = bp.getDate()!=null ? bp.getDate().getTime() : null;
                        String dataField = StringUtils.noNull(bp.getSystolicBP())+"/"+StringUtils.noNull(bp.getDiastolicBP());
                        String dataUnit = bp.getBPUnit()!=null ? "in "+bp.getBPUnit().toString() : "";

                        if (bp.getDate()==null) err_data.add("Error! No Date for Blood Pressure in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(bp.getSystolicBP())) err_data.add("Error! No value for Systolic Blood Pressure in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(bp.getDiastolicBP())) err_data.add("Error! No value for Diastolic Blood Pressure in Care Element ("+(i+1)+")");
                        if (bp.getBPUnit()==null) err_data.add("Error! No unit for Blood Pressure in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("BP", demographicNo, admProviderNo, dataField, dataUnit, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.SmokingStatus[] smoks = ce.getSmokingStatusArray();
                    for (cdsDt.SmokingStatus ss : smoks) {
                        Date dateObserved = ss.getDate()!=null ? ss.getDate().getTime() : null;
                        String dataField = ss.getStatus()!=null ? ss.getStatus().toString() : "";

                        if (ss.getDate()==null) err_data.add("Error! No Date for Smoking Status in Care Element ("+(i+1)+")");
                        if (ss.getStatus()==null) err_data.add("Error! No value for Smoking Status in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("SKST", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.SmokingPacks[] smokp = ce.getSmokingPacksArray();
                    for (cdsDt.SmokingPacks sp : smokp) {
                        Date dateObserved = sp.getDate()!=null ? sp.getDate().getTime() : null;
                        String dataField = sp.getPerDay()!=null ? sp.getPerDay().toString() : "";

                        if (sp.getDate()==null) err_data.add("Error! No Date for Smoking Packs/Day in Care Element ("+(i+1)+")");
                        if (sp.getPerDay()==null) err_data.add("Error! No value for Smoking Packs/Day in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("POSK", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.SelfMonitoringBloodGlucose[] smbg = ce.getSelfMonitoringBloodGlucoseArray();
                    for (cdsDt.SelfMonitoringBloodGlucose sg : smbg) {
                        Date dateObserved = sg.getDate()!=null ? sg.getDate().getTime() : null;
                        String dataField = sg.getSelfMonitoring()!=null ? sg.getSelfMonitoring().toString() : "";

                        if (sg.getDate()==null) err_data.add("Error! No Date for Self-monitoring Blood Glucose in Care Element ("+(i+1)+")");
                        if (sg.getSelfMonitoring()==null) err_data.add("Error! No value for Self-monitoring Blood Glucose in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("SMBG", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.DiabetesEducationalSelfManagement[] desm = ce.getDiabetesEducationalSelfManagementArray();
                    for (cdsDt.DiabetesEducationalSelfManagement dm : desm) {
                        Date dateObserved = dm.getDate()!=null ? dm.getDate().getTime() : null;
                        String dataField = dm.getEducationalTrainingPerformed()!=null ? dm.getEducationalTrainingPerformed().toString() : null;

                        if (dm.getDate()==null) err_data.add("Error! No Date for Diabetes Educational/Self-management Training in Care Element ("+(i+1)+")");
                        if (dm.getEducationalTrainingPerformed()==null) err_data.add("Error! No value for Diabetes Educational/Self-management Training in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("DMME", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.DiabetesSelfManagementChallenges[] dsmc = ce.getDiabetesSelfManagementChallengesArray();
                    for (cdsDt.DiabetesSelfManagementChallenges dc : dsmc) {
                        Date dateObserved = dc.getDate().getTime();
                        String dataField = dc.getChallengesIdentified().toString();
                        String dataCode = dc.getCodeValue()!=null ? "LOINC="+dc.getCodeValue().toString() : "";

                        if (dc.getDate()==null) err_data.add("Error! No Date for Diabetes Self-management Challenges in Care Element ("+(i+1)+")");
                        if (dc.getChallengesIdentified()==null) err_data.add("Error! No Challenges Identified for Diabetes Self-management Challenges in Care Element ("+(i+1)+")");
                        if (dc.getCodeValue()==null) err_data.add("Error! No Code value for Diabetes Self-management Challenges in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("SMCD", demographicNo, admProviderNo, dataField, dataCode, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.DiabetesMotivationalCounselling[] dmc = ce.getDiabetesMotivationalCounsellingArray();
                    for (cdsDt.DiabetesMotivationalCounselling dc : dmc) {
                        Date dateObserved = dc.getDate()!=null ? dc.getDate().getTime() : null;
                        String dataField = "Yes";
                        if (dc.getCounsellingPerformed()!=null) {
                            cdsDt.DiabetesMotivationalCounselling.CounsellingPerformed cp = null;
                            if (dc.getCounsellingPerformed().equals(cp.NUTRITION)) {
                                ImportExportMeasurements.saveMeasurements("MCCN", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            }
                            else if (dc.getCounsellingPerformed().equals(cp.EXERCISE)) {
                                ImportExportMeasurements.saveMeasurements("MCCE", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            }
                            else if (dc.getCounsellingPerformed().equals(cp.SMOKING_CESSATION)) {
                                ImportExportMeasurements.saveMeasurements("MCCS", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            }
                            else if (dc.getCounsellingPerformed().equals(cp.OTHER)) {
                                ImportExportMeasurements.saveMeasurements("MCCO", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            }
                        }
                        if (dc.getDate()==null) err_data.add("Error! No Date for Diabetes Motivational Counselling in Care Element ("+(i+1)+")");
                        if (dc.getCounsellingPerformed()==null) err_data.add("Error! No Diabetes Motivational Counselling Performed value for Diabetes Self-management Challenges in Care Element ("+(i+1)+")");
                    }
                    cdsDt.DiabetesComplicationScreening[] dcs = ce.getDiabetesComplicationsScreeningArray();
                    for (cdsDt.DiabetesComplicationScreening ds : dcs) {
                        Date dateObserved = ds.getDate()!=null ? ds.getDate().getTime() : null;
                        String dataField = "Yes";
                        if (ds.getExamCode()!=null) {
                            cdsDt.DiabetesComplicationScreening.ExamCode ec = null;
                            if (ds.getExamCode().equals(ec.X_32468_1)) {
                                ImportExportMeasurements.saveMeasurements("EYEE", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            } else if (ds.getExamCode().equals(ec.X_11397_7)) {
                                ImportExportMeasurements.saveMeasurements("FTE", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            } else if (ds.getExamCode().equals(ec.NEUROLOGICAL_EXAM)) {
                                ImportExportMeasurements.saveMeasurements("FTLS", demographicNo, admProviderNo, dataField, dateObserved);
                                addOneEntry(CAREELEMENTS);
                            }
                        }
                        if (ds.getDate()==null) err_data.add("Error! No Date for Diabetes Complications Screening in Care Element ("+(i+1)+")");
                        if (ds.getExamCode()==null) err_data.add("Error! No Exam Code for Diabetes Complications Screening in Care Element ("+(i+1)+")");
                    }
                    cdsDt.DiabetesSelfManagementCollaborative[] dsco = ce.getDiabetesSelfManagementCollaborativeArray();
                    for (cdsDt.DiabetesSelfManagementCollaborative dc : dsco) {
                        Date dateObserved = dc.getDate()!=null ? dc.getDate().getTime() : null;
                        String dataField = StringUtils.noNull(dc.getDocumentedGoals());
                        String dataCode = dc.getCodeValue()!=null ? "LOINC="+dc.getCodeValue().toString() : "";

                        if (dc.getDate()==null) err_data.add("Error! No Date for Diabetes Self-management/Collaborative Goal Setting in Care Element ("+(i+1)+")");
                        if (StringUtils.empty(dc.getDocumentedGoals())) err_data.add("Error! No Documented Goal for Diabetes Self-management/Collaborative Goal Setting in Care Element ("+(i+1)+")");
                        if (dc.getCodeValue()==null) err_data.add("Error! No Code Value for Diabetes Self-management/Collaborative Goal Setting in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("CGSD", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                    cdsDt.HypoglycemicEpisodes[] hype = ce.getHypoglycemicEpisodesArray();
                    for (cdsDt.HypoglycemicEpisodes he : hype) {
                        Date dateObserved = he.getDate()!=null ? he.getDate().getTime() : null;
                        String dataField = he.getNumOfReportedEpisodes()!=null ? he.getNumOfReportedEpisodes().toString() : "";

                        if (he.getDate()==null) err_data.add("Error! No Date for Hypoglycemic Episodes in Care Element ("+(i+1)+")");
                        if (he.getNumOfReportedEpisodes()==null) err_data.add("Error! No Reported Episodes value for Hypoglycemic Episodes in Care Element ("+(i+1)+")");
                        ImportExportMeasurements.saveMeasurements("HYPE", demographicNo, admProviderNo, dataField, dateObserved);
                        addOneEntry(CAREELEMENTS);
                    }
                }
            }
            if(demoRes != null) {
                err_demo.addAll(demoRes.getWarningsCollection());
            }
            Util.cleanFile(xmlFile);

            return packMsgs(err_demo, err_data, err_summ, err_othe, err_note, warnings);
	}


	File makeImportLog(ArrayList demo, String dir) throws IOException {
		String[][] keyword = new String[2][16];
		keyword[0][0] = PATIENTID;
		keyword[1][0] = "ID";
                keyword[0][1] = " "+PERSONALHISTORY;
                keyword[1][1] = " History";
                keyword[0][2] = " "+FAMILYHISTORY;
                keyword[1][2] = " History";
                keyword[0][3] = " "+PASTHEALTH;
                keyword[1][3] = " Health";
                keyword[0][4] = " "+PROBLEMLIST;
                keyword[1][4] = " List";
                keyword[0][5] = " "+RISKFACTOR;
                keyword[1][5] = " Factor";
                keyword[0][6] = " "+ALLERGY;
                keyword[0][7] = " "+MEDICATION;
                keyword[0][8] = " "+IMMUNIZATION;
                keyword[0][9] = " "+LABS;
                keyword[0][10] = " "+APPOINTMENT;
                keyword[0][11] = " "+CLINICALNOTE;
                keyword[1][11] = " Note";
                keyword[0][12] = "    Report    ";
                keyword[1][12] = " "+REPORTTEXT;
                keyword[1][13] = " "+REPORTBINARY;
                keyword[0][14] = " "+CAREELEMENTS;
                keyword[1][14] = " Elements";
                keyword[0][15] = " "+ALERT;

                for (int i=0; i<keyword[0].length; i++) {
                    if (keyword[0][i].contains("Report")) {
                        keyword[0][i+1] = "Report2";
                        i++;
                        continue;
                    }
                    if (keyword[1][i]==null) keyword[1][i] = " ";
                    if (keyword[0][i].length()>keyword[1][i].length()) keyword[1][i] = fillUp(keyword[1][i], ' ', keyword[0][i].length());
                    if (keyword[0][i].length()<keyword[1][i].length()) keyword[0][i] = fillUp(keyword[0][i], ' ', keyword[1][i].length());
                }

		File importLog = new File(dir, "ImportEvent-"+UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss")+".log");
		BufferedWriter out = new BufferedWriter(new FileWriter(importLog));
                int tableWidth = 0;
                for (int i=0; i<keyword.length; i++) {
                    for (int j=0; j<keyword[i].length; j++) {
                        out.write(keyword[i][j]+" |");
                        if (keyword[i][j].trim().equals("Report")) j++;
                        if (i==1) tableWidth += keyword[i][j].length()+2;
                    }
                    out.newLine();
                }
                out.write(fillUp("",'-',tableWidth)); out.newLine();

                //general log data
                if (importNo==0) importNo = 1;
                for (int i=0; i<importNo; i++) {
                    for (int j=0; j<keyword[0].length; j++) {
                        String category = keyword[0][j].trim();
                        if (category.contains("Report")) category = keyword[1][j].trim();
                        Integer occurs = entries.get(category+i);
                        if (occurs==null) occurs = 0;
                        out.write(fillUp(occurs.toString(), ' ', keyword[1][j].length()));
                        out.write(" |");
                    }
                    out.newLine();
                    out.write(fillUp("",'-',tableWidth)); out.newLine();
                }
                out.newLine();
                out.newLine();
                out.newLine();

                //error log
                String column1 = "Patient ID";
                out.write(column1+" |");
                out.write("Errors/Notes");
                out.newLine();
                out.write(fillUp("",'-',tableWidth)); out.newLine();

                for (int i=0; i<importNo; i++) {
                    Integer id = entries.get(PATIENTID+i);
                    if (id==null) id = 0;
                    out.write(fillUp(id.toString(), ' ', column1.length()));
                    out.write(" |");
                    String[] info = (String[]) demo.get(i);
                    if (info!=null && info.length>0) {
                        String[] text = info[info.length-1].split("\n");
                        out.write(text[0]);
                        out.newLine();
                        for (int j=1; j<text.length; j++) {
                            out.write(fillUp("",' ',column1.length()));
                            out.write(" |");
                            out.write(text[j]);
                            out.newLine();
                        }
                    }
                    out.write(fillUp("",'-',tableWidth)); out.newLine();
                }
		out.close();
                importNo = 0;
                entries.clear();
		return importLog;
	}


	boolean matchFileExt(String filename, String ext) {
		if (StringUtils.empty(filename) || StringUtils.empty(ext)) return false;
		if (filename.length()<ext.length()+2) return false;
		if (filename.charAt(filename.length()-ext.length()-1)!='.') return false;

		if (filename.substring(filename.length()-ext.length()).equals(ext)) return true;
		else return false;
	}

	String fillUp(String filled, char c, int size) {
		if (size>=filled.length()) {
			int fill = size-filled.length();
			for (int i=0; i<fill; i++) filled += c;
		}
		return filled;
	}

	String getCalDate(Calendar c) {
		if (c==null) return "";
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		return f.format(c.getTime());
	}
        String getCalDate(Calendar c, int timeShiftInDays) {
                if (c==null) return "";

                c.add(Calendar.DAY_OF_YEAR, timeShiftInDays);
                return getCalDate(c);
        }
	String getCalDateTime(Calendar c) {
		if (c==null) return "";
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return f.format(c.getTime());
	}
	String getCalTime(Calendar c) {
		if (c==null) return "";
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		return f.format(c.getTime());
	}

	String getCountrySubDivCode(String countrySubDivCode) {
		if (StringUtils.empty(countrySubDivCode)) return "";
		String[] csdc = countrySubDivCode.split("-");
		if (csdc.length==2) {
			if (csdc[0].trim().equals("CA")) return csdc[1].trim(); //return w/o "CA-"
			if (csdc[1].trim().equals("US")) return countrySubDivCode.trim(); //return w/ "US-"
		}
		return "OT"; //Other
	}

    String dateTimeFPtoString(cdsDt.DateTimeFullOrPartial dtfp, int timeshiftInDays) {
		if (dtfp==null) return "";

		if (dtfp.getFullDateTime()!=null)  {
			dtfp.getFullDateTime().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dtfp.getFullDateTime());
		}
		if (dtfp.getFullDate()!=null)  {
			dtfp.getFullDate().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dtfp.getFullDate());
		}
		else if (dtfp.getYearMonth()!=null) {
			dtfp.getYearMonth().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dtfp.getYearMonth());
		}
		else if (dtfp.getYearOnly()!=null)
		{
			dtfp.getYearOnly().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dtfp.getYearOnly());
		}
		else
			return "";
    }

    String dateFPtoString(cdsDt.DateFullOrPartial dfp, int timeshiftInDays) {
		if (dfp==null) return "";
				
		if (dfp.getFullDate()!=null)  {
			dfp.getFullDate().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dfp.getFullDate());
		}		
		else if (dfp.getYearMonth()!=null) {
			dfp.getYearMonth().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dfp.getYearMonth());
		}
		else if (dfp.getYearOnly()!=null)  
		{
			dfp.getYearOnly().add(Calendar.DAY_OF_YEAR, timeshiftInDays);
			return getCalDate(dfp.getYearOnly());
		}
		else 
			return "";
    }

    String dateFPGetPartial(cdsDt.DateFullOrPartial dfp) {
		if (dfp==null) return "";

		if (dfp.getYearMonth()!=null) {
                        return CaseManagementNoteExt.YEARMONTH;
		}
		else if (dfp.getYearOnly()!=null) {
                        return CaseManagementNoteExt.YEARONLY;
		}
		else
			return "";
    }

    Date dateTimeFPtoDate(cdsDt.DateTimeFullOrPartial dtfp, int timeShiftInDays) {
		String sdate = dateTimeFPtoString(dtfp,timeShiftInDays);
		Date dDate = UtilDateUtilities.StringToDate(sdate, "yyyy-MM-dd HH:mm:ss");
		if (dDate==null)
			dDate = UtilDateUtilities.StringToDate(sdate, "yyyy-MM-dd");
		if (dDate==null)
			dDate = UtilDateUtilities.StringToDate(sdate, "HH:mm:ss");
		
		return dDate;
    }
	    
    Date dateFPtoDate(cdsDt.DateFullOrPartial dfp, int timeShiftInDays) {
            String sdate = dateFPtoString(dfp,timeShiftInDays);
            Date dDate = UtilDateUtilities.StringToDate(sdate, "yyyy-MM-dd");
            if (dDate==null)
                    dDate = UtilDateUtilities.StringToDate(sdate, "HH:mm:ss");

            return dDate;
    }

    String dateOnly(String d) {
            return UtilDateUtilities.DateToString(UtilDateUtilities.StringToDate(d),"yyyy-MM-dd");
    }

    HashMap getPersonName(cdsDt.PersonNameSimple person) {
            HashMap<String,String> name = new HashMap<String,String>();
            if (person!=null) {
                name.put("firstname", StringUtils.noNull(person.getFirstName()).trim());
                name.put("lastname", StringUtils.noNull(person.getLastName()).trim());
            }
            return name;
    }

    HashMap getPersonName(cdsDt.PersonNameSimpleWithMiddleName person) {
            HashMap<String,String> name = new HashMap<String,String>();
            if (person!=null) {
                name.put("firstname", StringUtils.noNull(person.getFirstName()).trim()+" "+StringUtils.noNull(person.getMiddleName()).trim());
                name.put("lastname", StringUtils.noNull(person.getLastName()).trim());
            }
            return name;
    }

	String getDefaultProvider() {
		ProviderData pd = getProviderByNames("doctor", "oscardoc", true);
		if (pd!=null) return pd.getProviderNo();

		return writeProviderData("doctor", "oscardoc", "");
	}

	Set<CaseManagementIssue> getCMIssue(String code) {
		CaseManagementIssue cmIssu = new CaseManagementIssue();
		cmIssu.setDemographic_no(demographicNo);
		Issue isu = caseManagementManager.getIssueInfoByCode(StringUtils.noNull(code));
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getType());
		caseManagementManager.saveCaseIssue(cmIssu);

		Set<CaseManagementIssue> sCmIssu = new HashSet<CaseManagementIssue>();
		sCmIssu.add(cmIssu);
		return sCmIssu;
	}

        boolean isICD9(cdsDt.StandardCoding diagCode) {
                if (diagCode==null) return false;

                String codingSystem = StringUtils.noNull(diagCode.getStandardCodingSystem()).toLowerCase();
		return (codingSystem.contains("icd") && codingSystem.contains("9"));
        }

        boolean isICD9(cdsDt.Code diagCode) {
                if (diagCode==null) return false;

                String codingSystem = StringUtils.noNull(diagCode.getCodingSystem()).toLowerCase();
		return (codingSystem.contains("icd") && codingSystem.contains("9"));
        }

	Set<CaseManagementIssue> getCMIssue(String cppName, cdsDt.StandardCoding diagCode) {
		Set<CaseManagementIssue> sCmIssu = new HashSet<CaseManagementIssue>();
		Issue isu = caseManagementManager.getIssueInfoByCode(StringUtils.noNull(cppName));
		if (isu!=null) {
			CaseManagementIssue cmIssu = new CaseManagementIssue();
			cmIssu.setDemographic_no(demographicNo);
			cmIssu.setIssue_id(isu.getId());
			cmIssu.setType(isu.getType());
			caseManagementManager.saveCaseIssue(cmIssu);
			sCmIssu.add(cmIssu);
		}
                if (isICD9(diagCode)) {
			isu = caseManagementManager.getIssueInfoByCode(StringUtils.noNull(diagCode.getStandardCode()));
			if (isu!=null) {
				CaseManagementIssue cmIssu = new CaseManagementIssue();
				cmIssu.setDemographic_no(demographicNo);
				cmIssu.setIssue_id(isu.getId());
				cmIssu.setType(isu.getType());
				caseManagementManager.saveCaseIssue(cmIssu);
				sCmIssu.add(cmIssu);
			}
		}
		return sCmIssu;
	}

	Set<CaseManagementIssue> getCMIssue(String cppName, cdsDt.Code diagCode) {
		Set<CaseManagementIssue> sCmIssu = new HashSet<CaseManagementIssue>();
		Issue isu = caseManagementManager.getIssueInfoByCode(StringUtils.noNull(cppName));
		if (isu!=null) {
			CaseManagementIssue cmIssu = new CaseManagementIssue();
			cmIssu.setDemographic_no(demographicNo);
			cmIssu.setIssue_id(isu.getId());
			cmIssu.setType(isu.getType());
			caseManagementManager.saveCaseIssue(cmIssu);
			sCmIssu.add(cmIssu);
		}
                if (isICD9(diagCode)) {
			isu = caseManagementManager.getIssueInfoByCode(StringUtils.noNull(diagCode.getValue()));
			if (isu!=null) {
				CaseManagementIssue cmIssu = new CaseManagementIssue();
				cmIssu.setDemographic_no(demographicNo);
				cmIssu.setIssue_id(isu.getId());
				cmIssu.setType(isu.getType());
				caseManagementManager.saveCaseIssue(cmIssu);
				sCmIssu.add(cmIssu);
			}
		}
		return sCmIssu;
	}

	String getCode(cdsDt.StandardCoding dCode, String dTitle) {
		if (dCode==null) return "";

		String ret = StringUtils.filled(dTitle) ? dTitle+" -" : "";
		ret = Util.addLine(ret, "Coding System: ", dCode.getStandardCodingSystem());
		ret = Util.addLine(ret, "Value: ", dCode.getStandardCode());
		ret = Util.addLine(ret, "Description: ", dCode.getStandardCodeDescription());

		return ret;
	}

	String getCode(cdsDt.Code dCode, String dTitle) {
		if (dCode==null) return "";

		String ret = StringUtils.filled(dTitle) ? dTitle+" -" : "";
		ret = Util.addLine(ret, "Coding System: ", dCode.getCodingSystem());
		ret = Util.addLine(ret, "Value: ", dCode.getValue());
		ret = Util.addLine(ret, "Description: ", dCode.getDescription());

		return ret;
	}

	ProviderData getProviderByNames(String firstName, String lastName, boolean matchAll) {
		ProviderData pd = new ProviderData();
		if (matchAll) {
			pd.getProviderWithNames(firstName, lastName);
		} else {
			pd.getExternalProviderWithNames(firstName, lastName);
		}
		if (StringUtils.filled(pd.getProviderNo())) return pd;
		else return null;
	}

	ProviderData getProviderByOhip(String OhipNo) {
		ProviderData pd = new ProviderData();
		pd.getProviderWithOHIP(OhipNo);
		if (StringUtils.filled(pd.getProviderNo())) return pd;
		else return null;
	}

	String getProvinceCode(cdsDt.HealthCardProvinceCode.Enum provinceCode) {
		String pcStr = provinceCode.toString();
		return getCountrySubDivCode(pcStr);
	}

	String getResidual(cdsDt.ResidualInformation resInfo) {
		String ret = "";
		if (resInfo==null) return ret;

		cdsDt.ResidualInformation.DataElement[] resData = resInfo.getDataElementArray();
		if (resData.length>0) ret = "- Residual Information -";
		for (int i=0; i<resData.length; i++) {
			if (StringUtils.filled(resData[i].getName())) {
				ret = Util.addLine(ret, "Data Name: ",   resData[i].getName());
				ret = Util.addLine(ret, "Data Type: ",   resData[i].getDataType());
				ret = Util.addLine(ret, "Content: ",     resData[i].getContent());
			}
		}
		return ret;
	}

	ArrayList<String> getUniques(String[] arr) {
		ArrayList<String> uniques = new ArrayList<String>();
		for (int i=0; i<arr.length; i++) {
			boolean match = false;
			for (String x : uniques) {
				if (arr[i].equals(x)) {
					match = true;
					break;
				}
			}
			if (!match) uniques.add(arr[i]);
		}
		return uniques;
	}

	String getYN(cdsDt.YnIndicator yn) {
		if (yn==null) return "";

		String ret = "No";
		if (yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y || yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y_2) {
			ret = "Yes";
		} else if (yn.getBoolean()) {
			ret = "Yes";
		}
		return ret;
	}

        Boolean getYN(cdsDt.YnIndicatorAndBlank yn) {
		if (yn==null) return null;

		boolean ret = false;
		if (yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y || yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y_2) {
			ret = true;
		} else if (yn.getBoolean()) {
			ret = true;
		}
		return ret;
        }

	String mapPreventionType(cdsDt.Code imCode) {
		if (imCode==null) return "";
		if (!imCode.getCodingSystem().equalsIgnoreCase("DIN")) return "";

		ArrayList<String> dinFlu = new ArrayList<String>();
		ArrayList<String> dinHebAB = new ArrayList<String>();
		ArrayList<String> dinCHOLERA = new ArrayList<String>();

		dinFlu.add("01914510");
		dinFlu.add("02015986");
		dinFlu.add("02269562");
		dinFlu.add("02223929");

		dinHebAB.add("02230578");
		dinHebAB.add("02237548");

		dinCHOLERA.add("00074969");
		dinCHOLERA.add("02247208");

		if (dinFlu.contains(StringUtils.noNull(imCode.getValue()))) return "Flu";
		if (dinHebAB.contains(StringUtils.noNull(imCode.getValue()))) return "HebAB";
		if (dinCHOLERA.contains(StringUtils.noNull(imCode.getValue()))) return "CHOLERA";
		return "OtherA";
	}

	String[] packMsgs(ArrayList err_demo, ArrayList err_data, ArrayList err_summ, ArrayList err_othe, ArrayList err_note, ArrayList warnings) {
		if (!(err_demo.isEmpty() && err_data.isEmpty() && err_summ.isEmpty() && err_othe.isEmpty() && err_note.isEmpty())) {
			String title = "Fail to import patient "+patientName;
			if (StringUtils.filled(demographicNo)) {
				title = "Patient "+patientName+" (Demographic no="+demographicNo+")";
			}
			warnings.add(fillUp("---- "+title, '-', 150));
		}
		warnings.addAll(err_demo);
		warnings.addAll(err_data);
		warnings.addAll(err_summ);
		warnings.addAll(err_othe);
		warnings.addAll(err_note);

		String err_all = aListToMsg(err_demo);
		err_all = Util.addLine(err_all, aListToMsg(err_data));
		err_all = Util.addLine(err_all, aListToMsg(err_summ));
		err_all = Util.addLine(err_all, aListToMsg(err_othe));
		err_all = Util.addLine(err_all, aListToMsg(err_note));

		String[] msgs = {demographicNo, err_data.isEmpty()?"Yes":"No",
		err_summ.isEmpty()?"Yes":"No",
		err_othe.isEmpty()?"Yes":"No", err_all};
		return msgs;
	}

	CaseManagementNote prepareCMNote(String caisi_role, String uuid) {
		CaseManagementNote cmNote = new CaseManagementNote();
		cmNote.setUpdate_date(new Date());
		cmNote.setObservation_date(new Date());
		cmNote.setDemographic_no(demographicNo);
		cmNote.setProviderNo(admProviderNo);
		cmNote.setSigning_provider_no(admProviderNo);
		cmNote.setSigned(true);
		cmNote.setHistory("");
		cmNote.setReporter_program_team("0");
		cmNote.setProgram_no(programId);
                if (StringUtils.filled(uuid)) cmNote.setUuid(uuid);
                else cmNote.setUuid(UUID.randomUUID().toString());

                if (caisi_role==null || (!caisi_role.equals("1") && !caisi_role.equals("2"))) caisi_role="1";
                cmNote.setReporter_caisi_role(caisi_role);  //"1" for doctor, "2" for nurse - note hidden in echart

		return cmNote;
	}

	void saveLinkNote(Long hostId, CaseManagementNote cmn) {
		saveLinkNote(cmn, CaseManagementNoteLink.CASEMGMTNOTE, hostId);
	}

	void saveLinkNote(CaseManagementNote cmn, Integer tableName, Long tableId) {
                saveLinkNote(cmn, tableName, tableId, null);
	}

	void saveLinkNote(CaseManagementNote cmn, Integer tableName, Long tableId, String otherId) {
		if (StringUtils.filled(cmn.getNote())) {
			caseManagementManager.saveNoteSimple(cmn);    //new note id created

			CaseManagementNoteLink cml = new CaseManagementNoteLink();
			cml.setTableName(tableName);
			cml.setTableId(tableId);
			cml.setNoteId(cmn.getId()); //new note id
                        cml.setOtherId(otherId);
			caseManagementManager.saveNoteLink(cml);
		}
	}

	void saveMeasurementsExt(Long measurementId, String key, String val) throws SQLException {
		if (measurementId!=null && StringUtils.filled(key)) {
			MeasurementsExt mx = new MeasurementsExt(measurementId.intValue());
			mx.setKeyVal(key);
			mx.setVal(StringUtils.noNull(val));
			ImportExportMeasurements.saveMeasurementsExt(mx);
		}
	}

	String updateExternalProviderNames(String firstName, String lastName, ProviderData pd) {
		// For external provider only
		if (pd.getProviderNo().charAt(0)=='-') {
			if (StringUtils.empty(pd.getFirst_name()) && StringUtils.empty(pd.getLast_name())) {
				pd.setFirst_name(StringUtils.noNull(firstName));
				pd.setLast_name(StringUtils.noNull(lastName));
			}
		}
		return pd.getProviderNo();
	}

	String updateExternalProviderOhip(String ohipNo, ProviderData pd) {
		// For external provider only
		if (pd.getProviderNo().charAt(0)=='-') {
			if (StringUtils.empty(pd.getOhip_no())) {
				pd.setOhip_no(StringUtils.noNull(ohipNo));
			}
		}
		return pd.getProviderNo();
	}

        String writeProviderData(String firstName, String lastName, String ohipNo) {
            return writeProviderData(firstName, lastName, ohipNo, null);
        }

	String writeProviderData(String firstName, String lastName, String ohipNo, String cpsoNo) {
                firstName = StringUtils.noNull(firstName);
                lastName = StringUtils.noNull(lastName);

		ProviderData pd = getProviderByOhip(ohipNo);
		if (pd!=null) return updateExternalProviderNames(firstName, lastName, pd);

		pd = getProviderByNames(firstName, lastName, matchProviderNames);
		if (pd!=null) return updateExternalProviderOhip(ohipNo, pd);

		//Write as a new provider
		if (StringUtils.empty(firstName) && StringUtils.empty(lastName) && StringUtils.empty(ohipNo)) return ""; //no information at all!
		pd = new ProviderData();
		pd.addExternalProvider(firstName, lastName, ohipNo, cpsoNo);
		return pd.getProviderNo();
	}

	String aListToMsg(ArrayList<String> alist) {
		String msgs = "";
		for (int i=0; i<alist.size(); i++) {
			String msg = alist.get(i);
			if (i>0) msgs += "\n";
			msgs += msg;
		}
		return msgs;
	}

	void insertIntoAdmission() {
		Admission admission = new Admission();
		admission.setClientId(Integer.valueOf(demographicNo));
		admission.setProviderNo(admProviderNo);
		admission.setProgramId(Integer.valueOf(programId));
		admission.setAdmissionDate(new Date());
		admission.setAdmissionFromTransfer(false);
		admission.setDischargeFromTransfer(false);
		admission.setAdmissionStatus("current");
		admission.setTeamId(0);
		admission.setTemporaryAdmission(false);
		admission.setRadioDischargeReason("0");
		admission.setClientStatusId(0);
		admission.setAutomaticDischarge(false);
		
		admissionDao.saveAdmission(admission);
	}

	String getLabDline(LaboratoryResults labRes, int timeShiftInDays){
		StringBuilder s = new StringBuilder();
		appendIfNotNull(s,"LaboratoryName",labRes.getLaboratoryName());
		appendIfNotNull(s,"TestNameReportedByLab", labRes.getTestNameReportedByLab());
		appendIfNotNull(s,"LabTestCode",labRes.getLabTestCode());
		appendIfNotNull(s,"TestName", labRes.getTestName());
		appendIfNotNull(s,"AccessionNumber",labRes.getAccessionNumber());

		if (labRes.getResult ()!=null) {
			appendIfNotNull(s,"Value",labRes.getResult().getValue());
			appendIfNotNull(s,"UnitOfMeasure",labRes.getResult().getUnitOfMeasure());
		}
		if (labRes.getReferenceRange()!=null) {
			LaboratoryResults.ReferenceRange ref = labRes.getReferenceRange();
			appendIfNotNull(s,"LowLimit",ref.getLowLimit());
			appendIfNotNull(s,"HighLimit",ref.getHighLimit());
			appendIfNotNull(s,"ReferenceRangeText", ref.getReferenceRangeText());
		}

		appendIfNotNull(s,"LabRequisitionDateTime",dateTimeFPtoString(labRes.getLabRequisitionDateTime(), timeShiftInDays));
		appendIfNotNull(s,"CollectionDateTime",dateTimeFPtoString( labRes.getCollectionDateTime(), timeShiftInDays));

                LaboratoryResults.ResultReviewer[] resultReviewers = labRes.getResultReviewerArray();
                if (resultReviewers.length>0) {
                    appendIfNotNull(s,"DateTimeResultReviewed",dateTimeFPtoString(resultReviewers[0].getDateTimeResultReviewed(), timeShiftInDays));
                    appendIfNotNull(s,"OHIP ID :", resultReviewers[0].getOHIPPhysicianId());
                    cdsDt.PersonNameSimple reviewerName = resultReviewers[0].getName();
                    if (reviewerName!=null) {
                        appendIfNotNull(s,"Reviewer First Name:", reviewerName.getFirstName());
                        appendIfNotNull(s,"Reviewer Last Name:", reviewerName.getLastName());
                    }
                }
		
		appendIfNotNull(s,"ResultNormalAbnormalFlag",""+labRes.getResultNormalAbnormalFlag());
		appendIfNotNull(s,"TestResultsInformationreportedbytheLaboratory",labRes.getTestResultsInformationReportedByTheLab());
		appendIfNotNull(s,"NotesFromLab",labRes.getNotesFromLab());
		appendIfNotNull(s,"PhysiciansNotes",labRes.getPhysiciansNotes());

		return s.toString();
	}

	void appendIfNotNull(StringBuilder s, String name, String object){
		if (object != null){
			s.append(name);
                        s.append(": ");
                        s.append(object);
                        s.append("\n");
		}
	}

        void addOneEntry(String category) {
            if (StringUtils.empty(category)) return;
            
            Integer n = entries.get(category+importNo);
            n = n==null ? 1 : n+1;
            entries.put(category+importNo, n);
        }
}