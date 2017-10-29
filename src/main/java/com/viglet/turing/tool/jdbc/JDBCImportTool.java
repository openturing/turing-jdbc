package com.viglet.turing.tool.jdbc;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class JDBCImportTool {
	@Parameter(names = { "--length", "-l" })
	int length;
	@Parameter(names = { "--pattern", "-p" })
	int pattern;

	public static void main(String... argv) {
		JDBCImportTool main = new JDBCImportTool();
		JCommander.newBuilder().addObject(main).build().parse(argv);
		main.run();
		System.out.println("Viglet Turing JDBC Import Tool.");
	}

	public void run() {
		System.out.printf("%d %d", length, pattern);
	}
}
