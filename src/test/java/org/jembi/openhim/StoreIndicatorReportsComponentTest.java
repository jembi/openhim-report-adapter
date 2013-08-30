package org.jembi.openhim;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.jembi.Util;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class StoreIndicatorReportsComponentTest {

	@Test
	public void test() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, ParseException {
		String xmlStr = Util.getResourceAsString("generated-message130828.xml");
		StoreIndicatorReportsComponent sirc = new StoreIndicatorReportsComponent();
		Document doc = sirc.getMessageAsDocument(xmlStr);
		
		List<Report> reports = sirc.extractReports(doc);
		
		assertNotNull(reports);
		assertEquals(1, reports.size());
		
		Report report = reports.get(0);
		assertEquals("363", report.getSiteId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
		assertEquals(sdf.parse("2013-08-28"), report.getDate());
		
		assertEquals(8, report.getIndicators().size());
		
		Indicator indicator = report.getIndicators().get(0);
		assertEquals("responseTimes", indicator.getName());
		
		assertEquals(3, indicator.getDataElements().size());
		
		DataElement dataElement = indicator.getDataElements().get(0);
		assertEquals("minResponseTimeMS", dataElement.getName());
		assertEquals("int", dataElement.getDataType());
		assertEquals("msec", dataElement.getUnits());
		assertEquals("0", dataElement.getValue());
	}

}
