/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0;

import com.liferay.client.extension.type.manager.CETManager;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.util.ExportImportAttachmentManagerUtil;
import com.liferay.exportimport.vulcan.batch.engine.ExportImportVulcanBatchEngineTaskItemDelegate;
import com.liferay.headless.admin.site.dto.v1_0.ContentPageSpecification;
import com.liferay.headless.admin.site.dto.v1_0.MasterPage;
import com.liferay.headless.admin.site.dto.v1_0.PageSpecification;
import com.liferay.headless.admin.site.internal.odata.entity.v1_0.MasterPageEntityModel;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.FileEntryUtil;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.GroupUtil;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutUtil;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.PageSpecificationUtil;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.ServiceContextUtil;
import com.liferay.headless.admin.site.resource.v1_0.MasterPageResource;
import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.layout.page.template.constants.LayoutPageTemplateConstants;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.petra.io.StreamUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SearchUtil;

import jakarta.ws.rs.core.MultivaluedMap;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.liferay.headless.admin.site.dto.v1_0.URLReference;
import com.liferay.exportimport.attachment.ExportImportAttachmentManager;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldId;
import com.liferay.portal.kernel.util.ListUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * @author Rubén Pulido
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/master-page.properties",
	property = "export.import.vulcan.batch.engine.task.item.delegate=true",
	scope = ServiceScope.PROTOTYPE, service = MasterPageResource.class
)
public class MasterPageResourceImpl
	extends BaseMasterPageResourceImpl
	implements ExportImportVulcanBatchEngineTaskItemDelegate<MasterPage> {

	@Override
	public void deleteSiteMasterPage(
		String siteExternalReferenceCode,
		String masterPageExternalReferenceCode)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		_layoutPageTemplateEntryService.deleteLayoutPageTemplateEntry(
			masterPageExternalReferenceCode,
			GroupUtil.getGroupId(
				false, contextCompany.getCompanyId(),
				siteExternalReferenceCode));
	}

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public ExportImportDescriptor getExportImportDescriptor() {
		return new ExportImportDescriptor() {

			@Override
			public String getItemClassName() {
				return LayoutPageTemplateEntry.class.getName();
			}

			@Override
			public String getLabel() {
				return "master-pages";
			}

			@Override
			public List<String> getNestedFields() {
				return List.of(
					"friendlyUrlHistory", "pageSpecifications", "thumbnail");
			}

			@Override
			public String getPortletId() {
				return LayoutAdminPortletKeys.GROUP_PAGES;
			}

			@Override
			public Scope getScope() {
				return Scope.SITE;
			}

			@Override
			public boolean isActive(PortletDataContext portletDataContext) {
				return FeatureFlagManagerUtil.isEnabled("LPD-35443");
			}

		};
	}

	@Override
	public ContentPageSpecification postSiteMasterPagePageSpecification(
		String siteExternalReferenceCode,
		String pageTemplateExternalReferenceCode,
		ContentPageSpecification contentPageSpecification)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				fetchLayoutPageTemplateEntryByExternalReferenceCode(
					pageTemplateExternalReferenceCode,
					GroupUtil.getGroupId(
						false, contextCompany.getCompanyId(),
						siteExternalReferenceCode));

		if (!Objects.equals(
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
			layoutPageTemplateEntry.getType())) {

			throw new UnsupportedOperationException();
		}

		return (ContentPageSpecification) _pageSpecificationDTOConverter.toDTO(
			LayoutUtil.addDraftToLayout(
				_cetManager, contentPageSpecification,
				_layoutLocalService.getLayout(
					layoutPageTemplateEntry.getPlid()),
				ServiceContextUtil.createServiceContext(
					layoutPageTemplateEntry.getGroupId(),
					contextHttpServletRequest, contextUser.getUserId())));
	}

	@Override
	protected MasterPage doGetSiteMasterPage(
		String siteExternalReferenceCode,
		String masterPageExternalReferenceCode)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				getLayoutPageTemplateEntryByExternalReferenceCode(
					masterPageExternalReferenceCode,
					GroupUtil.getGroupId(
						true, contextCompany.getCompanyId(),
						siteExternalReferenceCode));

		if (!Objects.equals(
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
			layoutPageTemplateEntry.getType())) {

			throw new UnsupportedOperationException();
		}

		return _masterPageDTOConverter.toDTO(layoutPageTemplateEntry);
	}

	@Override
	protected Page<MasterPage> doGetSiteMasterPagesPage(
		String siteExternalReferenceCode, String search,
		Aggregation aggregation, Filter filter, Pagination pagination,
		Sort[] sorts)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		long groupId = GroupUtil.getGroupId(
			true, contextCompany.getCompanyId(), siteExternalReferenceCode);

		return SearchUtil.search(
			Collections.emptyMap(),
			booleanQuery -> {
			},
			filter, LayoutPageTemplateEntry.class.getName(), search, pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.setAttribute(
					"types",
					new String[]{
						String.valueOf(
							LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT)
					});
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(new long[]{groupId});
			},
			sorts,
			document -> _masterPageDTOConverter.toDTO(
				_layoutPageTemplateEntryService.fetchLayoutPageTemplateEntry(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))));
	}

	@NestedField(parentClass = MasterPage.class, value = "thumbnail")
	public Page<URLReference> doGetSiteMasterPagesThumbnail(
		String siteExternalReferenceCode,
		@NestedFieldId(value = "externalReferenceCode") String
			masterPageExternalReferenceCode)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				getLayoutPageTemplateEntryByExternalReferenceCode(
					masterPageExternalReferenceCode,
					GroupUtil.getGroupId(
						true, contextCompany.getCompanyId(),
						siteExternalReferenceCode));
