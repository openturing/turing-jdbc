package com.viglet.turing.tool.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

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
			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("id");
				String name = rs.getString("name");

				// Display values
				System.out.print("ID: " + id);
				System.out.print(", Name: " + name + "\n");
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
}
