package com.viglet.turing.tool.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.viglet.turing.tool.jdbc.format.TurFormatValue;
import com.viglet.turing.util.HtmlManipulator;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class JDBCImportTool {
	static final Logger logger = LogManager.getLogger(JDBCImportTool.class.getName());

	@Parameter(names = { "--driver", "-d" }, description = "Manually specify JDBC driver class to use", required = true)
	private String driver = null;

	@Parameter(names = { "--connect", "-c" }, description = "Specify JDBC connect string", required = true)
	private String connect = null;

	@Parameter(names = { "--query", "-q" }, description = "Import the results of statement", required = true)
	private String query = null;

	@Parameter(names = { "--server", "-s" }, description = "Viglet Turing Server")
	private String turingServer = "http://localhost:2700";

	@Parameter(names = { "--username", "-u" }, description = "Set authentication username")
	private String username = null;

	@Parameter(names = { "--password", "-p" }, description = "Set authentication password")
	private String password = null;

	@Parameter(names = { "--type", "-t" }, description = "Set Content Type name")
	public String type = "CONTENT_TYPE";

	@Parameter(names = { "--chunk", "-z" }, description = "Number of items to be sent to the queue")
	private int chunk = 100;

	@Parameter(names = { "--include-type-in-id", "-i" }, description = "Include Content Type name in Id", arity = 1)
	public boolean typeInId = false;

	@Parameter(names = { "--multi-valued-separator" }, description = "Multi Valued Separator")
	private String mvSeparator = ",";

	@Parameter(names = { "--multi-valued-field" }, description = "Multi Valued Fields")
	private String mvField = "";

	@Parameter(names = { "--remove-html-tags-field" }, description = "Remove HTML Tags into content of field")
	public String htmlField = "";

	@Parameter(names = "--help", description = "Print usage instructions", help = true)
	private boolean help = false;

	private static TurFormatValue turFormatValue = null;
	
	public static void main(String... argv) {

		JDBCImportTool main = new JDBCImportTool();
		JCommander jCommander = JCommander.newBuilder().addObject(main).build();
		try {
			jCommander.parse(argv);
			if (main.help) {
				jCommander.usage();
				return;
			}
			System.out.println("Viglet Turing JDBC Import Tool.");
			turFormatValue = new TurFormatValue(main);
			main.run();
		} catch (ParameterException e) {
			// Handle everything on your own, i.e.
			logger.info("Error: " + e.getLocalizedMessage());
			jCommander.usage();
		}

	}

	public void run() {
		logger.info(String.format("driver: %s", driver));
		logger.info(String.format("connect: %s", connect));
		logger.info(String.format("query: %s", query));
		logger.info(String.format("username: %s", username));

		this.select();
	}

	public void select() {
		Connection conn = null;
		Statement stmt = null;
		try {
			// Register JDBC driver
			Class.forName(driver);

			// Open a connection
			logger.info("Connecting to database...");
			conn = DriverManager.getConnection(connect, username, password);

			// Execute a query
			logger.info("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = query;
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			JSONArray jsonResult = new JSONArray();
			int chunkCurrent = 0;
			int chunkTotal = 0;
			while (rs.next()) {

				JSONObject jsonRow = new JSONObject();
				ResultSetMetaData rsmd = rs.getMetaData();

				// Retrieve by column name
				for (int c = 1; c <= rsmd.getColumnCount(); c++) {
					String name = rsmd.getColumnLabel(c);
					String className = rsmd.getColumnClassName(c);
					String[] strMvFields = mvField.toLowerCase().split(",");

					boolean isMultiValued = false;

					for (String strMvField : strMvFields) {
						if (name.toLowerCase().equals(strMvField.toLowerCase())) {
							isMultiValued = true;
							String[] mvValues = rs.getString(c).split(mvSeparator);
							JSONArray jsonMVValues = new JSONArray();
							for (String mvValue : mvValues) {
								jsonMVValues.put(turFormatValue.format(name, mvValue));
							}
							jsonRow.put(name, jsonMVValues);
						}
					}

					if (!isMultiValued) {
						if (className.equals("java.lang.Integer")) {
							int intValue = rs.getInt(c);
							jsonRow.put(name, turFormatValue.format(name,Integer.toString(intValue)));
						} else {
							String strValue = rs.getString(c);
							jsonRow.put(name, turFormatValue.format(name, strValue));
						}
					}
				}
				jsonRow.put("type", type);
				jsonResult.put(jsonRow);

				chunkTotal++;
				chunkCurrent++;
				if (chunkCurrent == chunk) {
					this.sendServer(jsonResult, chunkTotal);
					chunkCurrent = 0;
				}
			}
			if (chunkCurrent > 0) {
				this.sendServer(jsonResult, chunkTotal);
				chunkCurrent = 0;
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
	}


	public void sendServer(JSONArray jsonResult, int chunkTotal) throws ClientProtocolException, IOException {
		System.out.print("Importing " + (chunkTotal - chunk) + " to " + chunkTotal + " items\n");
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(String.format("%s/api/sn/import", turingServer));
		StringEntity entity = new StringEntity(jsonResult.toString());
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = client.execute(httpPost);
		// System.out.println(response.toString());
		client.close();
	}
}
