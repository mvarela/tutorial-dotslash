# clj.intro/dotslash

##  Let's create a toy API

### DotSlash API

This service will offer a means for users to post "articles", and for other
users to comment and vote on them. For the first iteration, there will be no
persistence (the data will be held in memory), and all posts and comments will
be anonymous (via the "anonymous coward" user).

We will have few endpoints for posting articles, posting comments (which will be
all top-level, no replying to other comments will be allowed), fetching articles
and their associated comments (as JSON or EDN, to taste), and also rendering an
HTML views of the "front page", containing the latest 10 articles, and an HTML
endpoint for rendering a pretty version of the article and its comments

 It doesn't matter if you can't complete this now, the idea is to get you
 acquainted with working with the REPL, and thinking in Clojure.

 In later sessions, we will gradually add more features, such as:
   - persistence (to a DB of your choice, maybe SQLITE, or XTDB?)
   - component management (e.g., Integrant)
   - users / auth (user creation, login, JWT)
   - comment replies / threads
   - a nice client made in Clojurescript
   - etc...

## Instructions

We will create a "reference" branch for each tutorial session, and progressively
add more features as we cover different topics.

Checkout the relevant session branch, and have a look under the `doc` directory

### Practicalities

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build an uberjar (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/dotslash-0.1.0-SNAPSHOT.jar

If you remove `version` from `build.clj`, the uberjar will become `target/dotslash-standalone.jar`.

## License

Copyright © 2023 Martín Varela

Distributed under the Eclipse Public License version 1.0.
