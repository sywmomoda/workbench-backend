package com.feiniu.yx.common.interceptor;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.feiniu.ssoutil.SsoAuth;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.UserUtil;
import com.feiniu.yx.util.WebUtil;

/**
 * Portal后台SSO认证拦截器
 *
 * @author: zhouxiang
 * @date: 2018年3月8日 上午11:24:21       
 */
public class PortalInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private SsoAuth ssoAuth;

	private String portalSsoEnv = SystemEnv.getProperty("portal.sso.env");

	private String thisServerName = SystemEnv.getProperty("portal.this.servername");

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		boolean isLogined = true;
		String ssoCookieTokenValue = WebUtil.getCookieValue(request, YXConstant.PORTAL_SSO_COOKIE_TOKEN_NAME);
		
		if (StringUtils.isNotBlank(ssoCookieTokenValue)) {
			String username = ssoAuth.authLogined(ssoCookieTokenValue);
			if (username != null) {
				// 将当前username存入request
				request.setAttribute(YXConstant.PORTAL_CURRENT_USERNAME, username);
				UserUtil.setUserId(username);
			} else {
				isLogined = false;
			}
		} else {
			isLogined = false;
		}

		// 认证失败处理
		if (!isLogined) {
			response.sendRedirect(this.getLoginUrl(request, response));
		}

		return isLogined;
	}

	private String getLoginUrl(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		String domain = null;
		if (("dev".equals(this.portalSsoEnv)) || ("beta".equals(this.portalSsoEnv)))
			domain = "http://sso.beta1.fn/login?r=";
		else if (("preview".equals(this.portalSsoEnv)) || ("online".equals(this.portalSsoEnv))) {
			domain = "http://sso.idc1.fn/login?r=";
		}
		String backUrl = WebUtil.getCurrentRequestUrl(request, response, this.thisServerName);
		return domain + Base64.encodeBase64URLSafeString(backUrl.getBytes("UTF-8"));
	}
}
