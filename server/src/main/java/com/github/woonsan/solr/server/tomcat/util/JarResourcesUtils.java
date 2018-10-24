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
package com.github.woonsan.solr.server.tomcat.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import com.github.woonsan.solr.server.tomcat.launch.Main;

public final class JarResourcesUtils {

    private JarResourcesUtils() {
    }

    public static File getJarFileHavingClasspathResource(final String resourcePath) throws IOException {
        final String resourceUrl = Main.class.getClassLoader().getResource(resourcePath).toString();

        if (!resourceUrl.startsWith("jar:")) {
            return null;
        }

        final int offset = resourceUrl.indexOf('!');

        if (offset == -1) {
            return null;
        }

        return new File(URI.create(resourceUrl.substring(4, offset)));
    }

    public static void extractJarToFolder(final File jarFile, final String entryPrefix, final File destFolder)
            throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        JarArchiveInputStream jais = null;

        try {
            fis = new FileInputStream(jarFile);
            bis = new BufferedInputStream(fis);
            jais = new JarArchiveInputStream(bis);

            JarArchiveEntry entry;
            File destFile;

            while ((entry = jais.getNextJarEntry()) != null) {
                String entryName = entry.getName();

                if (!entryName.startsWith(entryPrefix)) {
                    continue;
                }

                entryName = entryName.substring(entryPrefix.length());

                if (entry.isDirectory()) {
                    new File(destFolder, entryName).mkdirs();
                } else {
                    destFile = new File(destFolder, entryName);

                    FileOutputStream output = null;

                    try {
                        output = new FileOutputStream(destFile);
                        IOUtils.copyLarge(jais, output, 0, entry.getSize());
                    } finally {
                        IOUtils.closeQuietly(output);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(jais);
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }
    }

}
