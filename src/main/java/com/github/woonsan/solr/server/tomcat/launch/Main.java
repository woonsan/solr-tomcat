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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.woonsan.solr.server.tomcat.servlet.HelloServlet;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static Options cliOptions = createCLIOptions();

    private static final LifecycleListener noopLifecycleListener = new LifecycleListener() {
        public void lifecycleEvent(LifecycleEvent event) {
        }
    };

    public static void main(String[] args) throws Exception {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(cliOptions, args);

        if (cmd.hasOption("help")) {
            new HelpFormatter().printHelp("solrserver", cliOptions);
            System.exit(1);
        }

        final String solrHome = cmd.getOptionValue("solr-home", null);
        final String tomcatBase = cmd.getOptionValue("tomcat-base", "tomcat-base");
        final String tomcatPort = cmd.getOptionValue("tomcat-port", "8080");
        final String contextPath = cmd.getOptionValue("context-path", "");
        final String docBase = cmd.getOptionValue("doc-base", tomcatBase + "/webapp");

        if (solrHome == null || solrHome.isEmpty()) {
            new HelpFormatter().printHelp("solrserver", cliOptions);
            System.exit(1);
        }

        final File solrHomeDir = new File(solrHome);
        if (!solrHomeDir.isDirectory()) {
            solrHomeDir.mkdirs();
        }
        System.setProperty("solr.solr.home", solrHomeDir.getAbsolutePath());

        final File tomcatBaseDir = new File(tomcatBase);
        if (!tomcatBaseDir.isDirectory()) {
            tomcatBaseDir.mkdirs();
        }
        final File appDocBaseDir = new File(docBase);
        if (!appDocBaseDir.isDirectory()) {
            appDocBaseDir.mkdirs();
        }

        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");

        final Tomcat tomcat = new Tomcat() {
            @Override
            public LifecycleListener getDefaultWebXmlListener() {
                return noopLifecycleListener;
            }
        };

        tomcat.setPort(Integer.parseInt(tomcatPort));
        tomcat.setBaseDir(tomcatBaseDir.getAbsolutePath());

        // Note: make sure that tomcat creates the default connector...
        tomcat.getConnector();

        StandardContext ctx = (StandardContext) tomcat.addWebapp(contextPath, appDocBaseDir.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());

        addSolrRequestFilter(ctx);
        addServletAndMappings(ctx, "HelloServlet", new HelloServlet(), "/hello/*");

        tomcat.start();
        log.info("Tomcat has started and is waiting for requests.");

        tomcat.getServer().await();
    }

    private static void addSolrRequestFilter(final StandardContext ctx) {
        final FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("SolrRequestFilter");
        filterDef.setFilter(new SolrDispatchFilter());
        filterDef.addInitParameter("excludePatterns", "/partials/.+,/libs/.+,/css/.+,/js/.+,/img/.+,/tpl/.+");

        final FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("SolrRequestFilter");
        filterMap.addURLPatternDecoded("/*");

        ctx.addFilterDef(filterDef);
        ctx.addFilterMap(filterMap);
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

    private static Options createCLIOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");
        options.addOption("s", "solr-home", true, "Solr home path");
        options.addOption("t", "tomcat-base", true, "Tomcat base path");
        options.addOption("p", "tomcat-port", true, "Tomcat port number");
        options.addOption("c", "context-path", true, "Application context path");
        options.addOption("d", "doc-base", true, "Application doc path");
        return options;
    }
}
