# solr-tomcat

WIP: My PoC to try to demonstrate how to embed Apache Tomcat 9 for Apache Solr 7, inspired by this thread in users@:
- https://lists.apache.org/thread.html/2824f64b917e54c378a1e6aab3d544704fdde69e62c2cbc15e0ac83c@%3Cusers.tomcat.apache.org%3E

*Note*: Sorry, it's not working yet. Pull it again later.

## How to build

    $ mvn clean package

## How to run

I've created a test runner script for easy testing:

```
$ ./run.sh
...
Oct 26, 2018 4:23:31 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-nio-8983"]
...
2018-10-26 16:23:32.412 INFO  (main) [   ] o.a.s.s.SolrDispatchFilter  ___      _       Welcome to Apache Solr™ version 7.5.0
2018-10-26 16:23:32.412 INFO  (main) [   ] o.a.s.s.SolrDispatchFilter / __| ___| |_ _   Starting in standalone mode on port null
2018-10-26 16:23:32.413 INFO  (main) [   ] o.a.s.s.SolrDispatchFilter \__ \/ _ \ | '_|  Install dir: null
2018-10-26 16:23:32.434 INFO  (main) [   ] o.a.s.s.SolrDispatchFilter |___/\___/_|_|    Start time: 2018-10-26T20:23:32.416Z
...
INFO: Starting ProtocolHandler ["http-nio-8983"]
2018-10-26 16:23:33.460 INFO  (main) [   ] c.g.w.s.s.t.l.Main Tomcat has started and is waiting for requests.
...
```

## How to test

I've added a non-solr servlet, [HelloServlet](), just to test if the embedded Tomcat works--it works.

```
$ curl -i http://localhost:8983/solr/hello
HTTP/1.1 200 
Content-Type: text/html
Content-Length: 55
Date: Fri, 26 Oct 2018 21:34:10 GMT

<html>
<body>
<p>Hello, Solr!</p>
</body>
</html>
```

But if you visit http://localhost:8983/solr/index.html in your web browser, it doesn't work yet:

```
SolrCore Initialization Failures

    {{core}}: {{error}}

Please check your logs for more information
```

I couldn't find the cause(s) yet even after looking into logs at `./target/temp-tomcat-base/logs/solr.log`.

That's what I need to investigate on my next available time.

## Tomcat Dependency

It's just two tomcat JAR libraries. Really lightweight!

```
$ mvn dependency:tree -Dincludes=org.apache.tomcat
...
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ solr-tomcat-server ---
[INFO] com.github.woonsan:solr-tomcat-server:jar:0.1.0-SNAPSHOT
[INFO] \- org.apache.tomcat.embed:tomcat-embed-core:jar:9.0.12:compile
[INFO]    \- org.apache.tomcat:tomcat-annotations-api:jar:9.0.12:compile
```

## Solr Dependency

It seems to pull in all the solr dependencies through solr-core:

