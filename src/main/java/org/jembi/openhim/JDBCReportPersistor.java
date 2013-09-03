package org.jembi.openhim;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class JDBCReportPersistor implements Callable {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private String url = "jdbc:mysql://localhost:3306/interoperability_layer";
	private String username = "root";
	private String password = "Jembi#123";
	
	private static final String insertReport = "INSERT INTO report (report_date, site, transaction_id) VALUES (?, ?, ?)";
	private static final String insertIndicator = "INSERT INTO indicator (NAME, report_id) VALUES (?, ?)";
	private static final String insertDataElement = "INSERT INTO data_element (DATATYPE, NAME, UNITS, VALUE, indicator_id) VALUES (?, ?, ?, ?, ?)";
	private static final String selectTransactionId = "SELECT id from transaction_log WHERE uuid = ?";
	private static final String selectSiteId = "SELECT id from sites WHERE implementation_id = ?";

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage msg = eventContext.getMessage();
		Report report = (Report) msg.getPayload();
		
		String txUUID = msg.getProperty("OPENHIM_TX_UUID", PropertyScope.INBOUND);
		
		try {
			persistReport(report, txUUID);
		} catch (SQLException e) {
			log.error("Could not save report to database", e);
		}
		
		return null;
	}

	private void persistReport(Report r, String txUUID) throws SQLException {
		Connection conn = getConnection();
		conn.setAutoCommit(false);
		
		PreparedStatement ps = conn.prepareStatement(selectTransactionId, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, txUUID);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			r.setTransactionId(rs.getInt(1));
		}
		
		ps = conn.prepareStatement(selectSiteId, Statement.RETURN_GENERATED_KEYS);
		ps.setInt(1, r.getSiteId());
		rs = ps.executeQuery();
		if (rs.next()) {
			r.setSiteId(rs.getInt(1));
		}
		
		ps = conn.prepareStatement(insertReport, Statement.RETURN_GENERATED_KEYS);
		
		ps.setDate(1, new Date(r.getDate().getTime()));
		ps.setInt(2, r.getSiteId());
		ps.setInt(3, r.getTransactionId());
		
		ps.executeUpdate();
		
		ResultSet genKeys = ps.getGeneratedKeys();
		int reportId = -1;
		if (genKeys.next()) {
			reportId = genKeys.getInt(1);
		}
		
		for (Indicator i : r.getIndicators()) {
			persistIndicator(conn, i, reportId);
		}
		
		conn.commit();
		conn.close();
	}
	
	private void persistIndicator(Connection conn, Indicator i, int reportId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(insertIndicator, Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, i.getName());
		ps.setInt(2, reportId);
		
		ps.executeUpdate();
		
		ResultSet genKeys = ps.getGeneratedKeys();
		int indicatorId = -1;
		if (genKeys.next()) {
			indicatorId = genKeys.getInt(1);
		}
		
		for (DataElement d : i.getDataElements()) {
			persistDataElement(conn, d, indicatorId);
		}
	}

	private void persistDataElement(Connection conn, DataElement d,
			int indicatorId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(insertDataElement, Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, d.getDatatype());
		ps.setString(2, d.getName());
		ps.setString(3, d.getUnits());
		ps.setString(4, d.getValue());
		ps.setInt(5, indicatorId);
		
		ps.executeUpdate();
	}

	private Connection getConnection() throws SQLException {

	    Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", this.username);
	    connectionProps.put("password", this.password);

        conn = DriverManager.getConnection(url, connectionProps);
	        
	    return conn;
	}

}
