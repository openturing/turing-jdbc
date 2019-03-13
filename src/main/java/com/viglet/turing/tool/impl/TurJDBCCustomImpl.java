package com.viglet.turing.tool.impl;

import java.sql.Connection;
import java.util.Map;

public interface TurJDBCCustomImpl {

	public Map<String, Object> run(Connection connection, Map<String, Object> attributes);
}
