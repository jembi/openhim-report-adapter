package org.jembi.openhim; 

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.mule.api.MuleMessage;
import org.mule.transformer.AbstractMessageTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IndicatorReportXmlToObjectTransformer extends AbstractMessageTransformer {
	
	private XPathFactory xPathfactory = XPathFactory.newInstance();
	private XPath xpath = xPathfactory.newXPath();
	
	@Override
	public Object transformMessage(MuleMessage msg, String outputEncoding)
			throws org.mule.api.transformer.TransformerException {
		String xml;
		Report report = null;
		try {
			xml = msg.getPayloadAsString();
			Document doc = getMessageAsDocument(xml);
			report = extractReport(doc);
		} catch (Exception e) {
			throw new org.mule.api.transformer.TransformerException(this, e);
		}
		
		return report;
	}

	public Report extractReport(Document doc) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("/report");
		NodeList reportNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		Node node = reportNodes.item(0);
		Report r = new Report();
		
		expr = xpath.compile("/report/date/text()");
		String dateStr = (String) expr.evaluate(node, XPathConstants.STRING);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		r.setDate(date);
		
		expr = xpath.compile("/report/siteId/text()");
		String siteIdStr = (String) expr.evaluate(node, XPathConstants.STRING);
		r.setSiteId(Integer.parseInt(siteIdStr));
		
		expr = xpath.compile("/report/indicator");
		NodeList indicatorNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		List<Indicator> indicators = new ArrayList<Indicator>();
		for (int j = 0; j < indicatorNodes.getLength(); j++) {
			indicators.add(extractIndicators(indicatorNodes.item(j), r));
		}
		r.setIndicators(indicators);
		
		return r;
	}

	private Indicator extractIndicators(Node indicator, Report r) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("string(@name)");
		String name = (String) expr.evaluate(indicator, XPathConstants.STRING);
		
		Indicator i = new Indicator();
		i.setName(name);
		i.setReport(r);
		
		expr = xpath.compile("dataElement");
		NodeList dataElementNodes = (NodeList) expr.evaluate(indicator, XPathConstants.NODESET);
		
		List<DataElement> dataElements = new ArrayList<DataElement>();
		for (int j = 0; j < dataElementNodes.getLength(); j++) {
			dataElements.add(extractDataElements(dataElementNodes.item(j), i));
		}
		i.setDataElements(dataElements);
		
		return i;
	}

	private DataElement extractDataElements(Node dataElement, Indicator i) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("string(@name)");
		String name = (String) expr.evaluate(dataElement, XPathConstants.STRING);
		
		expr = xpath.compile("string(@dataType)");
		String dataType = (String) expr.evaluate(dataElement, XPathConstants.STRING);
		
		expr = xpath.compile("string(@units)");
		String units = (String) expr.evaluate(dataElement, XPathConstants.STRING);
		
		expr = xpath.compile("string(text())");
		String value = (String) expr.evaluate(dataElement, XPathConstants.STRING);
		
		DataElement d = new DataElement();
		d.setName(name);
		d.setDatatype(dataType);
		d.setUnits(units);
		d.setValue(value);
		d.setIndicator(i);
		
		return d;
	}
	
	public Document getMessageAsDocument(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xml)));
	}
	
}
