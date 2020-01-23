/*
 * Copyright (C) 2017-2020 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.turing.tool.jdbc.format;

import com.viglet.turing.tool.jdbc.JDBCImportTool;
import com.viglet.turing.util.HtmlManipulator;

/**
*
* @author Alexandre Oliveira
* @since 0.3.0
*
**/
public class TurFormatValue {
	JDBCImportTool jdbcImportTool = null;
	public TurFormatValue(JDBCImportTool jdbcImportTool) {
		this.jdbcImportTool = jdbcImportTool;
	}
	public String format(String name, String value) {
		String[] strHTMLFields = jdbcImportTool.htmlField.toLowerCase().split(",");
		for (String strHTMLField : strHTMLFields) {
			if (name.toLowerCase().equals(strHTMLField.toLowerCase())) {
				if (name.toLowerCase().equals("id")) {
					this.idField(HtmlManipulator.html2Text(value));

				} else {
					return HtmlManipulator.html2Text(value);
				}
			}
		}
		if (name.toLowerCase().equals("id")) {
			return this.idField(value);
		} else {
			return value;
		}
	}

	public String idField(int idValue) {
		if (jdbcImportTool.typeInId) {
			return jdbcImportTool.type + idValue;
		} else {
			return Integer.toString(idValue);
		}
	}

	public String idField(String idValue) {
		if (jdbcImportTool.typeInId) {
			return jdbcImportTool.type + idValue;
		} else {
			return idValue;
		}
	}

}
