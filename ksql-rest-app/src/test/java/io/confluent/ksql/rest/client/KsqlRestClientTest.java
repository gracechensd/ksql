/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.rest.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.confluent.ksql.rest.client.exception.KsqlRestClientException;
import io.confluent.ksql.rest.ssl.SslClientConfigurer;
import io.confluent.rest.RestConfig;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KsqlRestClientTest {

  private static final String SERVER_ADDRESS = "http://timbuktu";

  @Mock
  private ClientBuilder clientBuilder;
  @Mock
  private SslClientConfigurer sslClientConfigurer;
  @Mock
  private Client client;
  private Map<String, String> clientProps;
  private Map<String, String> localProps;

  @Before
  public void setUp() {
    clientProps = new HashMap<>();
    localProps = new HashMap<>();

    when(clientBuilder.build()).thenReturn(client);
  }

  @Test
  public void shouldConfigureSslOnTheClient() {
    // Given:
    clientProps.put(RestConfig.SSL_TRUSTSTORE_LOCATION_CONFIG, "/trust/store/path");

    // When:
    new KsqlRestClient(SERVER_ADDRESS, localProps, clientProps, clientBuilder, sslClientConfigurer);

    // Then:
    verify(sslClientConfigurer).configureSsl(clientBuilder, clientProps);
  }

  @Test
  public void shouldThrowIfFailedToConfigureClient() {
    // Given:
    when(clientBuilder.register(any(Object.class))).thenThrow(new RuntimeException("boom"));

    // When:
    final KsqlRestClientException e = assertThrows(
        KsqlRestClientException.class,
        () -> new KsqlRestClient(SERVER_ADDRESS, localProps, clientProps, clientBuilder, sslClientConfigurer)
    );

    // Then:
    assertThat(e.getMessage(), containsString("Failed to configure rest client"));
    assertThat(e.getCause(), hasMessage(is("boom")));
  }

  @Test
  public void shouldThrowOnInvalidServerAddress() {
    // When:
    final KsqlRestClientException e = assertThrows(
        KsqlRestClientException.class,
        () -> new KsqlRestClient("timbuktu", localProps, clientProps, clientBuilder, sslClientConfigurer)
    );

    // Then:
    assertThat(e.getMessage(), containsString("The supplied serverAddress is invalid: timbuktu"));
  }
}