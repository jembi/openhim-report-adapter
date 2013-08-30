package org.jembi.openhim;

import java.util.Date;
import java.util.List;

public class Report {
	
	private Date date;
	private String siteId;
	private List<Indicator> indicators;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public List<Indicator> getIndicators() {
		return indicators;
	}
	public void setIndicators(List<Indicator> indicators) {
		this.indicators = indicators;
	}

}
