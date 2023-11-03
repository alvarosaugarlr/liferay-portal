/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.configuration.web.internal.scheduler;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailService;
import com.liferay.oauth2.provider.exception.NoSuchOAuth2ApplicationException;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.notifications.NotificationEvent;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.notifications.UserNotificationManagerUtil;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerConfiguration;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.PrefsProps;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.scim.client.configuration.SCIMClientOAuth2ApplicationConfiguration;
import com.liferay.scim.client.util.SCIMClientUtil;
import com.liferay.scim.configuration.web.internal.constants.SCIMConstants;
import com.liferay.scim.configuration.web.internal.constants.SCIMWebKeys;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.mail.internet.InternetAddress;

/**
 * @author Alvaro Saugar
 */
@Component(
	service = SchedulerJobConfiguration.class
)
public class SCIMNotificationSchedulerJobConfiguration
	implements SchedulerJobConfiguration {




	@Override
	public UnsafeConsumer<Long, Exception>
	getCompanyJobExecutorUnsafeConsumer() {

		System.out.println("entra en getCompanyJobExecutorUnsafeConsumer");
		return null;
		//return companyId -> _updateMetadata(companyId);
	}

	@Override
	public UnsafeRunnable<Exception> getJobExecutorUnsafeRunnable() {

		System.out.println("entra en getJobExecutorUnsafeRunnable ");

		return () -> _companyLocalService.forEachCompany(
			company -> {
				if (!company.isActive()) {
					return;
				}

				_sendNotification(company.getCompanyId());
			});


	}

	@Override
	public TriggerConfiguration getTriggerConfiguration() {
		return TriggerConfiguration.createTriggerConfiguration(
			1, TimeUnit.MINUTE);
		/*return TriggerConfiguration.createTriggerConfiguration(
			_samlConfiguration.getSpMessageCheckInterval(), TimeUnit.MINUTE);

		 */
	}

	@Activate
	protected void activate(Map<String, Object> properties) {

		_scimClientOAuth2ApplicationConfiguration = ConfigurableUtil.createConfigurable(
			SCIMClientOAuth2ApplicationConfiguration.class, properties);
	}


	private void _sendNotification(long companyId) {
		
		String clientId = SCIMClientUtil.generateSCIMClientId(
			_scimClientOAuth2ApplicationConfiguration.applicationName());

		OAuth2Application oAuth2Application = null;
		try {
			oAuth2Application =
				_oAuth2ApplicationLocalService.getOAuth2Application(
					companyId, clientId);

			List<OAuth2Authorization> applicationOAuth2Authorizations =
				null;

			applicationOAuth2Authorizations =
				_oAuth2AuthorizationLocalService.getOAuth2Authorizations(
					oAuth2Application.getOAuth2ApplicationId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			Date accessTokenDate = null;

			boolean hasToSentNotification =
				_hasToSentNotification(applicationOAuth2Authorizations,
					accessTokenDate);


			if (hasToSentNotification) {

				Role role = null;
				role = _roleLocalService.getRole(
					companyId, RoleConstants.ADMINISTRATOR);

				List<User> users =
					_userLocalService.getRoleUsers(role.getRoleId());


				SimpleDateFormat formatter =
					new SimpleDateFormat("dd-MMM-yyyy");

				String strDate = formatter.format(accessTokenDate);

				Company company =
					_companyLocalService.getCompany(companyId);

				ResourceBundle resourceBundle =
					ResourceBundleUtil.getBundle(
						"content.Language", company.getLocale(),
						getClass());
				String subject = _language.get(
					resourceBundle,
					"scim-email-subject");


				String body = _language.get(
					resourceBundle,
					"scim-email-body", strDate);


				_sendNotificationEvent(users, role.getRoleId(), body);
				_sendEmail(companyId, subject, body, users);

			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private boolean _hasToSentNotification(List<OAuth2Authorization> applicationOAuth2Authorizations, Date accessTokenDate) {

		Iterator<OAuth2Authorization> iterator =
			applicationOAuth2Authorizations.iterator();

		boolean sendNotification = false;

		while (iterator.hasNext()) {
			OAuth2Authorization oAuth2Authorization = iterator.next();

			Date accessTokenExpirationDate =
				oAuth2Authorization.getAccessTokenExpirationDate();

			int daysBetween = DateUtil.getDaysBetween(
				new Date(),
				accessTokenExpirationDate);

			if ((daysBetween == MONTH) || (daysBetween == WEEK) || (daysBetween == DAY)) {
				sendNotification = true;
			} else if (daysBetween > MONTH) {
				return false;
			}
			if (DateUtil.compareTo(accessTokenDate, accessTokenExpirationDate) > 0){
				accessTokenDate = accessTokenExpirationDate;
			}

		}

		return sendNotification;
	}

	private void _sendNotificationEvent(List<User> users, long roleValue, String body)
		 {

			 try {
			 	for (User user: users) {
					 if (UserNotificationManagerUtil.isDeliver(
						 user.getUserId(), SCIMWebKeys.SCIM_CONFIGURATION, 0,
						 UserNotificationDefinition.NOTIFICATION_TYPE_ADD_ENTRY,
						 UserNotificationDeliveryConstants.TYPE_WEBSITE)) {

						 NotificationEvent notificationEvent = new NotificationEvent(
							 System.currentTimeMillis(), SCIMWebKeys.SCIM_CONFIGURATION,
							 JSONUtil.put(
								 "body", body
							 ));

						 notificationEvent.setDeliveryType(
							 UserNotificationDeliveryConstants.TYPE_WEBSITE);


						 _userNotificationEventLocalService.addUserNotificationEvent(
							 user.getUserId(), notificationEvent);
						 }
				 }
			 }
			 catch (PortalException e) {
				 throw new RuntimeException(e);
			 }
		 }


	private void _sendEmail(long companyId, String subject, String body, List<User> users) throws Exception {

		String defaultEmailFromAddress = _prefsProps.getString(
			companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String defaultEmailFromName = _prefsProps.getString(
			companyId, PropsKeys.ADMIN_EMAIL_FROM_NAME);

		InternetAddress from = new InternetAddress(
			defaultEmailFromAddress, defaultEmailFromName);

		List<InternetAddress> bcc = TransformUtil.transform(
			users,
			user -> {
				if (!StringUtil.equals(
					user.getEmailAddress(), "test@liferay.com")) {

					try {
						return new InternetAddress(
							user.getEmailAddress(), user.getFullName());
					}
					catch (Exception exception) {
						_log.error(exception);
					}
				}

				return null;
			});

		MailMessage mailMessage = new MailMessage(from, subject, body, true);

		InternetAddress[] bccArray = (InternetAddress[])bcc.toArray(new InternetAddress[0]);

		mailMessage.setBCC(bccArray);

		_mailService.sendEmail(mailMessage);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SCIMNotificationSchedulerJobConfiguration.class);

	public static final int MONTH = 30;

	public static final int WEEK = 7;

	public static final int DAY = 1;
	private SCIMClientOAuth2ApplicationConfiguration _scimClientOAuth2ApplicationConfiguration;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private OAuth2AuthorizationLocalService _oAuth2AuthorizationLocalService;

	@Reference
	private UserNotificationEventLocalService
		_userNotificationEventLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;


	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private MailService _mailService;

	@Reference
	private PrefsProps _prefsProps;

	@Reference
	private Language _language;
}