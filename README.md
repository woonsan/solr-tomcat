# solr-tomcat

PoC to embed Apache Tomcat 9 for Apache Solr 7.

## How to build

    $ mvn clean package

## How to run

```
$ cd target
$ bin/solrserver
usage: solrserver
 -c,--context-path <arg>   Application context path
 -d,--doc-base <arg>       Application doc path
 -h,--help                 Print help
 -p,--tomcat-port <arg>    Tomcat port number
 -s,--solr-home <arg>      Solr home path
 -t,--tomcat-base <arg>    Tomcat base path

$ bin/solrserver --solr-home=classes/META-INF/solr-server/solr
...
INFO: Starting ProtocolHandler ["http-nio-8080"]
2018-10-21 23:17:42,778 [main] INFO  com.github.woonsan.solr.server.tomcat.launch.Main - Tomcat has started and is waiting for requests.
```

## How to test

    $ curl -v http://localhost:8080/hello/
