package com.viglet.turing.tool.ext;

import java.sql.Connection;
import java.util.Map;

import com.viglet.turing.tool.impl.TurJDBCCustomImpl;

public class TurJDBCCustomSample implements TurJDBCCustomImpl {
	final static String TITLE = "title";
	
	@Override
	public Map<String, Object> run(Connection connection, Map<String, Object> attributes) {
	
		if (attributes.containsKey(TITLE))
			attributes.replace(TITLE, String.format("Sample: %s", attributes.get(TITLE)));

		return attributes;
	}

}
