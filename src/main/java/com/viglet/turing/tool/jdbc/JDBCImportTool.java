package com.viglet.turing.tool.jdbc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

public class JDBCImportTool {
	@Parameter(names = { "--driver", "-d" })
	String driver;
	@Parameter(names = { "--connect", "-c" })
	String connect;
	@Parameter(names = { "--query", "-q" })
	String query;
	@Parameter(names = { "--username", "-u" })
	String username;
	@Parameter(names = { "--password", "-p" })
	String password;
	@Parameter(names = { "--type", "-t" })
	String type;
	@Parameter(names = { "--chunk", "-z" })
	int chunk;

	public static void main(String... argv) {
		JDBCImportTool main = new JDBCImportTool();
		JCommander.newBuilder().addObject(main).build().parse(argv);
		System.out.println("Viglet Turing JDBC Import Tool.");
		main.run();

	}

	public void run() {
		System.out.printf("driver: %s\nconnect: %s\nquery: %s\nusername: %s\npassword %s\n", driver, connect, query,
				username, password);
		this.select();
	}

	public void select() {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(driver);

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(connect, username, password);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
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
					// System.out.print("classname: " + rsmd.getColumnClassName(c) + " " + name + ":
					// ");
					if (className.equals("java.lang.Integer")) {
						int intValue = rs.getInt(c);
						// System.out.print(intValue + " ");
						jsonRow.put(name, intValue);
					} else {
						String strValue = rs.getString(c);
						// System.out.print(strValue + " ");
						jsonRow.put(name, strValue);
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
		HttpPost httpPost = new HttpPost("http://localhost:2700/api/sn/import");
		StringEntity entity = new StringEntity(jsonResult.toString());
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = client.execute(httpPost);
		//System.out.println(response.toString());
		client.close();
	}
}
