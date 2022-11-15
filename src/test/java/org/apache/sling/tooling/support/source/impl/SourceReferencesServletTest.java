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
package org.apache.sling.tooling.support.source.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Dictionary;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;

@ExtendWith(MockitoExtension.class)
class SourceReferencesServletTest {

    @SuppressWarnings("unchecked")
    @Test
    void testWriteBundleSourceJson() throws IOException {
        Bundle bundle1 = Mockito.mock(Bundle.class);
        BundleRevision bundleRevision = Mockito.mock(BundleRevision.class);
        Mockito.when(bundle1.adapt(BundleRevision.class)).thenReturn(bundleRevision);
        Dictionary<String, String> dictionary = Mockito.mock(Dictionary.class);
        Mockito.when(bundle1.getHeaders()).thenReturn(dictionary);
        BundleContext ctx = Mockito.mock(BundleContext.class);
        URLConnection mockUrlConnection = Mockito.mock(URLConnection.class);
        URLStreamHandler stubUrlHandler = new URLStreamHandler() {
            @Override
             protected URLConnection openConnection(URL u) throws IOException {
                return mockUrlConnection;
             }
        };
        URL url = new URL("foo", "bar", 99, "/foobar", stubUrlHandler);
        Mockito.when(mockUrlConnection.getInputStream()).thenReturn(this.getClass().getResourceAsStream("pom.properties"));
        Mockito.when(bundle1.findEntries("/META-INF/maven", "pom.properties", true)).thenReturn(Collections.enumeration(Collections.singleton(url)));

        SourceReferencesServlet servlet = new SourceReferencesServlet(ctx);
        StringWriter writer = new StringWriter();
        servlet.writeBundleSourceJson(writer, bundle1);
        String expectedOutput;
        try (InputStream input = this.getClass().getResourceAsStream("expectedBundleSource.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            // normalize line separators to system default
            expectedOutput = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertEquals(expectedOutput, writer.toString());
    }

}
