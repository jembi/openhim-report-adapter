package org.jembi.openhim;

import java.util.List;

public class Indicator {
	
	private String name;
	private List<DataElement> dataElements;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<DataElement> getDataElements() {
		return dataElements;
	}
	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}

}
