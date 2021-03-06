package com.fotonauts.lackr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Jetty handler adapter for Demo Proxies.
 * 
 * @author kali
 *
 */
public class LackrProxyJettyHandler extends AbstractHandler {

    private BaseProxy proxy;

    public LackrProxyJettyHandler() {

    }

    public LackrProxyJettyHandler(BaseProxy proxy) {
        setProxy(proxy);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        getProxy().handle(request, response);
    }

    public BaseProxy getProxy() {
        return proxy;
    }

    public void setProxy(BaseProxy proxy) {
        if (proxy == this.proxy)
            return;
        if (this.proxy != null)
            removeBean(this.proxy);
        this.proxy = proxy;
        addBean(proxy);
    }

}
