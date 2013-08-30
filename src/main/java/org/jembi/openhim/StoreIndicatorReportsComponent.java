package org.jembi.openhim; 

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.component.DefaultJavaComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StoreIndicatorReportsComponent extends DefaultJavaComponent implements Callable {
	
	private XPathFactory xPathfactory = XPathFactory.newInstance();
	private XPath xpath = xPathfactory.newXPath();

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		MuleMessage msg = eventContext.getMessage();
		String xml = msg.getPayloadAsString();
		
		Document doc = getMessageAsDocument(xml);
		
		extractReports(doc);
		
		return null;
	}

	public List<Report> extractReports(Document doc) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("/report");
		NodeList reportNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		List<Report> reports = new ArrayList<Report>();
		for (int i = 0; i < reportNodes.getLength(); i++) {
			Node node = reportNodes.item(i);
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
			String siteId = (String) expr.evaluate(node, XPathConstants.STRING);
			r.setSiteId(siteId);
			
			expr = xpath.compile("/report/indicator");
			NodeList indicatorNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			
			List<Indicator> indicators = new ArrayList<Indicator>();
			for (int j = 0; j < indicatorNodes.getLength(); j++) {
				indicators.add(extractIndicators(indicatorNodes.item(j)));
			}
			r.setIndicators(indicators);
			
			reports.add(r);
		}

		return reports;
	}

	private Indicator extractIndicators(Node indicator) throws XPathExpressionException {
		System.out.println(nodeToString(indicator));
		
		XPathExpression expr = xpath.compile("string(@name)");
		String name = (String) expr.evaluate(indicator, XPathConstants.STRING);
		
		Indicator i = new Indicator();
		i.setName(name);
		
		expr = xpath.compile("dataElement");
		NodeList dataElementNodes = (NodeList) expr.evaluate(indicator, XPathConstants.NODESET);
		
		List<DataElement> dataElements = new ArrayList<DataElement>();
		for (int j = 0; j < dataElementNodes.getLength(); j++) {
			dataElements.add(extractDataElements(dataElementNodes.item(j)));
		}
		i.setDataElements(dataElements);
		
		return i;
	}

	private DataElement extractDataElements(Node dataElement) throws XPathExpressionException {
		System.out.println(nodeToString(dataElement));
		
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
		d.setDataType(dataType);
		d.setUnits(units);
		d.setValue(value);
		
		return d;
	}
	
	private int saveReportToDB(String date, String siteId, int i) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private int saveIndicatorToDB(String name, int reportId) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void saveDataElement(String name, String dataType, String value,
			int indicatorId) {
		// TODO Auto-generated method stub
	}

	public Document getMessageAsDocument(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xml)));
	}
	
	// TODO: remove testing code
	private static String nodeToString(Node node) {
	    StringWriter sw = new StringWriter();
	    try {
	      Transformer t = TransformerFactory.newInstance().newTransformer();
	      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	      t.setOutputProperty(OutputKeys.INDENT, "yes");
	      t.transform(new DOMSource(node), new StreamResult(sw));
	    } catch (TransformerException te) {
	      System.out.println("nodeToString Transformer Exception");
	    }
	    return sw.toString();
	  }
	
}
