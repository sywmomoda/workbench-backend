package com.feiniu.yx.config;

/**
 * @author tongwenhuan
 * 2017年2月27日 下午3:12:26
 */
public class CasValidationFilter {
//public class CasValidationFilter extends Cas20ProxyReceivingTicketValidationFilter {
//	
//	private static final String CAS_SERVER_URL_PREFIX = "casServerUrlPrefix";
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
//				if (CAS_SERVER_URL_PREFIX.equals(name)) {
//					return SystemEnv.getProperty(CAS_SERVER_URL_PREFIX);
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
