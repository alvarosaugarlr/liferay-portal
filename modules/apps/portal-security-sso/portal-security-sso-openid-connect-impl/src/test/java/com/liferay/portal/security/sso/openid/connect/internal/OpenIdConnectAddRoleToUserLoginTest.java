package com.liferay.portal.security.sso.openid.connect.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.StagedModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.model.impl.CompanyImpl;
import com.liferay.portal.model.impl.RoleImpl;
import com.liferay.portal.model.impl.UserImpl;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.social.kernel.model.SocialActivityInterpreter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.liferay.portal.kernel.service.UserLocalService;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessorImpl;
import net.minidev.json.JSONObject;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import org.mockito.Mockito;

public class OpenIdConnectAddRoleToUserLoginTest {


	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;


	@Before
	public void setUp() throws Exception{

		// setup needed before each test method is executed.

		_openIdConnectUserInfoProcessorImpl = new OpenIdConnectUserInfoProcessorImpl();

		ReflectionTestUtil.setFieldValue(
			_openIdConnectUserInfoProcessorImpl,
			"_userLocalService", userLocalServiceMock);

		ReflectionTestUtil.setFieldValue(
			_openIdConnectUserInfoProcessorImpl,
			"_companyLocalService", companyLocalServiceMock);

		ReflectionTestUtil.setFieldValue(
			_openIdConnectUserInfoProcessorImpl,
			"_props", props);

		ReflectionTestUtil.setFieldValue(
			_openIdConnectUserInfoProcessorImpl,
			"_roleLocalService", roleLocalServiceMock);

/*		ReflectionTestUtil.setFieldValue(
			_openIdConnectUserInfoProcessorImpl,
			"company", companyMock);
*/


	}


	@Test
	public void testUseCase1() {

		String issuer = "https://accounts.google.com";
		String roleName = "Power User";

		Role role = new RoleImpl();
		long roleId = 3333;
		role.setCompanyId(companyId);
		role.setName(roleName);
		role.setType(RoleConstants.TYPE_REGULAR);
		role.setRoleId(roleId);

		setUpEnvironment(companyId, role);
		setUpPropsUtil(issuer, roleName);



		long userId = 0;
		try {
			 userId = _openIdConnectUserInfoProcessorImpl.processUserInfo(userInfo, companyId, issuer, mainPath,
				 portalURL);

			assertNotEquals(0, userId);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}
	}



	private void _setUpLocaleUtil() {
		LocaleUtil localeUtil = ReflectionTestUtil.getFieldValue(
			LocaleUtil.class, "_localeUtil");

		Map<String, Locale> locales = ReflectionTestUtil.getFieldValue(
			localeUtil, "_locales");

		locales.clear();

		locales.put("en", LocaleUtil.ENGLISH);

	}


	protected void setUpPropsUtil(
		String issuer, String roleName) {

		PropsUtil.setProps(props);

		Mockito.when(
			props.get("open.id.connect.user.info.processor.impl.issuer")
		).thenReturn(
			issuer
		);

		Mockito.when(
			props.get("open.id.connect.user.info.processor.impl.regular.role")
		).thenReturn(
			roleName
		);

	}


	private void setUpEnvironment(long companyId, Role role){


		String emailAddress = "email@liferay.com";

		Map <String, String> info = new HashMap<String, String>();
		info.put("sub", "000000000");
		info.put("email_verified", "true");
		info.put("name", "name");
		info.put("given_name", "given_name");
		info.put("locale", "en");
		info.put("hd", "liferay.com");
		info.put("family_name", "family_name");
		info.put("middle_name", "middle_name");
		info.put("picture", "picture");
		info.put("email", emailAddress);

		JSONObject jsonObject = new JSONObject(info);


		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = null;
		String password2 = null;
		boolean autoScreenName = true;
		String screenName = StringPool.BLANK;
		Locale locale = null;
		long prefixId = 0;
		long suffixId = 0;
		boolean male = true;
		int birthdayMonth = Calendar.JANUARY;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = StringPool.BLANK;
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = false;

		long[] roleIds = new long[] {role.getRoleId()}; //null; //_getRoleIds(companyId, issuer);


		userInfo = new UserInfo(jsonObject);
		mainPath = "/c";
		portalURL = "http://localhost:8080";


		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setPathMain(mainPath);
		serviceContext.setPortalURL(portalURL);

		String firstName = userInfo.getGivenName();
		String lastName = userInfo.getFamilyName();



		try {
			Mockito.when(
				companyLocalServiceMock.getCompany(companyId)
			).thenReturn(
				companyMock
			);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}


		_setUpLocaleUtil();

		User user = new UserImpl();
		user.setScreenName(screenName);
		user.setUserId(22);
		user.setEmailAddress(emailAddress);
		user.setDigest("digest");
		user.setLanguageId("en");


		User userError = new UserImpl();
		userError.setUserId(0);

		Mockito.when(
			userLocalServiceMock.fetchUserByEmailAddress(companyId, emailAddress)
		).thenReturn(
			null
		);

		try {
			Mockito.when(
				companyMock.getLocale()
			).thenReturn(
				LocaleUtil.ENGLISH
			);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}		}


//ver si quitar por locale
		try {
			Mockito.when(
				userLocalServiceMock.getDefaultUser(companyId)
			).thenReturn(
				user
			);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		try {
			Mockito.when(
				userLocalServiceMock.addUser(Mockito.anyLong(), Mockito.eq(companyId), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(),
					Mockito.eq(autoScreenName), Mockito.anyString(), Mockito.anyString(), Mockito.eq(locale), Mockito.anyString(),
					Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean(),
					Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(),
					Mockito.any(), Mockito.eq(roleIds), Mockito.any(), Mockito.anyBoolean(), Mockito.any(ServiceContext.class))
			).thenAnswer(
				invocationOnMock -> {
					Object[] arguments = invocationOnMock.getArguments();

					Object roleIdsArgument = arguments[21];

					if (Objects.equals(roleIdsArgument, roleIds)) {
						return user;
					}
					else {
						return userError;
					}
				}
			);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}		}

		try {

			Mockito.when(
				userLocalServiceMock.updatePasswordReset(user.getUserId(), false)
			).thenReturn(
				user
			);

		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}		}

		Mockito.when(
			roleLocalServiceMock.fetchRole(companyId, "Power User")
		).thenReturn(
			role
		);
	}

	private UserInfo userInfo = null;
	private String mainPath = null;
	private String portalURL = null;
	private final long companyId = 444444;

	private final UserLocalService userLocalServiceMock = Mockito.mock(UserLocalService.class);
	private final CompanyLocalService companyLocalServiceMock = Mockito.mock(CompanyLocalService.class);
	private final Props props = Mockito.mock(Props.class);
	private final RoleLocalService roleLocalServiceMock = Mockito.mock(RoleLocalService.class);
	private final Company companyMock = Mockito.mock(Company.class);
	private final UserLocalServiceUtil userLocalServiceUtilMock = Mockito.mock(UserLocalServiceUtil.class);


	private static OpenIdConnectUserInfoProcessorImpl _openIdConnectUserInfoProcessorImpl;


	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectAddRoleToUserLoginTest.class);

}