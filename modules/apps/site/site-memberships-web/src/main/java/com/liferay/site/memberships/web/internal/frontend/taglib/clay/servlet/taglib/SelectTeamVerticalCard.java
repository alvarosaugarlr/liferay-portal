/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.memberships.web.internal.frontend.taglib.clay.servlet.taglib;

import com.liferay.frontend.taglib.clay.servlet.taglib.VerticalCard;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.Team;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Eudaldo Alonso
 */
public class SelectTeamVerticalCard implements VerticalCard {

	public SelectTeamVerticalCard(Team team, RenderRequest renderRequest) {
		_team = team;

		_httpServletRequest = PortalUtil.getHttpServletRequest(renderRequest);
	}

	@Override
	public String getCssClass() {
		return "selector-button";
	}

	@Override
	public Map<String, String> getDynamicAttributes() {
		return HashMapBuilder.put(
			"data-id", String.valueOf(_team.getTeamId())
		).build();
	}

	@Override
	public String getIcon() {
		return "users";
	}

	@Override
	public String getSubtitle() {
		return LanguageUtil.get(_httpServletRequest, _team.getDescription());
	}

	@Override
	public String getTitle() {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return _team.getName();
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	private final HttpServletRequest _httpServletRequest;
	private final Team _team;

}