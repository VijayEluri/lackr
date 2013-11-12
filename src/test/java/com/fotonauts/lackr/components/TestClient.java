package com.fotonauts.lackr.components;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class TestClient extends AbstractLifeCycle {

    HttpClient client;
    int port;

    public TestClient(int port) {
        this.port = port;
        this.client = Factory.buildFullClient();
    }

    @Override
    protected void doStart() throws Exception {
        client.start();
    }

    @Override
    protected void doStop() throws Exception {
        client.stop();
    }

    public Request createExchange(String path) {
        return client.newRequest("http://localhost:" + port + path);
    }

    public ContentResponse runRequest(Request e, String expect) throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = e.timeout(600, TimeUnit.SECONDS).send();
        //System.err.println(response.getContentAsString());
        assertEquals(200, response.getStatus());
        assertEquals(expect, response.getContentAsString());
        return response;
    }

    public HttpClient getClient() {
        return client;
    }

}