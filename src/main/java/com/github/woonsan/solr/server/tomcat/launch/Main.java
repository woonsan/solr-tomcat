/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.woonsan.solr.server.tomcat.launch;

import java.io.File;

import javax.servlet.Servlet;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.woonsan.solr.server.tomcat.servlet.HelloServlet;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static final LifecycleListener noopLifecycleListener = new LifecycleListener() {
        public void lifecycleEvent(LifecycleEvent event) {
        }
    };

    public static void main(String[] args) throws Exception {

        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");

        final File tomcatBase = new File("tomcat");
        final File docBaseDir = new File(tomcatBase, "webapp");
        if (!docBaseDir.isDirectory()) {
            docBaseDir.mkdirs();
        }

        final Tomcat tomcat = new Tomcat() {
            @Override
            public LifecycleListener getDefaultWebXmlListener() {
                return noopLifecycleListener;
            }
        };

        tomcat.setPort(8080);
        tomcat.setBaseDir(tomcatBase.getAbsolutePath());

        // Note: make sure that tomcat creates the default connector...
        tomcat.getConnector();

        StandardContext ctx = (StandardContext) tomcat.addWebapp("", docBaseDir.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());

        addServletAndMappings(ctx, "HelloServlet", new HelloServlet(), "/hello/*");

        tomcat.start();
        log.info("Tomcat has started and is waiting for requests.");

        tomcat.getServer().await();
    }

    private static void addServletAndMappings(final StandardContext ctx, final String servletName,
            final Servlet servlet, final String... decodedMappings) {
        Tomcat.addServlet(ctx, servletName, servlet);

        if (decodedMappings != null) {
            for (String decodedMapping : decodedMappings) {
                ctx.addServletMappingDecoded(decodedMapping, servletName);
            }
        }

        log.info("{} servlet added: {}, with mappings: {}", servletName, servlet, decodedMappings);
    }
}
