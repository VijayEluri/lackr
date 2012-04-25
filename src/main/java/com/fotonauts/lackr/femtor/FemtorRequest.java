package com.fotonauts.lackr.femtor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import com.fotonauts.lackr.BackendRequest;

public class FemtorRequest extends HttpServletRequestWrapper {

	BackendRequest request;
	private HttpFields headers = new HttpFields();
    private MultiMap<String> params;

	public FemtorRequest(HttpServletRequest httpServletRequest, BackendRequest request) {
		super(httpServletRequest);
		this.request = request;
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public String getPathInfo() {
		return request.getPath();
	}

	@Override
	public String getQueryString() {
		return request.getParams();
	}

	@Override
	public String getRequestURI() {
		return request.getPath();
	}

	public void addHeader(String name, String value) {
	    headers.add(name, value);
	}

	protected MultiMap<String> getParams() {
	    if(params == null) {
	        params = new MultiMap<String>();
	        UrlEncoded.decodeTo(getQueryString(), params, "UTF-8");
	    }
	    return params;
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	public Enumeration getParameterNames() {
        if(request.getFrontendRequest().getRootRequest() == request)
            return super.getParameterNames();
        else
            return Collections.enumeration(getParams().keySet());
	}
	
	@Override
	public String getParameter(String name) {
	    if(request.getFrontendRequest().getRootRequest() == request)
	        return super.getParameter(name);
	    else
	        return getParams().getString(name);
	}
	
	@Override
	public String[] getParameterValues(String name) {
        if(request.getFrontendRequest().getRootRequest() == request)
            return super.getParameterValues(name);
        else
	    return getParams().getValues(name).toArray(new String[0]);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {
        if(request.getFrontendRequest().getRootRequest() == request)
            return super.getParameterMap();
        else
        return getParams().toStringArrayMap();
	    
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaderNames() {
	    return headers.getFieldNames();
	}

	@Override
	public String getHeader(String name) {
	    return headers.getStringField(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaders(String name) {
	    return headers.getValues(name);
	}
}
