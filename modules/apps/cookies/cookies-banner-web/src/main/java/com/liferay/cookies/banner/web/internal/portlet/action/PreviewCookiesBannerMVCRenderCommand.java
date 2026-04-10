package com.liferay.cookies.banner.web.internal.portlet.action;

import com.liferay.cookies.banner.web.internal.constants.CookiesBannerPortletKeys;
import com.liferay.cookies.banner.web.internal.constants.CookiesBannerWebKeys;
import com.liferay.cookies.banner.web.internal.display.context.CookiesBannerDisplayContext;
import com.liferay.cookies.configuration.CookiesConfigurationProvider;
import com.liferay.layout.utility.page.kernel.provider.LayoutUtilityPageEntryLayoutProvider;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true,
	property = {
		"jakarta.portlet.name=" + CookiesBannerPortletKeys.COOKIES_BANNER,
		"mvc.command.name=/cookies_banner/preview"
	},
	service = MVCRenderCommand.class
)
public class PreviewCookiesBannerMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(RenderRequest renderRequest, RenderResponse renderResponse) {

	/*	CookiesBannerDisplayContext cookiesBannerDisplayContext =
			new CookiesBannerDisplayContext(
				_cookiesConfigurationProvider,
				_layoutUtilityPageEntryLayoutProvider, renderRequest);

		renderRequest.setAttribute(
			CookiesBannerWebKeys.COOKIES_BANNER_DISPLAY_CONTEXT,
			cookiesBannerDisplayContext);


	 */

		renderRequest.setAttribute("isPreviewMode", true);

		return "/cookies_banner/view.jsp";
	}


	@Reference
	private CookiesConfigurationProvider _cookiesConfigurationProvider;

	@Reference
	private LayoutUtilityPageEntryLayoutProvider
		_layoutUtilityPageEntryLayoutProvider;
}