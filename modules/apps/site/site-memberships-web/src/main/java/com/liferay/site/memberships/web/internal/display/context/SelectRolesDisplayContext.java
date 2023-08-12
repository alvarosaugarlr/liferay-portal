/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.memberships.web.internal.display.context;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.Team;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.SearchDisplayStyleUtil;
import com.liferay.portal.kernel.portlet.SearchOrderByUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.TeamLocalServiceUtil;
import com.liferay.portal.kernel.service.TeamServiceUtil;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.RolePermissionUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.roles.admin.search.RoleSearch;
import com.liferay.roles.admin.search.RoleSearchTerms;
import com.liferay.site.memberships.constants.SiteMembershipsPortletKeys;
import com.liferay.site.memberships.web.internal.util.DepotRolesUtil;
import com.liferay.users.admin.kernel.util.UsersAdmin;
import com.liferay.users.admin.kernel.util.UsersAdminUtil;
import com.liferay.site.teams.web.internal.search.TeamSearch;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class SelectRolesDisplayContext {

	public SelectRolesDisplayContext(
		HttpServletRequest httpServletRequest, RenderRequest renderRequest,
		RenderResponse renderResponse) {

		_httpServletRequest = httpServletRequest;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
	}

	public String getDisplayStyle() {
		if (Validator.isNotNull(_displayStyle)) {
			return _displayStyle;
		}

		_displayStyle = SearchDisplayStyleUtil.getDisplayStyle(
			_httpServletRequest,
			SiteMembershipsPortletKeys.SITE_MEMBERSHIPS_ADMIN,
			"display-style-roles", "icon");

		return _displayStyle;
	}

	public String getEventName() {
		if (Validator.isNotNull(_eventName)) {
			return _eventName;
		}

		_eventName = ParamUtil.getString(
			_httpServletRequest, "eventName",
			_renderResponse.getNamespace() + "selectRole");

		return _eventName;
	}

	public long getGroupId() {
		if (_groupId != null) {
			return _groupId;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		_groupId = ParamUtil.getLong(
			_httpServletRequest, "groupId",
			themeDisplay.getSiteGroupIdOrLiveGroupId());

		return _groupId;
	}

	public String getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_renderRequest, "keywords");

		return _keywords;
	}

	public String getOrderByCol() {
		if (Validator.isNotNull(_orderByCol)) {
			return _orderByCol;
		}

		_orderByCol = SearchOrderByUtil.getOrderByCol(
			_httpServletRequest,
			SiteMembershipsPortletKeys.SITE_MEMBERSHIPS_ADMIN,
			"order-by-col-roles", "title");

		return _orderByCol;
	}

	public String getOrderByType() {
		if (Validator.isNotNull(_orderByType)) {
			return _orderByType;
		}

		_orderByType = SearchOrderByUtil.getOrderByType(
			_httpServletRequest,
			SiteMembershipsPortletKeys.SITE_MEMBERSHIPS_ADMIN,
			"order-by-type-roles", "asc");

		return _orderByType;
	}

	public PortletURL getPortletURL() {
		return PortletURLBuilder.createRenderURL(
			_renderResponse
		).setMVCPath(
			"/select_site_role.jsp"
		).setKeywords(
			() -> {
				String keywords = getKeywords();

				if (Validator.isNotNull(keywords)) {
					return keywords;
				}

				return null;
			}
		).setParameter(
			"displayStyle",
			() -> {
				String displayStyle = getDisplayStyle();

				if (Validator.isNotNull(displayStyle)) {
					return displayStyle;
				}

				return null;
			}
		).setParameter(
			"eventName", getEventName()
		).setParameter(
			"groupId", getGroupId()
		).setParameter(
			"orderByCol",
			() -> {
				String orderByCol = getOrderByCol();

				if (Validator.isNotNull(orderByCol)) {
					return orderByCol;
				}

				return null;
			}
		).setParameter(
			"orderByType",
			() -> {
				String orderByType = getOrderByType();

				if (Validator.isNotNull(orderByType)) {
					return orderByType;
				}

				return null;
			}
		).setParameter(
			"roleType", getRoleType()
		).buildPortletURL();
	}

	public SearchContainer<Role> getRoleSearchSearchContainer()
		throws PortalException {

		if (_roleSearch != null) {
			return _roleSearch;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		RoleSearch roleSearch = new RoleSearch(_renderRequest, getPortletURL());

		RoleSearchTerms searchTerms =
			(RoleSearchTerms)roleSearch.getSearchTerms();

		Set<String> names = new TreeSet<String>();
		names.addAll(ListUtil.toList(RoleLocalServiceUtil.getUserGroupGroupRoles(themeDisplay.getUserId(), getGroupId()), Role.TITLE_ACCESSOR));

		names.addAll(ListUtil.toList(UserGroupRoleLocalServiceUtil.getUserGroupRoles(themeDisplay.getUserId(), getGroupId()), UsersAdmin.USER_GROUP_ROLE_TITLE_ACCESSOR));

		List<Team> teams = TeamLocalServiceUtil.getUserOrUserGroupTeams(getGroupId(), themeDisplay.getUserId());

		names.addAll(ListUtil.toList(teams, Team.NAME_ACCESSOR));


		List<Role> roles = RoleLocalServiceUtil.search(
			themeDisplay.getCompanyId(), searchTerms.getKeywords(),
			new Integer[] {getRoleType()}, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			roleSearch.getOrderByComparator());

		Group group = GroupLocalServiceUtil.fetchGroup(getGroupId());

		if (group.isDepot()) {
			roles = DepotRolesUtil.filterGroupRoles(
				themeDisplay.getPermissionChecker(), getGroupId(), roles);
		}
		else {
			roles = UsersAdminUtil.filterGroupRoles(
				themeDisplay.getPermissionChecker(), getGroupId(), roles);
		}

	/*	String resourceName = GetterUtil.getString(
			(String)themeDisplay.getAttribute(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME),
			(String)workflowContext.get(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME));
*/
		List<Role> filteredGroupRoles = ListUtil.copy(roles);
		boolean fo = false;
		for (Role bo:roles){
			 fo = RolePermissionUtil.contains(themeDisplay.getPermissionChecker(), getGroupId(), bo.getRoleId(), ActionKeys.VIEW);
			 if (!fo) {
				 filteredGroupRoles.remove(bo);
			 }
		}


		roleSearch.setResultsAndTotal(roles);

		_roleSearch = roleSearch;

		return _roleSearch;
	}


	public SearchContainer<Team> getTeamsSearchSearchContainer()
		throws PortalException {

	/*	if (_roleSearch != null) {
			return _roleSearch;
		}
*/
		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		RoleSearch roleSearch = new RoleSearch(_renderRequest, getPortletURL());

		RoleSearchTerms searchTerms =
			(RoleSearchTerms)roleSearch.getSearchTerms();

		Set<String> names = new TreeSet<String>();
		names.addAll(ListUtil.toList(RoleLocalServiceUtil.getUserGroupGroupRoles(themeDisplay.getUserId(), getGroupId()), Role.TITLE_ACCESSOR));

		names.addAll(ListUtil.toList(UserGroupRoleLocalServiceUtil.getUserGroupRoles(themeDisplay.getUserId(), getGroupId()), UsersAdmin.USER_GROUP_ROLE_TITLE_ACCESSOR));

		List<Team> teams = TeamLocalServiceUtil.getUserOrUserGroupTeams(getGroupId(), themeDisplay.getUserId());

		names.addAll(ListUtil.toList(teams, Team.NAME_ACCESSOR));


		List<Role> roles = RoleLocalServiceUtil.search(
			themeDisplay.getCompanyId(), searchTerms.getKeywords(),
			new Integer[] {getRoleType()}, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			roleSearch.getOrderByComparator());

		Group group = GroupLocalServiceUtil.fetchGroup(getGroupId());

		if (group.isDepot()) {
			roles = DepotRolesUtil.filterGroupRoles(
				themeDisplay.getPermissionChecker(), getGroupId(), roles);
		}
		else {
			roles = UsersAdminUtil.filterGroupRoles(
				themeDisplay.getPermissionChecker(), getGroupId(), roles);
		}

	/*	String resourceName = GetterUtil.getString(
			(String)themeDisplay.getAttribute(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME),
			(String)workflowContext.get(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME));
*/
		List<Role> filteredGroupRoles = ListUtil.copy(roles);
		boolean fo = false;
		for (Role bo:roles){
			fo = RolePermissionUtil.contains(themeDisplay.getPermissionChecker(), getGroupId(), bo.getRoleId(), ActionKeys.VIEW);
			if (!fo) {
				filteredGroupRoles.remove(bo);
			}
		}


		roleSearch.setResultsAndTotal(roles);

		_roleSearch = roleSearch;


		TeamSearch teamSearch = new TeamSearch(_renderRequest, getPortletURL());


		List<Team> teimas = TeamServiceUtil.getGroupTeams(getGroupId());

		teamSearch.setResultsAndTotal(teimas);

		_teamSearch = teamSearch;



		return _teamSearch;


		//return _roleSearch;
	}


	public int getRoleType() {
		if (_roleType != null) {
			return _roleType;
		}

		_roleType = ParamUtil.getInteger(
			_httpServletRequest, "roleType", RoleConstants.TYPE_SITE);

		return _roleType;
	}

	private String _displayStyle;
	private String _eventName;
	private Long _groupId;
	private final HttpServletRequest _httpServletRequest;
	private String _keywords;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private RoleSearch _roleSearch;
	private TeamSearch _teamSearch;
	private Integer _roleType;

}