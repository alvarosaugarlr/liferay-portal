/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.client.serdes.v1_0;

import com.liferay.scim.rest.client.dto.v1_0.SchemaDefinition;
import com.liferay.scim.rest.client.dto.v1_0.Schemas;
import com.liferay.scim.rest.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Olivér Kecskeméty
 * @generated
 */
@Generated("")
public class SchemasSerDes {

	public static Schemas toDTO(String json) {
		SchemasJSONParser schemasJSONParser = new SchemasJSONParser();

		return schemasJSONParser.parseToDTO(json);
	}

	public static Schemas[] toDTOs(String json) {
		SchemasJSONParser schemasJSONParser = new SchemasJSONParser();

		return schemasJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Schemas schemas) {
		if (schemas == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (schemas.getItemsPerPage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemsPerPage\": ");

			sb.append(schemas.getItemsPerPage());
		}

		if (schemas.getSchemaDefinitions() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"schemaDefinitions\": ");

			sb.append("[");

			for (int i = 0; i < schemas.getSchemaDefinitions().length; i++) {
				sb.append(String.valueOf(schemas.getSchemaDefinitions()[i]));

				if ((i + 1) < schemas.getSchemaDefinitions().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (schemas.getStartIndex() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"startIndex\": ");

			sb.append(schemas.getStartIndex());
		}

		if (schemas.getTotalResults() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"totalResults\": ");

			sb.append(schemas.getTotalResults());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		SchemasJSONParser schemasJSONParser = new SchemasJSONParser();

		return schemasJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Schemas schemas) {
		if (schemas == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (schemas.getItemsPerPage() == null) {
			map.put("itemsPerPage", null);
		}
		else {
			map.put("itemsPerPage", String.valueOf(schemas.getItemsPerPage()));
		}

		if (schemas.getSchemaDefinitions() == null) {
			map.put("schemaDefinitions", null);
		}
		else {
			map.put(
				"schemaDefinitions",
				String.valueOf(schemas.getSchemaDefinitions()));
		}

		if (schemas.getStartIndex() == null) {
			map.put("startIndex", null);
		}
		else {
			map.put("startIndex", String.valueOf(schemas.getStartIndex()));
		}

		if (schemas.getTotalResults() == null) {
			map.put("totalResults", null);
		}
		else {
			map.put("totalResults", String.valueOf(schemas.getTotalResults()));
		}

		return map;
	}

	public static class SchemasJSONParser extends BaseJSONParser<Schemas> {

		@Override
		protected Schemas createDTO() {
			return new Schemas();
		}

		@Override
		protected Schemas[] createDTOArray(int size) {
			return new Schemas[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "itemsPerPage")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "schemaDefinitions")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "startIndex")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "totalResults")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			Schemas schemas, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "itemsPerPage")) {
				if (jsonParserFieldValue != null) {
					schemas.setItemsPerPage(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "schemaDefinitions")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					SchemaDefinition[] schemaDefinitionsArray =
						new SchemaDefinition[jsonParserFieldValues.length];

					for (int i = 0; i < schemaDefinitionsArray.length; i++) {
						schemaDefinitionsArray[i] =
							SchemaDefinitionSerDes.toDTO(
								(String)jsonParserFieldValues[i]);
					}

					schemas.setSchemaDefinitions(schemaDefinitionsArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "startIndex")) {
				if (jsonParserFieldValue != null) {
					schemas.setStartIndex(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "totalResults")) {
				if (jsonParserFieldValue != null) {
					schemas.setTotalResults(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}