#!/bin/sh

./server/target/bin/solrserver \
    --solr-home=webapp/target/classes/META-INF/solr/solr \
    --doc-base=server/target/tomcat-webapp
