package com.feiniu.yx.config;

/**
 * @author tongwenhuan
 * 2017年2月27日 下午2:37:58
 */
public class CasAuthenticationFilter {
//public class CasAuthenticationFilter extends AuthenticationFilter {
//	
//	private static final String CAS_SERVER_LOGIN_URL = "casServerLoginUrl";
//	private static final String SERVER_NAME = "serverName";
//
//	@Override
//	protected void initInternal(final FilterConfig filterConfig) throws ServletException {
//		this.setServerName(SystemEnv.getProperty(SERVER_NAME));
//		super.initInternal(new FilterConfig() {
//			@Override
//			public ServletContext getServletContext() {
//				return filterConfig.getServletContext();
//			}
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public Enumeration<String> getInitParameterNames() {
//				return filterConfig.getInitParameterNames();
//			}
//
//			@Override
//			public String getInitParameter(String name) {
//				if (CAS_SERVER_LOGIN_URL.equals(name)) {
//					return SystemEnv.getProperty(CAS_SERVER_LOGIN_URL);
//				} else if (SERVER_NAME.equals(name)) {
//					return SystemEnv.getProperty(SERVER_NAME);
//				} else {
//					return filterConfig.getInitParameter(name);
//				}
//			}
//
//			@Override
//			public String getFilterName() {
//				return filterConfig.getFilterName();
//			}
//		});
//	}
}
