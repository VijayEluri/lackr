package com.fotonauts.lackr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fotonauts.lackr.testutils.Factory;
import com.fotonauts.lackr.testutils.RemoteControlledStub;
import com.fotonauts.lackr.testutils.TestClient;

public class TestBaseProxy {

    RemoteControlledStub remoteControlledStub;
    Server proxyServer;
    TestClient client;

    @Before
    public void setup() throws Exception {
        
        remoteControlledStub = new RemoteControlledStub();
        remoteControlledStub.start();
        proxyServer = Factory.buildSimpleProxyServer(remoteControlledStub.getPort());
        proxyServer.start();
        client = new TestClient(proxyServer);
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        LifeCycle[] zombies = new LifeCycle[] { client, proxyServer, remoteControlledStub };
        for (LifeCycle z : zombies)
            z.stop();
        assertTrue(Thread.getAllStackTraces().size() < 10);
    }

    @Test
    public void hostProp() throws Exception {

        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, request.getHeader("Host").getBytes(), MimeType.TEXT_HTML);
            }
        });

        Request e = client.createRequest("/");
        e.header("Host", "something");
        client.runRequest(e, "something");
    }

    @Test
    public void userAgentProp() throws Exception {

        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, request.getHeader("User-Agent").getBytes(), MimeType.TEXT_HTML);
            }
        });

        Request e = client.createRequest("/");
        e.header("User-Agent", "something");
        client.runRequest(e, "something");
    }

    @Test
    public void urlWithUtf8EncodedStuff() throws Exception {

        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, baseRequest.getPathInfo().getBytes("UTF-8"), MimeType.TEXT_HTML);
            }
        });

        Request e = client.createRequest("/V%C3%B5ru");
        client.runRequest(e, "/Võru");
    }

    @Test
    public void reqBodyProp() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, IO.readBytes(request.getInputStream()), MimeType.TEXT_HTML);
            }
        });

        Request e = client.createRequest("/");
        System.err.println();
        e.method(HttpMethod.POST);
        e.content(new StringContentProvider("coin"));
        e.header("Content-Length", "4");
        client.runRequest(e, "coin");
    }

    @Test
    public void accept() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, request.getHeader("Accept") != null ? request.getHeader("Accept")
                        .getBytes() : "null".getBytes(), MimeType.TEXT_PLAIN);
            }
        });

        Request e = client.createRequest("/");
        e.header("Accept", "test/accept");
        client.runRequest(e, "test/accept");
    }

    @Test
    public void noAccept() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, request.getHeader("Accept") != null ? request.getHeader("Accept")
                        .getBytes() : "null".getBytes(), MimeType.TEXT_PLAIN);
            }
        });

        Request e = client.createRequest("/");
        client.runRequest(e, "null");
    }

    @Test
    public void redirect() throws InterruptedException, TimeoutException, ExecutionException {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                response.addHeader("Location", "http://blah.com");
                response.setStatus(301);
                response.flushBuffer();
            }
        });

        Request e = client.createRequest("/");
        ContentResponse r = e.send();

        assertEquals(301, r.getStatus());
        assertEquals("http://blah.com", r.getHeaders().getStringField("Location"));
    }

    @Test
    public void timeout() throws InterruptedException, TimeoutException, ExecutionException {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                try {
                    Thread.sleep(1000 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Request e = client.createRequest("/");
        e.send();
    }

    @Test
    public void parameters() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, (request.getParameter("par") != null ? request.getParameter("par")
                        : "#null").getBytes(), MimeType.TEXT_PLAIN);
            }
        });
        Request e = client.createRequest("/?par=toto");
        client.runRequest(e, "toto");

        e = client.createRequest("/?par=toto");
        e.method(HttpMethod.POST);
        client.runRequest(e, "toto");

        e = client.createRequest("/?par=toto");
        e.method(HttpMethod.PUT);
        client.runRequest(e, "toto");
    }

    @Test
    public void queryStringParameterNotMovedToBody() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, (request.getQueryString() != null ? request.getQueryString()
                        .getBytes() : "".getBytes()), MimeType.TEXT_PLAIN);
            }
        });
        Request e = client.createRequest("/queryString?par=toto");
        client.runRequest(e, "par=toto");

        e = client.createRequest("/queryString?par=toto");
        e.method(HttpMethod.POST);
        client.runRequest(e, "par=toto");
    }

    @Test
    public void cookiesIsolation() throws Exception {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                if (target.indexOf("SETIT") > 0)
                    response.addHeader(HttpHeader.SET_COOKIE.asString(), "c=1; Expires=Wed, 09-Jun-2021 10:18:14 GMT");
                RemoteControlledStub.writeResponse(response, request.getHeader("Cookie") != null ? request.getHeader("Cookie")
                        .getBytes() : "null".getBytes(), MimeType.TEXT_PLAIN);
            }
        });
        Request e = client.createRequest("/");
        client.runRequest(e, "null");
        assertTrue("cookie store empty", client.getClient().getCookieStore().getCookies().size() == 0);

        e = client.createRequest("/SETIT");
        ContentResponse r = client.runRequest(e, "null");
//        System.err.println(r.getHeaders().getStringField(HttpHeader.SET_COOKIE.asString()).contains("c=1"));
        assertTrue("cookie store empty", client.getClient().getCookieStore().getCookies().size() == 0);
        assertTrue("response has set-cookie", r.getHeaders().getStringField(HttpHeader.SET_COOKIE.asString()).contains("c=1"));

        e = client.createRequest("/");
        client.runRequest(e, "null");
        assertTrue("cookie store empty", client.getClient().getCookieStore().getCookies().size() == 0);

        e = client.createRequest("/");
        e.getHeaders().add(HttpHeader.COOKIE.asString(), "c=2");
        client.runRequest(e, "c=2");
        assertTrue("cookie store empty", client.getClient().getCookieStore().getCookies().size() == 0);
    }

    @Test
    public void hugeCookie() {
        remoteControlledStub.getCurrentHandler().set(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                RemoteControlledStub.writeResponse(response, "yop!".getBytes(), MimeType.TEXT_PLAIN);
            }
        });
        Request e = client.createRequest("/");
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 10000; i++)
            sb.append("c");
        e.getHeaders().add("Cookie", "cook=" + sb.toString());
        ContentResponse r = null;
        try {
            r = e.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e1) {
            e1.printStackTrace();
        }
        assertEquals(HttpStatus.REQUEST_ENTITY_TOO_LARGE_413, r.getStatus());
    }

    // here, the backend will emit a java.net.SocketTimeoutException after about one or two seconds.
    // the proxy should fail immediately after that with a "502 Bad gateway"
    // I set a 15s timeout on the client to avoid waiting for ages, but it should not trigger anyway.
    @Test
    public void testConnectionTimeout() throws Exception {
        Server brokenProxyServer = null;
        HttpClient brokenClient = null;
        // www.google.com port 81 drops packets
        try {
            brokenProxyServer = Factory.buildSimpleProxyServer(Factory.buildFullClientBackend("http://www.google.com:81", null));
            brokenProxyServer.setStopTimeout(1);
            brokenProxyServer.start();
            brokenClient = new HttpClient();
            brokenClient.start();
            int port = ((ServerConnector) brokenProxyServer.getConnectors()[0]).getLocalPort();
            ContentResponse response = brokenClient.newRequest("http://localhost:" + port + "/").timeout(3, TimeUnit.SECONDS).send();
            assertEquals(response.getStatus(), 502);
        } finally {
            if (brokenProxyServer != null) {
                brokenProxyServer.stop();
            }
            if(brokenClient != null) {
                brokenClient.stop();
            }
        }

    }
}
