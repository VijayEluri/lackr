Lackr Backends
==============

Lackr backend is an abstraction that hide the details of how queries sent by the proxy should be executed.

Getting started layout
----------------------

A trivial proxy like the one from the getting-started Demo is plugged on a simple "ClientBackend" that send
request to a given remote http server (that we have also called "backend" so far, just to get things a bit more
confusing).

```
                       +----------------------------+                                                                         
                       |                            |                                                                         
   Incoming            |  +----------------------+  |                                                                         
   Requests    --------+->|       BaseProxy      |  |                                                                         
                       |  +----------------------+  |                                                                         
                       |              |             |                                                                         
                       |              v             |                                                                         
                       |  +----------------------+  |                                                                         
                       |  |     ClientBackend    |--+------> Actual HTTP server Backend                                    
                       |  +----------------------+  |                                                                         
                       |                            |                                                                         
                       |         Lackr Demo         |                                                                         
                       +----------------------------+                                                                         
```

a real life example
-------------------

But in real life, things can get slightly more interesting. This is the _real_ backend hierarchy that is in
use for the Fotopedia Web app and Web services.

```java
    new TryPassBackend(
        new LoggingBackend(
            new InProcessBackend( [ fast scala stack servlet instance ] )
        ),
        new LoggingBackend(
            new HashRingBackend(
                new LoggingBackend(new ClientBackend()), // varnish server 1
                new LoggingBackend(new ClientBackend()), // varnish server 2
                new LoggingBackend(new ClientBackend())  // varnish server 3
            )
        )
    )
```

It's pseudo-code. Attributes of the various backends have been ommited. Let's have a look at what does what.

- TryPassBackend is a backend that is configured with an orderd list of backends. It will try them in turn until one
  of them handles the query.
- LoggingBackend is logically transparent but... logs stuff. It's currently not part of Lackr but of our
  application specific code. We may move it to Lackr someday, but it needs some work.
- InProcessBackend wraps a standard Servlet (which is actually our fast-stack app) and performs queries against it
  without going through the network.
- HashRingBackend performs consistent hashing against a ring made of its children backends. The choice of backend
  is done by hashing the path and query paramters of the query in order to optimise the use of memory in our
  3-server Varnish cache cluster.
- ClientBackend is our old friend from getting-started. It wraps a jetty http client to perform HTTP over the network
  against another server. It will performs queries against one given varnish, which will in turn forward them to our
  slow Ruby-on-Rails stack if necessary.

So basically, we give the *fast* and *in-process* stack a chance to deal with the request itself before falling back
to a cache cluster, which in turn falls back to the Ruby-on-Rails app. We could use a ClientBackend and have the fast
stack run elsewhere in its own JVM, but the use of InProcessBackend is an optimisation to avoid a network roundtrip to
a remote HTTP server that in many cases would result to a 501 "please pass to the next" response.

Obviously this optimisation can only work for backends that are implemented as Servlet. If the fast stack was in go, for
instance, we would have no choice but use the ClientBackend. Another constraint on the InProcessBackend is for the
wrapped servlet to be strictly synchronous. Once again, if it was to use asynchronous servlet processing (as Lackr
proxies do, by the way), we would have to go through a separate server and a ClientBackend.