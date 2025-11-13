/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.service.permission;

import com.liferay.exportimport.kernel.staging.permission.StagingPermissionUtil;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.model.impl.VirtualLayout;
import com.liferay.portal.kernel.portlet.ControlPanelEntry;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.util.PortletCategoryKeys;
import com.liferay.portal.kernel.util.StringUtil;

import jakarta.portlet.PortletMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 */
public class PortletPermissionUtil {

	public static void check(
			PermissionChecker permissionChecker, Layout layout,
			String portletId, String actionId)
		throws PortalException {

		if (!contains(
				permissionChecker, 0, layout, portletId, actionId,
				_STRICT_DEFAULT)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, Layout layout,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		if (!contains(
				permissionChecker, 0, layout, portletId, actionId, strict)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId)
		throws PortalException {

		if (!contains(
				permissionChecker, groupId, layout, portletId, actionId,
				_STRICT_DEFAULT)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		check(
			permissionChecker, groupId, layout, portletId, actionId, strict,
			_CHECK_STAGING_PERMISSION_DEFAULT);
	}

	public static void check(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId, boolean strict,
			boolean checkStagingPermission)
		throws PortalException {

		if (!contains(
				permissionChecker, groupId, layout, portletId, actionId, strict,
				checkStagingPermission)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, long groupId, long plid,
			String portletId, String actionId)
		throws PortalException {

		check(
			permissionChecker, groupId, plid, portletId, actionId,
			_STRICT_DEFAULT);
	}

	public static void check(
			PermissionChecker permissionChecker, long groupId, long plid,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		if (!contains(
				permissionChecker, groupId, plid, portletId, actionId,
				strict)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, long plid, String portletId,
			String actionId)
		throws PortalException {

		check(permissionChecker, plid, portletId, actionId, _STRICT_DEFAULT);
	}

	public static void check(
			PermissionChecker permissionChecker, long plid, String portletId,
			String actionId, boolean strict)
		throws PortalException {

		if (!contains(permissionChecker, plid, portletId, actionId, strict)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, String portletId,
			String actionId)
		throws PortalException {

		if (!contains(permissionChecker, portletId, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, Portlet.class.getName(), portletId,
				actionId);
		}
	}

	public static boolean contains(
			PermissionChecker permissionChecker, Layout layout, Portlet portlet,
			String actionId)
		throws PortalException {

		return contains(
			permissionChecker, layout, portlet, actionId, _STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, Layout layout, Portlet portlet,
			String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, 0, layout, portlet, actionId, strict);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, Layout layout,
			String portletId, String actionId)
		throws PortalException {

		return contains(
			permissionChecker, layout, portletId, actionId, _STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, Layout layout,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, 0, layout, portletId, actionId, strict);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			Portlet portlet, String actionId)
		throws PortalException {

		return contains(
			permissionChecker, groupId, layout, portlet, actionId,
			_STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			Portlet portlet, String actionId, boolean strict)
		throws PortalException {

		if (portlet.isUndeployedPortlet()) {
			return false;
		}

		return contains(
			permissionChecker, groupId, layout, portlet, actionId, strict,
			_CHECK_STAGING_PERMISSION_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			Portlet portlet, String actionId, boolean strict,
			boolean checkStagingPermission)
		throws PortalException {
_log.error("***********entrad en contains");
		long plid = -1;
		long layoutMvccVersion = -1;

		if (layout != null) {
			_log.error("+++++layout != null");

			plid = layout.getPlid();
			layoutMvccVersion = layout.getMvccVersion();

			_log.error("+++++plid:"+plid);
			_log.error("+++++layoutMvccVersion:"+layoutMvccVersion);

		}

		Map<Object, Object> permissionChecksMap =
			permissionChecker.getPermissionChecksMap();
		_log.error("+++++permissionChecksMap:"+permissionChecksMap);

		CacheKey cacheKey = new CacheKey(
			groupId, plid, layoutMvccVersion, portlet.getPortletId(),
			portlet.getMvccVersion(), actionId, strict, checkStagingPermission);
		_log.error("+++++cacheKey:"+cacheKey);

		Boolean contains = (Boolean)permissionChecksMap.get(cacheKey);
		_log.error("+++++contains:"+contains);

		if (contains == null) {
			_log.error("+++++dentro de contains:");

			contains = _contains(
				permissionChecker, groupId, layout, portlet, actionId, strict,
				checkStagingPermission);
			_log.error("+++++contains:"+contains);

			permissionChecksMap.put(cacheKey, contains);

		}
		_log.error("+++++contains:"+contains);

		return contains;
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId)
		throws PortalException {

		return contains(
			permissionChecker, groupId, layout, portletId, actionId,
			_STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, groupId, layout, portletId, actionId, strict,
			_CHECK_STAGING_PERMISSION_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String portletId, String actionId, boolean strict,
			boolean checkStagingPermission)
		throws PortalException {

		Portlet portlet = PortletLocalServiceUtil.getPortletById(
			permissionChecker.getCompanyId(), portletId);

		if ((portlet == null) || portlet.isUndeployedPortlet()) {
			return false;
		}

		return contains(
			permissionChecker, groupId, layout, portlet, actionId, strict,
			checkStagingPermission);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long groupId, long plid,
			String portletId, String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, groupId,
			LayoutLocalServiceUtil.fetchLayout(plid), portletId, actionId,
			strict);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long plid, Portlet portlet,
			String actionId)
		throws PortalException {

		return contains(
			permissionChecker, LayoutLocalServiceUtil.fetchLayout(plid),
			portlet, actionId, _STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long plid, Portlet portlet,
			String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, 0, LayoutLocalServiceUtil.fetchLayout(plid),
			portlet, actionId, strict);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long plid, String portletId,
			String actionId)
		throws PortalException {

		return contains(
			permissionChecker, LayoutLocalServiceUtil.fetchLayout(plid),
			portletId, actionId, _STRICT_DEFAULT);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long plid, String portletId,
			String actionId, boolean strict)
		throws PortalException {

		return contains(
			permissionChecker, 0, LayoutLocalServiceUtil.fetchLayout(plid),
			portletId, actionId, strict);
	}

	public static boolean contains(
			PermissionChecker permissionChecker, String portletId,
			String actionId)
		throws PortalException {

		return contains(permissionChecker, 0, portletId, actionId);
	}

	public static String getPrimaryKey(long plid, String portletId) {
		return StringBundler.concat(
			plid, PortletConstants.LAYOUT_SEPARATOR, portletId);
	}

	public static boolean hasAccessPermission(
			PermissionChecker permissionChecker, long scopeGroupId,
			Layout layout, Portlet portlet, PortletMode portletMode)
		throws PortalException {

		if ((layout != null) && layout.isTypeControlPanel()) {
			String category = portlet.getControlPanelEntryCategory();

			if (StringUtil.startsWith(
					category, PortletCategoryKeys.SITE_ADMINISTRATION)) {

				layout = null;
			}
		}

		boolean access = contains(
			permissionChecker, scopeGroupId, layout, portlet, ActionKeys.VIEW);

		if (access && portletMode.equals(PortletMode.EDIT)) {
			access = contains(
				permissionChecker, scopeGroupId, layout, portlet,
				ActionKeys.PREFERENCES);
		}

		return access;
	}

	public static boolean hasConfigurationPermission(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			String actionId)
		throws PortalException {

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		for (Portlet portlet : layoutTypePortlet.getAllPortlets(false)) {
			if (contains(
					permissionChecker, groupId, layout, portlet.getPortletId(),
					actionId) ||
				contains(
					permissionChecker, groupId, null,
					portlet.getRootPortletId(), actionId)) {

				return true;
			}
		}

		return false;
	}

	public static boolean hasControlPanelAccessPermission(
			PermissionChecker permissionChecker, long scopeGroupId,
			Collection<Portlet> portlets)
		throws PortalException {

		for (Portlet portlet : portlets) {
			if (hasControlPanelAccessPermission(
					permissionChecker, scopeGroupId, portlet)) {

				return true;
			}
		}

		return false;
	}

	public static boolean hasControlPanelAccessPermission(
			PermissionChecker permissionChecker, long scopeGroupId,
			Portlet portlet)
		throws PortalException {

		if (portlet == null) {
			return false;
		}

		Group group = GroupLocalServiceUtil.getGroup(scopeGroupId);

		ControlPanelEntry controlPanelEntry =
			portlet.getControlPanelEntryInstance();

		try {
			return controlPanelEntry.hasAccessPermission(
				permissionChecker, group, portlet);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Cannot process control panel access permission",
					exception);
			}

			return false;
		}
	}

	public static boolean hasControlPanelAccessPermission(
			PermissionChecker permissionChecker, long scopeGroupId,
			String portletId)
		throws PortalException {

		Portlet portlet = PortletLocalServiceUtil.getPortletById(portletId);

		return hasControlPanelAccessPermission(
			permissionChecker, scopeGroupId, portlet);
	}

	public static boolean hasLayoutManagerPermission(
		String portletId, String actionId) {

		try {
			portletId = PortletIdCodec.decodePortletName(portletId);

			List<String> layoutManagerActions =
				ResourceActionsUtil.getPortletResourceLayoutManagerActions(
					portletId);

			return layoutManagerActions.contains(actionId);
		}
		catch (Exception exception) {
			_log.error(exception);

			return false;
		}
	}

	private static boolean _contains(
			PermissionChecker permissionChecker, long groupId, Layout layout,
			Portlet portlet, String actionId, boolean strict,
			boolean checkStagingPermission)
		throws PortalException {
		_log.error("******* dentro de _contains");

		String portletId = portlet.getPortletId();
		_log.error("+++++portletId:"+portletId);
		_log.error("+++++layout:"+layout);
		if (layout == null) {

			_log.error("+++++permissionChecker.hasPermission(\n" +
					   "\t\t\t\tgroupId, portletId, portletId, actionId):"+permissionChecker.hasPermission(
				groupId, portletId, portletId, actionId));

			return permissionChecker.hasPermission(
				groupId, portletId, portletId, actionId);
		}

		Group group = null;
		_log.error("+++++group:"+group);

		if (groupId > 0) {
			group = GroupLocalServiceUtil.fetchGroup(groupId);
		}
		_log.error("+++++group:"+group);

		if (group == null) {
			group = layout.getGroup();

			groupId = layout.getGroupId();
		}
		_log.error("+++++groupId:"+groupId);

		_log.error("+++++group.isControlPanel():"+group.isControlPanel());
		_log.error("+++++layout.isTypeControlPanel():"+layout.isTypeControlPanel());
		_log.error("+++++actionId.equals(ActionKeys.VIEW):"+actionId.equals(ActionKeys.VIEW));

		if ((group.isControlPanel() || layout.isTypeControlPanel()) &&
			actionId.equals(ActionKeys.VIEW)) {

			return true;
		}
		_log.error("+++++layout instanceof VirtualLayout:"+(layout instanceof VirtualLayout));

		if (layout instanceof VirtualLayout) {
			_log.error("+++++layout.isCustomizable():"+layout.isCustomizable());

			_log.error("+++++!actionId.equals(ActionKeys.VIEW):"+!actionId.equals(ActionKeys.VIEW));

			if (layout.isCustomizable() && !actionId.equals(ActionKeys.VIEW)) {
				_log.error("+++++actionId.equals(ActionKeys.ADD_TO_PAGE):"+actionId.equals(ActionKeys.ADD_TO_PAGE));


				if (actionId.equals(ActionKeys.ADD_TO_PAGE)) {

					_log.error("+++++_hasAddToPagePermission(\n" +
							   "\t\t\t\t\t\tpermissionChecker, layout, portletId):"+_hasAddToPagePermission(
						permissionChecker, layout, portletId));

					return _hasAddToPagePermission(
						permissionChecker, layout, portletId);
				}
				_log.error("+++++_hasCustomizePermission(\n" +
						   "\t\t\t\t\tpermissionChecker, layout, portlet, actionId):"+_hasCustomizePermission(
					permissionChecker, layout, portlet, actionId));

				return _hasCustomizePermission(
					permissionChecker, layout, portlet, actionId);
			}

			VirtualLayout virtualLayout = (VirtualLayout)layout;

			layout = virtualLayout.getSourceLayout();
		}
		_log.error("+++++!group.isLayoutSetPrototype():"+!group.isLayoutSetPrototype());
		_log.error("+++++actionId.equals(ActionKeys.CONFIGURATION:"+actionId.equals(ActionKeys.CONFIGURATION));
		_log.error("+++++(layout instanceof VirtualLayout):"+(layout instanceof VirtualLayout));
		_log.error("+++++!layout.isLayoutUpdateable():"+!layout.isLayoutUpdateable());


		if (!group.isLayoutSetPrototype() &&
			actionId.equals(ActionKeys.CONFIGURATION) &&
			((layout instanceof VirtualLayout) ||
			 !layout.isLayoutUpdateable())) {

			return false;
		}

		String rootPortletId = PortletIdCodec.decodePortletName(portletId);
		_log.error("+++++rootPortletId:"+rootPortletId);

		_log.error("+++++checkStagingPermission:"+checkStagingPermission);


		if (checkStagingPermission) {
			Boolean hasPermission = StagingPermissionUtil.hasPermission(
				permissionChecker, group, rootPortletId, groupId, rootPortletId,
				actionId);
			_log.error("+++++hasPermission:"+hasPermission);
			if (hasPermission != null) {
				return hasPermission.booleanValue();
			}
		}

		String resourcePermissionPrimKey = getPrimaryKey(
			layout.getPlid(), portletId);
		_log.error("+++++resourcePermissionPrimKey:"+resourcePermissionPrimKey);

		_log.error("+++++strict:"+strict);

		if (strict) {
			_log.error("+++++permissionChecker.hasPermission(\n" +
					   "\t\t\t\tgroupId, rootPortletId, resourcePermissionPrimKey, actionId):"+permissionChecker.hasPermission(
				groupId, rootPortletId, resourcePermissionPrimKey, actionId));

			return permissionChecker.hasPermission(
				groupId, rootPortletId, resourcePermissionPrimKey, actionId);
		}

		_log.error("+++++_hasConfigurePermission(\n" +
				   "\t\t\t\tpermissionChecker, layout, portlet, actionId):"+_hasConfigurePermission(
			permissionChecker, layout, portlet, actionId));
		_log.error("+++++_hasCustomizePermission(\n" +
				   "\t\t\t\tpermissionChecker, layout, portlet, actionId):"+_hasCustomizePermission(
			permissionChecker, layout, portlet, actionId));

		if (_hasConfigurePermission(
				permissionChecker, layout, portlet, actionId) ||
			_hasCustomizePermission(
				permissionChecker, layout, portlet, actionId)) {

			return true;
		}
		_log.error("+++++permissionChecker.hasPermission(\n" +
				   "\t\t\tgroup, rootPortletId, resourcePermissionPrimKey, actionId):"+permissionChecker.hasPermission(
			group, rootPortletId, resourcePermissionPrimKey, actionId));

		return permissionChecker.hasPermission(
			group, rootPortletId, resourcePermissionPrimKey, actionId);
	}

	private static boolean _hasAddToPagePermission(
			PermissionChecker permissionChecker, Layout layout,
			String portletId)
		throws PortalException {

		if (LayoutPermissionUtil.contains(
				permissionChecker, layout, ActionKeys.CUSTOMIZE)) {

			return contains(
				permissionChecker, portletId, ActionKeys.ADD_TO_PAGE);
		}

		return false;
	}

	private static boolean _hasConfigurePermission(
			PermissionChecker permissionChecker, Layout layout, Portlet portlet,
			String actionId)
		throws PortalException {

		if (!actionId.equals(ActionKeys.CONFIGURATION) &&
			!actionId.equals(ActionKeys.PREFERENCES) &&
			!actionId.equals(ActionKeys.GUEST_PREFERENCES)) {

			return false;
		}

		if (portlet.isPreferencesUniquePerLayout() &&
			(layout.isTypeEmbedded() || layout.isTypePanel() ||
			 layout.isTypePortlet())) {

			return LayoutPermissionUtil.contains(
				permissionChecker, layout, ActionKeys.CONFIGURE_PORTLETS);
		}

		return GroupPermissionUtil.contains(
			permissionChecker, layout.getGroupId(),
			ActionKeys.CONFIGURE_PORTLETS);
	}

	private static boolean _hasCustomizePermission(
			PermissionChecker permissionChecker, Layout layout, Portlet portlet,
			String actionId)
		throws PortalException {

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		if (layoutTypePortlet.isCustomizedView() &&
			layoutTypePortlet.isPortletCustomizable(portlet.getPortletId()) &&
			LayoutPermissionUtil.contains(
				permissionChecker, layout, ActionKeys.CUSTOMIZE)) {

			if (actionId.equals(ActionKeys.VIEW)) {
				return true;
			}
			else if (actionId.equals(ActionKeys.CONFIGURATION)) {
				if (portlet.isInstanceable() ||
					portlet.isPreferencesUniquePerLayout()) {

					return true;
				}
			}
		}

		return false;
	}

	private static final boolean _CHECK_STAGING_PERMISSION_DEFAULT = true;

	private static final boolean _STRICT_DEFAULT = false;

	private static final Log _log = LogFactoryUtil.getLog(
		PortletPermissionUtil.class);

	private static class CacheKey {

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof CacheKey)) {
				return false;
			}

			CacheKey cacheKey = (CacheKey)object;

			if ((_groupId == cacheKey._groupId) && (_plid == cacheKey._plid) &&
				(_layoutMvccVersion == cacheKey._layoutMvccVersion) &&
				Objects.equals(_portletId, cacheKey._portletId) &&
				(_portletMvccVersion == cacheKey._portletMvccVersion) &&
				Objects.equals(_actionId, cacheKey._actionId) &&
				(_strict == cacheKey._strict) &&
				(_checkStagingPermission == cacheKey._checkStagingPermission)) {

				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			int hash = HashUtil.hash(0, _groupId);

			hash = HashUtil.hash(hash, _plid);
			hash = HashUtil.hash(hash, _layoutMvccVersion);
			hash = HashUtil.hash(hash, _portletId);
			hash = HashUtil.hash(hash, _portletMvccVersion);
			hash = HashUtil.hash(hash, _actionId);
			hash = HashUtil.hash(hash, _strict);

			return HashUtil.hash(hash, _checkStagingPermission);
		}

		private CacheKey(
			long groupId, long plid, long layoutMvccVersion, String portletId,
			long portletMvccVersion, String actionId, boolean strict,
			boolean checkStagingPermission) {

			_groupId = groupId;
			_plid = plid;
			_layoutMvccVersion = layoutMvccVersion;
			_portletId = portletId;
			_portletMvccVersion = portletMvccVersion;
			_actionId = actionId;
			_strict = strict;
			_checkStagingPermission = checkStagingPermission;
		}

		private final String _actionId;
		private final boolean _checkStagingPermission;
		private final long _groupId;
		private final long _layoutMvccVersion;
		private final long _plid;
		private final String _portletId;
		private final long _portletMvccVersion;
		private final boolean _strict;

	}

}