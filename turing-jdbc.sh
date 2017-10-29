#! /bin/bash
./gradlew build shadowJar
java -cp .:build/libs/turing-jdbc-fat-jar.jar com.viglet.turing.tool.jdbc.JDBCImportTool "$@"

