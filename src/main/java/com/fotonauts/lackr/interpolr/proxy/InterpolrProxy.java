package com.fotonauts.lackr.interpolr.proxy;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fotonauts.lackr.BaseFrontendRequest;
import com.fotonauts.lackr.BaseProxy;
import com.fotonauts.lackr.interpolr.Interpolr;

public class InterpolrProxy extends BaseProxy {

    static Logger log = LoggerFactory.getLogger(InterpolrProxy.class);

    protected Interpolr interpolr;

    public InterpolrProxy() {
    }

    public Interpolr getInterpolr() {
        return interpolr;
    }

    public void setInterpolr(Interpolr interpolr) {
        this.interpolr = interpolr;
    }

    protected BaseFrontendRequest createLackrFrontendRequest(HttpServletRequest request) {
        return new InterpolrFrontendRequest(this, request);        
    }
    
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        interpolr.start();
    }
    
    @Override
    public void doStop() throws Exception {
        interpolr.stop();
        super.doStop();
    }
}
