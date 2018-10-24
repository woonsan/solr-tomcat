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

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.woonsan.solr.server.tomcat.util.JarResourcesUtils;

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
        final String tomcatBase = cmd.getOptionValue("tomcat-base", "temp-tomcat-base");
        final String tomcatPort = cmd.getOptionValue("tomcat-port", "8080");
        final String contextPath = cmd.getOptionValue("context-path", "");
        final String docBase = cmd.getOptionValue("doc-base", tomcatBase + File.separator + "webapp");
        final String logDir = cmd.getOptionValue("log-dir", tomcatBase + File.separator + "logs");

        if (solrHome == null || solrHome.isEmpty()) {
            new HelpFormatter().printHelp("solrserver", cliOptions);
            System.exit(1);
        }

        final File solrHomeDir = new File(solrHome);
        if (!solrHomeDir.isDirectory()) {
            solrHomeDir.mkdirs();
            final File solrHomeResJarFile = JarResourcesUtils
                    .getJarFileHavingClasspathResource("META-INF/solr/solr/solr.xml");
            JarResourcesUtils.extractJarToFolder(solrHomeResJarFile, "META-INF/solr/solr/", solrHomeDir);
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
        final File appLogDir = new File(logDir);
        if (!appLogDir.isDirectory()) {
            appLogDir.mkdirs();
        }

        System.setProperty("solr.log.dir", logDir);
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

        final WebResourceRoot resourceRoot = new StandardRoot(ctx);
        final File solrWebappResJarFile = JarResourcesUtils
                .getJarFileHavingClasspathResource("META-INF/solr/webapp/index.html");
        final WebResourceSet resourceSet = new JarResourceSet(resourceRoot, "/", solrWebappResJarFile.getAbsolutePath(),
                "/META-INF/solr/webapp");
        resourceRoot.addPreResources(resourceSet);
        ctx.setResources(resourceRoot);

        tomcat.start();
        log.info("Tomcat has started and is waiting for requests.");

        tomcat.getServer().await();
    }

    private static Options createCLIOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");
        options.addOption("s", "solr-home", true, "Solr home path");
        options.addOption("t", "tomcat-base", true, "Tomcat base path");
        options.addOption("p", "tomcat-port", true, "Tomcat port number");
        options.addOption("c", "context-path", true, "Application context path");
        options.addOption("d", "doc-base", true, "Application doc path");
        options.addOption("l", "log-dir", true, "Application logging directory path");
        return options;
    }

}
