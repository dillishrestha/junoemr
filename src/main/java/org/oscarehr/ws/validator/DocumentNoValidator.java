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
package org.oscarehr.ws.validator;

import org.oscarehr.document.dao.DocumentDao;
import org.oscarehr.util.SpringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom validator to ensure document numbers match existing documents
 */
public class DocumentNoValidator implements ConstraintValidator<DocumentNoConstraint, Integer>
{
	private DocumentDao documentDao = SpringUtils.getBean(DocumentDao.class);
	private boolean allowNull;

	@Override
	public void initialize(DocumentNoConstraint documentNoConstraint)
	{
		allowNull = documentNoConstraint.allowNull();
	}

	@Override
	public boolean isValid(Integer documentNo, ConstraintValidatorContext constraintValidatorContext)
	{
		if(documentNo == null)
		{
			return allowNull;
		}
		if(documentNo < 1)
		{
			return false;
		}
		return documentDao.documentExists(documentNo);
	}
}