```
$ mvn dependency:tree
...
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ solr-tomcat-server ---
[INFO] com.github.woonsan:solr-tomcat-server:jar:0.1.0-SNAPSHOT
[INFO] +- org.apache.solr:solr-core:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-analyzers-common:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-analyzers-kuromoji:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-analyzers-nori:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-analyzers-phonetic:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-backward-codecs:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-classification:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-codecs:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-core:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-expressions:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-grouping:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-highlighter:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-join:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-memory:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-misc:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-queries:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-queryparser:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-sandbox:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-spatial-extras:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-spatial3d:jar:7.5.0:compile
[INFO] |  +- org.apache.lucene:lucene-suggest:jar:7.5.0:compile
[INFO] |  +- org.apache.solr:solr-solrj:jar:7.5.0:compile
[INFO] |  +- com.carrotsearch:hppc:jar:0.8.1:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.9.5:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-core:jar:2.9.5:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-databind:jar:2.9.5:compile
[INFO] |  +- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:jar:2.9.5:compile
[INFO] |  +- com.github.ben-manes.caffeine:caffeine:jar:2.4.0:compile
[INFO] |  +- com.google.guava:guava:jar:14.0.1:compile
[INFO] |  +- com.google.protobuf:protobuf-java:jar:3.1.0:compile
[INFO] |  +- com.lmax:disruptor:jar:3.4.0:compile
[INFO] |  +- com.tdunning:t-digest:jar:3.1:compile
[INFO] |  +- commons-codec:commons-codec:jar:1.10:compile
[INFO] |  +- commons-collections:commons-collections:jar:3.2.2:compile
[INFO] |  +- commons-configuration:commons-configuration:jar:1.6:compile
[INFO] |  +- commons-fileupload:commons-fileupload:jar:1.3.3:compile
[INFO] |  +- commons-io:commons-io:jar:2.5:compile
[INFO] |  +- commons-lang:commons-lang:jar:2.6:compile
[INFO] |  +- dom4j:dom4j:jar:1.6.1:compile
[INFO] |  +- info.ganglia.gmetric4j:gmetric4j:jar:1.0.7:compile
[INFO] |  +- io.dropwizard.metrics:metrics-core:jar:3.2.6:compile
[INFO] |  +- io.dropwizard.metrics:metrics-ganglia:jar:3.2.6:compile
[INFO] |  +- io.dropwizard.metrics:metrics-graphite:jar:3.2.6:compile
[INFO] |  +- io.dropwizard.metrics:metrics-jetty9:jar:3.2.6:compile
[INFO] |  +- io.dropwizard.metrics:metrics-jvm:jar:3.2.6:compile
[INFO] |  +- javax.servlet:javax.servlet-api:jar:3.1.0:compile
[INFO] |  +- joda-time:joda-time:jar:2.2:compile
[INFO] |  +- net.hydromatic:eigenbase-properties:jar:1.1.5:compile
[INFO] |  +- org.antlr:antlr4-runtime:jar:4.5.1-1:compile
[INFO] |  +- org.apache.calcite:calcite-core:jar:1.13.0:compile
[INFO] |  +- org.apache.calcite:calcite-linq4j:jar:1.13.0:compile
[INFO] |  +- org.apache.calcite.avatica:avatica-core:jar:1.10.0:compile
[INFO] |  +- org.apache.commons:commons-exec:jar:1.3:compile
[INFO] |  +- org.apache.commons:commons-lang3:jar:3.6:compile
[INFO] |  +- org.apache.commons:commons-math3:jar:3.6.1:compile
[INFO] |  +- org.apache.curator:curator-client:jar:2.8.0:compile
[INFO] |  +- org.apache.curator:curator-framework:jar:2.8.0:compile
[INFO] |  +- org.apache.curator:curator-recipes:jar:2.8.0:compile
[INFO] |  +- org.apache.hadoop:hadoop-annotations:jar:2.7.4:compile
[INFO] |  |  \- jdk.tools:jdk.tools:jar:1.8:system
[INFO] |  +- org.apache.hadoop:hadoop-auth:jar:2.7.4:compile
[INFO] |  +- org.apache.hadoop:hadoop-common:jar:2.7.4:compile
[INFO] |  +- org.apache.hadoop:hadoop-hdfs:jar:2.7.4:compile
[INFO] |  +- org.apache.htrace:htrace-core:jar:3.2.0-incubating:compile
[INFO] |  +- org.apache.httpcomponents:httpclient:jar:4.5.3:compile
[INFO] |  +- org.apache.httpcomponents:httpcore:jar:4.4.6:compile
[INFO] |  +- org.apache.httpcomponents:httpmime:jar:4.5.3:compile
[INFO] |  +- org.apache.logging.log4j:log4j-1.2-api:jar:2.11.0:compile
[INFO] |  +- org.apache.logging.log4j:log4j-api:jar:2.11.0:compile
[INFO] |  +- org.apache.logging.log4j:log4j-core:jar:2.11.0:compile
[INFO] |  +- org.apache.logging.log4j:log4j-slf4j-impl:jar:2.11.0:compile
[INFO] |  +- org.apache.zookeeper:zookeeper:jar:3.4.11:compile
[INFO] |  +- org.codehaus.jackson:jackson-core-asl:jar:1.9.13:compile
[INFO] |  +- org.codehaus.jackson:jackson-mapper-asl:jar:1.9.13:compile
[INFO] |  +- org.codehaus.janino:commons-compiler:jar:2.7.6:compile
[INFO] |  +- org.codehaus.janino:janino:jar:2.7.6:compile
[INFO] |  +- org.codehaus.woodstox:stax2-api:jar:3.1.4:compile
[INFO] |  +- org.codehaus.woodstox:woodstox-core-asl:jar:4.4.1:compile
[INFO] |  +- org.eclipse.jetty:jetty-continuation:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-deploy:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-http:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-io:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-jmx:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-rewrite:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-security:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-server:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-servlet:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-servlets:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-util:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-webapp:jar:9.4.11.v20180605:compile
[INFO] |  +- org.eclipse.jetty:jetty-xml:jar:9.4.11.v20180605:compile
[INFO] |  +- org.locationtech.spatial4j:spatial4j:jar:0.7:compile
[INFO] |  +- org.noggit:noggit:jar:0.8:compile
[INFO] |  +- org.ow2.asm:asm:jar:5.1:compile
[INFO] |  +- org.ow2.asm:asm-commons:jar:5.1:compile
[INFO] |  +- org.restlet.jee:org.restlet:jar:2.3.0:compile
[INFO] |  +- org.restlet.jee:org.restlet.ext.servlet:jar:2.3.0:compile
[INFO] |  +- org.rrd4j:rrd4j:jar:3.2:compile
[INFO] |  +- org.slf4j:jcl-over-slf4j:jar:1.7.24:compile
[INFO] |  \- org.slf4j:slf4j-api:jar:1.7.24:compile
[INFO] +- org.apache.tomcat.embed:tomcat-embed-core:jar:9.0.12:compile
[INFO] |  \- org.apache.tomcat:tomcat-annotations-api:jar:9.0.12:compile
[INFO] +- commons-cli:commons-cli:jar:1.4:compile
[INFO] +- org.apache.commons:commons-compress:jar:1.18:compile
[INFO] +- com.github.woonsan:solr-tomcat-resources-solr-solr:jar:0.1.0-SNAPSHOT:compile
[INFO] \- com.github.woonsan:solr-tomcat-resources-solr-webapp:jar:0.1.0-SNAPSHOT:compile
```

By the way, in my PoC project, I've added `commons-cli:commons-cli` for command line parsing, and `org.apache.commons:commons-compress` for initialization of solr home and solr webapp by extracting jar files.
