/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.util;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.OrderByComparator;

import com.liferay.exportimport.attachment.ExportImportAttachmentManager;

import java.io.InputStream;
import java.io.Serializable;

import java.util.List;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.URL;


public class ExportImportAttachmentManagerUtil {

	public static String getFileURL(DLFileEntry dlFileEntry) throws Exception{
		return getService().getFileURL(dlFileEntry);
	}

	public static URL getURL(String url) throws MalformedURLException {
		return getService().getURL(url);
	}


	//public static ExportImportAttachmentManager getService() {
	//	return _service;
	//}

	public static void setService(ExportImportAttachmentManager service) {
		_service = service;
	}

	private static volatile ExportImportAttachmentManager _service;

	public static ExportImportAttachmentManager getService() {
		return _serviceSnapshot.get();
	}

	private static final Snapshot<ExportImportAttachmentManager> _serviceSnapshot =
		new Snapshot<>(
			ExportImportAttachmentManagerUtil.class, ExportImportAttachmentManager.class);


}