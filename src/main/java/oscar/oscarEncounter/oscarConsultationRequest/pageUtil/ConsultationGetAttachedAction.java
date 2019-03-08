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

package oscar.oscarEncounter.oscarConsultationRequest.pageUtil;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.oscarehr.consultations.service.ConsultationAttachmentService;
import org.oscarehr.eform.model.EFormData;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;
import oscar.dms.EDocUtil;
import oscar.oscarLab.ca.on.CommonLabResultData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsultationGetAttachedAction extends Action
{
	private static ConsultationAttachmentService consultationAttachmentService = SpringUtils.getBean(ConsultationAttachmentService.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		CommonLabResultData labData = new CommonLabResultData();
		String displayValue = "display: none;";

		LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
		String demoNo = request.getParameter("demo");
		String requestId = request.getParameter("requestId");

		ArrayList labs = labData.populateLabResultsData(loggedInInfo, demoNo, requestId, CommonLabResultData.ATTACHED);
		ArrayList privateDocs = EDocUtil.listDocs(loggedInInfo, demoNo, requestId, EDocUtil.ATTACHED);

		List<EFormData> eFormList = consultationAttachmentService.getAttachedEForms(Integer.parseInt(demoNo), Integer.parseInt(requestId));
		List<String> eFormLabels = getEFormLabels(eFormList);

		if(privateDocs.isEmpty() && labs.isEmpty() && eFormLabels.isEmpty())
		{
			displayValue = "";
		}

		request.setAttribute("displayValue", displayValue);
		request.setAttribute("docArray", privateDocs);
		request.setAttribute("labArray", labs);
		request.setAttribute("eFormArray", eFormLabels);

		return mapping.findForward("success");
	}

	private List<String> getEFormLabels(List<EFormData> eFormList)
	{
		List<String> labels = new ArrayList<>(eFormList.size());
		for(EFormData eform : eFormList)
		{
			labels.add(eform.getFormName());
		}
		return labels;
	}
}