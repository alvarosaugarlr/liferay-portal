/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.configure.web.internal.portlet.action;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.oauth.client.LocalOAuthClient;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.liferay.scim.client.internal.configuration.SCIMClientOAuth2ApplicationConfiguration;
import com.liferay.scim.configure.web.internal.configuration.SCIMConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;

/**
 * @author José Abelenda
 */
@Component(
	property = {
		"javax.portlet.name=" + ConfigurationAdminPortletKeys.INSTANCE_SETTINGS,
		"mvc.command.name=/scim/save_scim_configuration"
	},
	service = MVCActionCommand.class
)
public class SaveCompanyConfigurationMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {
System.out.println("BUENNNNNNNOOOOOOOOOOOOOOOOOOOOOOOOOOOO");


		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);
System.out.println(cmd);
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin(themeDisplay.getCompanyId())) {
			SessionErrors.add(actionRequest, PrincipalException.class);

			actionResponse.setRenderParameter("mvcPath", "/error.jsp");

			return;
		}

		_configurationProvider.saveSystemConfiguration(
			SCIMClientOAuth2ApplicationConfiguration.class,
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId",
				themeDisplay.getCompanyId()
			).put(
				"applicationName", ParamUtil.getString(actionRequest, "applicationName")
			).put(
				"matcherField", ParamUtil.getString(actionRequest, "matcherField")
			).build());


		_configurationProvider.saveSystemConfiguration(
			SCIMConfiguration.class,
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled",
				ParamUtil.getString(actionRequest, "enabled")
			).put(
				"applicationName", ParamUtil.getString(actionRequest, "applicationName")
			).put(
				"matcherField", ParamUtil.getString(actionRequest, "matcherField")
			).build());


		//Generate a new token
		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.getOAuth2Application(themeDisplay.getCompanyId(),
				"SCIM_appname");
		String token = _localOAuthClient.requestTokens(oAuth2Application, oAuth2Application.getUserId());

		//Control the expiration
/*		OAuth2Authorization oAuth2Authorization =
			_oAuth2AuthorizationLocalService.getOAuth2AuthorizationByAccessTokenContent(
				"ee");
		oAuth2Authorization.getAccessTokenExpirationDate();

		oAuth2Authorization.setAccessTokenExpirationDate(new Date());

*/


	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private LocalOAuthClient _localOAuthClient;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private OAuth2AuthorizationLocalService _oAuth2AuthorizationLocalService;

}