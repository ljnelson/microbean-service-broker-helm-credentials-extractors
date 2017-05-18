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
@Chart("prometheus")
public class PrometheusHelmChartCredentialsExtractor implements CredentialsExtractor {

  private static final Pattern PORT_PATTERN = Pattern.compile("The Prometheus server can be accessed via port (\\d+) on the following DNS name from within your cluster");

  @Inject
  public PrometheusHelmChartCredentialsExtractor(final KubernetesClient kubernetesClient) {
    super();
  }

  @Override
  public Map<? extends String, ?> extractCredentials(final Helm.Status status) throws ServiceBrokerException {
    Map<String, String> credentials = null;
    if (status != null) {
      final List<String> notes = status.getNotes();
      if (notes != null && !notes.isEmpty()) {
        credentials = new HashMap<>();
        for (int i = 0; i < notes.size(); i++) {
          String line = notes.get(i);
          Matcher matcher = null;
          matcher = PORT_PATTERN.matcher(line);
          assert matcher != null;
          if (matcher.find()) {
            final String port = matcher.group(1);
            credentials.put("port", port);
            if (i < notes.size() - 1) {
              String hostLine = notes.get(++i);
              credentials.put("host", hostLine);
              credentials.put("hostname", hostLine);
            }
            break;
          }
        }
      }
    }
    return credentials;
  }
}