/*
		if (!Objects.equals(
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
			layoutPageTemplateEntry.getType())) {

			throw new UnsupportedOperationException();
		}


 */
		return Page.of(
			_getPortletFileEntryURLReference(
				layoutPageTemplateEntry.getPreviewFileEntryId()));
	}

	private List<URLReference>
	_getPortletFileEntryURLReference(long fileEntryId)
		throws Exception {




	/*	URLReference urlReference new URLReference() {
			{
				setUrl(() -> _exportImportAttachmentManager.getFileURL(dlFileEntry));
			}
		};


	 */
		return ListUtil.fromArray(_toDTO(fileEntryId)
		);

	}


	public URLReference _toDTO(long fileEntryId)
		throws Exception {

		if (fileEntryId <= 0) {
			return null;
		}

		DLFileEntry dlFileEntry =
			_dlFileEntryLocalService.getFileEntry(fileEntryId);

		return new URLReference() {
			{
				setUrl(() -> _exportImportAttachmentManager.getFileURL(
					dlFileEntry));
			}
		};

	}

	/*
		private URLReference _toDTO(
			DTOConverterContext dtoConverterContext,
			LayoutPageTemplateEntry layoutPageTemplateEntry)
			throws Exception {

			Layout layout = _layoutLocalService.getLayout(
				layoutPageTemplateEntry.getPlid());

			return new URLReference() {
				{
					setThumbnail(
						() ->
							_getPortletFileEntryURLReference(
								layoutPageTemplateEntry.getPreviewFileEntryId()));
				}
			};
		}


	 */
	@Override
	protected MasterPage doPostSiteMasterPage(
		String siteExternalReferenceCode, MasterPage masterPage)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		return _addMasterPage(
			GroupUtil.getGroupId(
				false, contextCompany.getCompanyId(),
				siteExternalReferenceCode),
			masterPage);
	}

	@Override
	protected MasterPage doPutSiteMasterPage(
		String siteExternalReferenceCode,
		String masterPageExternalReferenceCode, MasterPage masterPage)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-35443")) {
			throw new UnsupportedOperationException();
		}

		long groupId = GroupUtil.getGroupId(
			false, contextCompany.getCompanyId(), siteExternalReferenceCode);
		URL uu = ExportImportAttachmentManagerUtil.getURL(
			masterPage.getThumbnail().getUrl());
		System.out.println(uu.toString());
/*

		URL url = _exportImportAttachmentManager.getURL(masterPage.getThumbnail().getUrl());



		URLConnection urlConnection = url.openConnection();

		if ((urlConnection instanceof
			HttpURLConnection httpURLConnection) &&
			(httpURLConnection.getResponseCode() !=
			 HttpURLConnection.HTTP_OK)) {


		}

		byte[] bytee = StreamUtil.toByteArray(
			urlConnection.getInputStream());

		Path rutaGuardado = Paths.get("/home/me/Downloads/imagen_guardada.png"); // Reemplaza la ruta y nombre del archivo
		try {
			Files.write(rutaGuardado, bytee);
			System.out.println("Imagen guardada correctamente.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		/// ///////////////////////////////////


		//long userId = // user que crea el archivo ;
		//long groupId = // site groupId, suele ser repositoryId ;
		long repositoryId = groupId;
		long folderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID; // o tu carpeta
		String fileName = "imagen.png";
		String title = "Miniatura";
		String description = "Subido desde bytes";
		String changeLog = "";
		//String mimeType = MimeTypesUtil.getContentType(fileName); // o calcula por contenido

		//byte[] bytes = // tu array ;

		ServiceContext sc = new ServiceContext();
		sc.setScopeGroupId(groupId);
// Permisos (ajusta según tu caso):
		sc.setAddGroupPermissions(true);
		sc.setAddGuestPermissions(false);

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(siteExternalReferenceCode, contextUser.getUserId(), repositoryId, folderId, getResourceName(), null, bytee, null, null, null, null);

*/

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				fetchLayoutPageTemplateEntryByExternalReferenceCode(
					masterPageExternalReferenceCode, groupId);

		if (layoutPageTemplateEntry == null) {
			return _addMasterPage(groupId, masterPage);
		}


		URL url = _exportImportAttachmentManager.getURL(
			masterPage.getThumbnail().getUrl());


		URLConnection urlConnection = url.openConnection();

		if ((urlConnection instanceof
			HttpURLConnection httpURLConnection) &&
			(httpURLConnection.getResponseCode() !=
			 HttpURLConnection.HTTP_OK)) {


		}

		byte[] bytee = StreamUtil.toByteArray(
			urlConnection.getInputStream());

		/// ///////////////////////////////////


		long repositoryId = groupId;
		long folderId =
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID; // o tu carpeta
		String fileName = "imagen.png";
		String title = "Miniatura";
		String description = "Subido desde bytes";
		String changeLog = "";
		//String mimeType = MimeTypesUtil.getContentType(fileName); // o calcula por contenido


		ServiceContext sc = new ServiceContext();
		sc.setScopeGroupId(groupId);
