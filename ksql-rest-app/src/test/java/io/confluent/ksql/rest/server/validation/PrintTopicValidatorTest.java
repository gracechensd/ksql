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

package io.confluent.ksql.rest.server.validation;

import static io.confluent.ksql.rest.entity.KsqlErrorMessageMatchers.errorMessage;
import static io.confluent.ksql.rest.entity.KsqlStatementErrorMessageMatchers.statement;
import static io.confluent.ksql.rest.server.resources.KsqlRestExceptionMatchers.exceptionStatementErrorMessage;
import static io.confluent.ksql.rest.server.resources.KsqlRestExceptionMatchers.exceptionStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.confluent.ksql.parser.KsqlParser.PreparedStatement;
import io.confluent.ksql.parser.tree.PrintTopic;
import io.confluent.ksql.rest.server.TemporaryEngine;
import io.confluent.ksql.rest.server.resources.KsqlRestException;
import io.confluent.ksql.statement.ConfiguredStatement;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrintTopicValidatorTest {

  @Rule public final TemporaryEngine engine = new TemporaryEngine();

  @Test
  public void shouldThrowExceptionOnPrintTopic() {
    // When:
    final KsqlRestException e = assertThrows(
        (KsqlRestException.class),
        () -> CustomValidators.PRINT_TOPIC.validate(
            ConfiguredStatement.of(
                PreparedStatement.of("PRINT 'topic';", mock(PrintTopic.class)),
                ImmutableMap.of(),
                engine.getKsqlConfig()),
            engine.getEngine(),
            engine.getServiceContext()
        )
    );

    // Then:
    assertThat(e, exceptionStatusCode(is(Code.BAD_REQUEST)));
    assertThat(e, exceptionStatementErrorMessage(errorMessage(containsString(
        "SELECT and PRINT queries must use the /query endpoint"))));
    assertThat(e, exceptionStatementErrorMessage(statement(containsString(
        "PRINT 'topic';"))));
  }

}
