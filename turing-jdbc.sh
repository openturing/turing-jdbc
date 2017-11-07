#! /bin/bash
./gradlew build shadowJar
java -Dlog4j.configurationFile=log4j2.properties -cp .:build/libs/turing-jdbc-fat-jar.jar com.viglet.turing.tool.jdbc.JDBCImportTool "$@"