// Permisos (ajusta según tu caso):
		sc.setAddGroupPermissions(true);
		sc.setAddGuestPermissions(false);

		FileEntry fileEntry =
			_dlAppLocalService.addFileEntry(
				siteExternalReferenceCode,
				contextUser.getUserId(), repositoryId, folderId,
				getResourceName(), null, bytee, null, null, null, null);

		/*
		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			contextUser.getUserId(), repositoryId, folderId,
			fileName, null, title, description, changeLog,
			bytee, sc);


		 */


		/// /////////////////////


		//	long previewFileEntryId = FileEntryUtil.getPreviewFileEntryId(
		//		groupId, masterPage.getThumbnail());
		long previewFileEntryId = fileEntry.getFileEntryId();

		if (previewFileEntryId !=
			layoutPageTemplateEntry.getPreviewFileEntryId()) {

			layoutPageTemplateEntry =
				_layoutPageTemplateEntryService.updateLayoutPageTemplateEntry(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
					previewFileEntryId);
		}

		Layout layout = _layoutLocalService.getLayout(
			layoutPageTemplateEntry.getPlid());

		ServiceContext serviceContext = _getServiceContext(groupId, masterPage);

		layout = LayoutUtil.updateContentLayout(
			_cetManager, layout, layout.getNameMap(), layout.getTitleMap(),
			layout.getDescriptionMap(), layout.getRobotsMap(),
			layout.getFriendlyURLMap(), masterPage.getPageSpecifications(),
			serviceContext);

		if (!layoutPageTemplateEntry.isApproved() && layout.isPublished()) {
			layoutPageTemplateEntry =
				_layoutPageTemplateEntryService.updateStatus(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
					WorkflowConstants.STATUS_APPROVED);
		}

		if (!Objects.equals(
			GetterUtil.getBoolean(masterPage.getMarkedAsDefault()),
			layoutPageTemplateEntry.isDefaultTemplate())) {

			layoutPageTemplateEntry =
				_layoutPageTemplateEntryService.updateLayoutPageTemplateEntry(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
					GetterUtil.getBoolean(masterPage.getMarkedAsDefault()));
		}

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try {
			return _masterPageDTOConverter.toDTO(
				_layoutPageTemplateEntryService.updateLayoutPageTemplateEntry(
					layoutPageTemplateEntry.getLayoutPageTemplateEntryId(),
					masterPage.getName()));
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	@Override
	protected Long getPermissionCheckerResourceId(
		String groupExternalReferenceCode, String externalReferenceCode)
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.
				getLayoutPageTemplateEntryByExternalReferenceCode(
					externalReferenceCode,
					getPermissionCheckerGroupId(groupExternalReferenceCode));

		return layoutPageTemplateEntry.getPrimaryKey();
	}

	@Override
	protected String getPermissionCheckerResourceName(
		String groupExternalReferenceCode, String externalReferenceCode)
		throws Exception {

		return LayoutPageTemplateEntry.class.getName();
	}

	@Override
	protected void preparePatch(
		MasterPage masterPage, MasterPage existingMasterPage) {

		if (masterPage.getKeywords() != null) {
			existingMasterPage.setKeywords(masterPage::getKeywords);
		}

		if (masterPage.getPageSpecifications() != null) {
			existingMasterPage.setPageSpecifications(
				masterPage::getPageSpecifications);
		}

		if (masterPage.getTaxonomyCategoryItemExternalReferences() != null) {
			existingMasterPage.setTaxonomyCategoryItemExternalReferences(
				masterPage::getTaxonomyCategoryItemExternalReferences);
		}

		if (masterPage.getThumbnail() != null) {
			existingMasterPage.setThumbnail(masterPage::getThumbnail);
		}
	}

	private MasterPage _addMasterPage(long groupId, MasterPage masterPage)
		throws Exception {

		boolean defaultTemplate = false;

		if (GetterUtil.getBoolean(masterPage.getMarkedAsDefault())) {
			defaultTemplate = true;
		}

		ServiceContext serviceContext = _getServiceContext(groupId, masterPage);


		URL url = _exportImportAttachmentManager.getURL(
			masterPage.getThumbnail().getUrl());


		URLConnection urlConnection = url.openConnection();

		if ((urlConnection instanceof
			HttpURLConnection httpURLConnection) &&
			(httpURLConnection.getResponseCode() !=
			 HttpURLConnection.HTTP_OK)) {


		}

		byte[] bytee = StreamUtil.toByteArray(
			urlConnection.getInputStream());

		/// ///////////////////////////////////


		long repositoryId = groupId;
		long folderId =
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID; // o tu carpeta
		String fileName = "imagen.png";
		String title = "Miniatura";
		String description = "Subido desde bytes";
		String changeLog = "";
		//String mimeType = MimeTypesUtil.getContentType(fileName); // o calcula por contenido


		ServiceContext sc = new ServiceContext();
		sc.setScopeGroupId(groupId);
// Permisos (ajusta según tu caso):
		sc.setAddGroupPermissions(true);
		sc.setAddGuestPermissions(false);

		FileEntry fileEntry =
			_dlAppLocalService.addFileEntry(
				masterPage.getExternalReferenceCode(),
				contextUser.getUserId(), repositoryId, folderId,
				getResourceName(), null, bytee, null, null, null, serviceContext);




		return _masterPageDTOConverter.toDTO(
			_layoutPageTemplateEntryService.addLayoutPageTemplateEntry(
				masterPage.getExternalReferenceCode(), groupId,
				LayoutPageTemplateConstants.
					PARENT_LAYOUT_PAGE_TEMPLATE_COLLECTION_ID_DEFAULT,
				masterPage.getKey(), 0, 0, masterPage.getName(),
				LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT,
				fileEntry.getFileEntryId(),
				defaultTemplate, 0,
				_getLayoutPlid(groupId, masterPage, serviceContext), 0,
				PageSpecificationUtil.getPublishedStatus(
					masterPage.getPageSpecifications()),
				serviceContext));
	}

	private long _getLayoutPlid(
		long groupId, MasterPage masterPage, ServiceContext serviceContext)
		throws Exception {

		Map<Locale, String> nameMap = Collections.singletonMap(
			_portal.getSiteDefaultLocale(groupId), masterPage.getName());

		serviceContext.setAttribute(
			"layout.instanceable.allowed", Boolean.TRUE);
		serviceContext.setAttribute(
			"layout.page.template.entry.type",
			LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT);

		Layout layout = LayoutUtil.addContentLayout(
			_cetManager, groupId, masterPage.getPageSpecifications(),
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, true, nameMap, nameMap,
			nameMap, null, LayoutConstants.TYPE_CONTENT, null, true, true,
			Collections.emptyMap(), WorkflowConstants.STATUS_APPROVED,
			serviceContext);

		return layout.getPlid();
	}

	private ServiceContext _getServiceContext(
		long groupId, MasterPage masterPage)
		throws Exception {

		return ServiceContextUtil.createServiceContext(
			masterPage.getTaxonomyCategoryItemExternalReferences(),
			contextCompany.getCompanyId(), masterPage.getDateCreated(), groupId,
			contextHttpServletRequest, masterPage.getKeywords(),
			masterPage.getDateModified(), contextUser.getUserId(),
			masterPage.getUuid(), null);
	}

	private static final EntityModel _entityModel = new MasterPageEntityModel();

	@Reference
	private CETManager _cetManager;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

	@Reference(
		target = "(component.name=com.liferay.headless.admin.site.internal.dto.v1_0.converter.MasterPageDTOConverter)"
	)
	private DTOConverter<LayoutPageTemplateEntry, MasterPage>
		_masterPageDTOConverter;

	@Reference(
		target = "(component.name=com.liferay.headless.admin.site.internal.dto.v1_0.converter.PageSpecificationDTOConverter)"
	)
	private DTOConverter<Layout, PageSpecification>
		_pageSpecificationDTOConverter;

	@Reference
	private Portal _portal;

	@Reference
	private DLFileEntryLocalService _dlFileEntryLocalService;

	@Reference
	private ExportImportAttachmentManager _exportImportAttachmentManager;

	@Reference
	private com.liferay.document.library.kernel.service.DLAppLocalService
		_dlAppLocalService;

}