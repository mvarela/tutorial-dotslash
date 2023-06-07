# Session 01 - Basics

In this first session, we will just get some basic tooling into the project, and
work off a single namespace, on the REPL. We won't concern ourselves with doing
things properly yet, but rather just get something up and running quickly, and
more importantly, get a feel for how REPL-driven development is done.

## Our stack 

We will need to add some dependencies, so we can get our API up and running. We
will be using [Reitit](https://github.com/metosin/reitit/tree/master) for our
routing, and the [jetty9](https://github.com/sunng87/ring-jetty9-adapter) Jetty
adapter, which gives us a recent version of
[Jetty](https://www.eclipse.org/jetty/) in a
[Ring](https://github.com/ring-clojure/ring)-compatible package. Ring is the
de-facto way of doing web stuff in Clojure, but since its inception, there have
been some developments which (in my opinion) improve on the default Ring way of
doing things. In any case, you'd do well to check out the [Ring
wiki](https://github.com/ring-clojure/ring/wiki) to get familiar with the basic
concepts.

The most noticeable departure from the Ring standard approach we'll take, is
that instead of using
[middleware](https://github.com/ring-clojure/ring/wiki/Middleware-Patterns), we
will use
[interceptors](https://quanttype.net/posts/2018-08-03-why-interceptors.html)
instead. This is largely a matter of personal preference, but I think they gel
better with Clojure's _it's just data_ philosophy, and also with Reitit
data-driven approach to routing.


## Other dependencies

 We will bring in some other dependencies that will be useful:
 
  *  org.clojure/data.json  - "easy" JSON manipulation (maybe we'll use [jsonista](https://github.com/metosin/jsonista) later, for better performance)
  *  org.clojure/tools.logging  - Basic logging framework for Clojure
  *  camel-snake-kebab/camel-snake-kebab  - Convenience conversions for names (in Clojure we tend to prefer kebab-case-identifiers, but for public APIs, maybe camelCaseIdentifiers are better. CSK helps with this)
  *  hiccup/hiccup - we'll use hiccup to generate HTML for our HTML endpoints
  *  http-kit/http-kit  - we'll use the HTTP client for testing out our APIs without needing Curl
  *  metosin/malli  - data specs
