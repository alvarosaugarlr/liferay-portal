/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.client.dto.v1_0;

import com.liferay.scim.rest.client.function.UnsafeSupplier;
import com.liferay.scim.rest.client.serdes.v1_0.SchemasSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Olivér Kecskeméty
 * @generated
 */
@Generated("")
public class Schemas implements Cloneable, Serializable {

	public static Schemas toDTO(String json) {
		return SchemasSerDes.toDTO(json);
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public void setItemsPerPage(
		UnsafeSupplier<Integer, Exception> itemsPerPageUnsafeSupplier) {

		try {
			itemsPerPage = itemsPerPageUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer itemsPerPage;

	public SchemaDefinition[] getSchemaDefinitions() {
		return schemaDefinitions;
	}

	public void setSchemaDefinitions(SchemaDefinition[] schemaDefinitions) {
		this.schemaDefinitions = schemaDefinitions;
	}

	public void setSchemaDefinitions(
		UnsafeSupplier<SchemaDefinition[], Exception>
			schemaDefinitionsUnsafeSupplier) {

		try {
			schemaDefinitions = schemaDefinitionsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected SchemaDefinition[] schemaDefinitions;

	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	public void setStartIndex(
		UnsafeSupplier<Integer, Exception> startIndexUnsafeSupplier) {

		try {
			startIndex = startIndexUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer startIndex;

	public Integer getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(Integer totalResults) {
		this.totalResults = totalResults;
	}

	public void setTotalResults(
		UnsafeSupplier<Integer, Exception> totalResultsUnsafeSupplier) {

		try {
			totalResults = totalResultsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer totalResults;

	@Override
	public Schemas clone() throws CloneNotSupportedException {
		return (Schemas)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Schemas)) {
			return false;
		}

		Schemas schemas = (Schemas)object;

		return Objects.equals(toString(), schemas.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return SchemasSerDes.toJSON(this);
	}

}