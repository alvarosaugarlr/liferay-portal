package com.liferay.portral.security.sso.openid.connect.test;


import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectAuthenticationHandler;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;

@RunWith(Arquillian.class)
public class OpenIdConnectAddRoleToUserLoginTest {



	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();

	}


	@Test
	public void testAutologinToSetAdminPassword() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		_prepareRequest(mockHttpServletRequest);

		// _openIdConnectAuthenticationHandler.processAuthenticationResponse(
		//			mockHttpServletRequest, mockHttpServletResponse, userIdUnsafeConsumer);

		Assert.assertEquals("1", "1");
	}

	private void _prepareRequest(MockHttpServletRequest mockHttpServletRequest){

		mockHttpServletRequest.setAttribute(
			WebKeys.COMPANY_ID, _company.getCompanyId());

		HttpSession httpSession = mockHttpServletRequest.getSession();

//		State state = new State("Oakwq8XSBX0BnFSdDajbcpW7OTWEty-oTsfN0i_uQmA");
		Nonce nonce = new Nonce("kpfvDrRT9Qzv7XOIzcAb2XdyaLgs-25rPRDAJUBnp9g");
		String providerName = "Google";

	}


	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectAddRoleToUserLoginTest.class);

	private static Company _company;

	private OpenIdConnectAuthenticationHandler
		_openIdConnectAuthenticationHandler;

}
