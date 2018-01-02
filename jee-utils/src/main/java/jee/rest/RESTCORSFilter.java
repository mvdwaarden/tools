package jee.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import data.LogUtil;
import jee.http.HTTPConst;

public class RESTCORSFilter implements Filter {
	public static final String CORS_ALLOW_METHODS = "CORS_ALLOW_METHODS";
	public static final String CORS_ALLOW_ORIGIN = "CORS_ALLOW_ORIGIN";

	private String paramCORSAllowOrigin = "*";
	private String paramCORSAllowMethods = "GET, POST, PUT, DELETE";

	@Override

	public void destroy() {
		LogUtil.getInstance().info("CORS filter destroyed");
	}

	public String getParamCORSAllowOrigin() {
		return paramCORSAllowOrigin;
	}

	public void setParamCORSAllowOrigin(String paramCORSAllowOrigin) {
		this.paramCORSAllowOrigin = paramCORSAllowOrigin;
	}

	public String getParamCORSAllowMethods() {
		return paramCORSAllowMethods;
	}

	public void setParamCORSAllowMethods(String paramCORSAllowMethods) {
		this.paramCORSAllowMethods = paramCORSAllowMethods;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			boolean addCORSHeaders = false;
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if (httpRequest.getMethod().equals(HTTPConst.METHOD_OPTIONS)) {
				String origin = httpRequest.getHeader(HTTPConst.HEADER_ORIGIN);
				if (null != origin && !origin.isEmpty())
					addCORSHeaders = true;
			} else if (isRESTCall(httpRequest)) {
				addCORSHeaders = true;
			}
			if (addCORSHeaders) {
				httpResponse.addHeader(HTTPConst.HEADER_CORS_ACL_ALLOW_ORIGIN, getParamCORSAllowOrigin());
				httpResponse.addHeader(HTTPConst.HEADER_CORS_ACL_ALLOW_METHODS, getParamCORSAllowMethods());
			}
		}

		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LogUtil.getInstance().info("CORS filter initialized");
		String tmp = filterConfig.getInitParameter(CORS_ALLOW_ORIGIN);
		if (null != tmp && !tmp.isEmpty())
			setParamCORSAllowOrigin(tmp);
		tmp = filterConfig.getInitParameter(CORS_ALLOW_METHODS);
		if (null != tmp && !tmp.isEmpty())
			setParamCORSAllowMethods(tmp);
	}

	public boolean isRESTCall(HttpServletRequest request) {
		return true;
	}

}
