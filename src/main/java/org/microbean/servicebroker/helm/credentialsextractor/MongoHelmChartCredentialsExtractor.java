/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.servicebroker.helm.credentialsextractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;

import org.microbean.servicebroker.api.ServiceBrokerException;

import org.microbean.servicebroker.helm.CredentialsExtractor;
import org.microbean.servicebroker.helm.Helm;

import org.microbean.servicebroker.helm.annotation.Chart;

@ApplicationScoped
@Chart("mongodb")
public class MongoHelmChartCredentialsExtractor implements CredentialsExtractor {

  private static final Pattern PATTERN = Pattern.compile("\\s*kubectl run .+ --host (\\S+)(\\s+-p\\s+(.+))?");

  @Inject
  public MongoHelmChartCredentialsExtractor(final KubernetesClient kubernetesClient) {
    super();
  }

  @Override
  public Map<? extends String, ?> extractCredentials(final Helm.Status status) throws ServiceBrokerException {
    Map<String, String> credentials = null;
    if (status != null) {
      final List<String> notes = status.getNotes();
      if (notes != null && !notes.isEmpty()) {
        credentials = new HashMap<>();
        credentials.put("port", "27017"); // the chart hardcodes this
        for (String line : notes) {
          if (line != null) {
            Matcher matcher = null;
            matcher = PATTERN.matcher(line);
            assert matcher != null;
            if (matcher.find()) {
              final String host = matcher.group(1);
              credentials.put("host", host);
              credentials.put("hostname", host);
              if (matcher.groupCount() > 2) {
                String password = matcher.group(3);
                if (password != null)
                  credentials.put("password", password);
              }
              break;
            }
          }
        }
      }
    }
    return credentials;
  }

}
