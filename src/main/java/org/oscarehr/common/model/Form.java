package org.oscarehr.common.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="form")
public class Form extends AbstractModel<Integer>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="form_no")
	private Integer id;

	@Column(name="demographic_no")
	private int demographicNo;

	@Column(name="provider_no")
	private String providerNo;

	@Column(name="form_date")
	@Temporal(TemporalType.DATE)
	private Date formDate;

	@Column(name="form_time")
	@Temporal(TemporalType.TIME)
	private Date formTime;

	@Column(name="form_name")
	private String formName;

	private String content;

	public Integer getId() {
    	return id;
    }

	public void setId(Integer id) {
    	this.id = id;
    }

	public int getDemographicNo() {
    	return demographicNo;
    }

	public void setDemographicNo(int demographicNo) {
    	this.demographicNo = demographicNo;
    }

	public String getProviderNo() {
    	return providerNo;
    }

	public void setProviderNo(String providerNo) {
    	this.providerNo = providerNo;
    }

	public Date getFormDate() {
    	return formDate;
    }

	public void setFormDate(Date formDate) {
    	this.formDate = formDate;
    }

	public Date getFormTime() {
    	return formTime;
    }

	public void setFormTime(Date formTime) {
    	this.formTime = formTime;
    }

	public String getFormName() {
    	return formName;
    }

	public void setFormName(String formName) {
    	this.formName = formName;
    }

	public String getContent() {
    	return content;
    }

	public void setContent(String content) {
    	this.content = content;
    }


}