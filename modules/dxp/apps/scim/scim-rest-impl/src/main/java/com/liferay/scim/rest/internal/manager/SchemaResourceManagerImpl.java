/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.internal.manager;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.URLUtil;

import java.io.IOException;

import java.net.URL;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.ConflictException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.exceptions.NotImplementedException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.protocol.ResponseCodeConstants;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.protocol.endpoints.AbstractResourceManager;
import org.wso2.charon3.core.protocol.endpoints.SchemaResourceManager;
import org.wso2.charon3.core.schema.SCIMConstants;

/**
 * @author Alvaro Saugar
 */
public class SchemaResourceManagerImpl extends SchemaResourceManager {

	@Override
	public SCIMResponse get(
		String id, UserManager userManager, String attributes,
		String excludeAttributes) {

		try {
			userManager.getCoreSchema();

			return new SCIMResponse(
				ResponseCodeConstants.CODE_OK, getSchemas(),
				getResponseHeaders());
		}
		catch (BadRequestException | CharonException | NotFoundException |
			   NotImplementedException e) {

			return AbstractResourceManager.encodeSCIMException(e);
		}
		catch (IOException | JSONException exceptione) {
			return new SCIMResponse(
				ResponseCodeConstants.CODE_INTERNAL_ERROR,
				"Error getting the schemas", null);
		}
		catch (Exception exception) {
			if (exception instanceof ConflictException) {
				return AbstractResourceManager.encodeSCIMException(
					(ConflictException)exception);
			}

			throw exception;
		}
	}

	private JSONObject _createSchema(String jsonFile)
		throws IOException, JSONException {

		Bundle bundle = FrameworkUtil.getBundle(
			SchemaResourceManagerImpl.class);

		URL urlUserSchemaJson = bundle.getResource(
			"META-INF/schemas/json/" + jsonFile);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			URLUtil.toString(urlUserSchemaJson));

		JSONArray schemas = JSONFactoryUtil.createJSONArray();

		schemas.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
		jsonObject.put("schemas", schemas);

		return jsonObject;
	}

	private Map<String, String> getResponseHeaders() throws NotFoundException {
		return HashMapBuilder.put(
			SCIMConstants.CONTENT_TYPE_HEADER, SCIMConstants.APPLICATION_JSON
		).put(
			SCIMConstants.LOCATION_HEADER,
			getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT)
		).build();
	}

	private String getSchemas() throws IOException, JSONException {
		JSONArray schemas = JSONFactoryUtil.createJSONArray(
			"urn:ietf:params:scim:api:messages:2.0:ListResponse");

		JSONObject root = JSONFactoryUtil.createJSONObject(
			HashMapBuilder.put(
				"itemsPerPage", 3
			).put(
				"startIndex", 1
			).put(
				"totalResults", 3
			).build());

		root.put("schemas", schemas);

		JSONArray resources = JSONFactoryUtil.createJSONArray();

		resources.put(_createSchema("user-schema.json"));
		resources.put(_createSchema("user-extension-schema.json"));
		resources.put(_createSchema("group-schema.json"));

		root.put("Resources", resources);

		return root.toString();
	}

}