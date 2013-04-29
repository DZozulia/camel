/**
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
package org.apache.camel.dataformat.bindy.fixed.link;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.FixedLengthRecord;
import org.apache.camel.dataformat.bindy.annotation.Link;
import org.apache.camel.model.dataformat.BindyDataFormat;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * This test validates that header and footer records are successfully
 * marshalled / unmarshalled in conjunction with the primary data records
 * defined for the bindy data format.
 */
public class BindySimpleFixedLengthWithLinkTest extends CamelTestSupport {

    public static final String URI_DIRECT_UNMARSHALL = "direct:unmarshall";
    public static final String URI_MOCK_UNMARSHALL_RESULT = "mock:unmarshall-result";

    private static final String TEST_RECORD = "AAABBBCCC\r\n";

    @EndpointInject(uri = URI_MOCK_UNMARSHALL_RESULT)
    private MockEndpoint unmarshallResult;

    // *************************************************************************
    // TESTS
    // *************************************************************************

    @Test
    public void testUnmarshallMessage() throws Exception {

        unmarshallResult.expectedMessageCount(1);

        template.sendBody(URI_DIRECT_UNMARSHALL, TEST_RECORD);

        unmarshallResult.assertIsSatisfied();

        // check the model
        Exchange exchange = unmarshallResult.getReceivedExchanges().get(0);
        BindySimpleFixedLengthWithLinkTest.Order order = (BindySimpleFixedLengthWithLinkTest.Order) exchange.getIn().getBody();
        assertEquals("AAA", order.fieldA);
        assertEquals("BBB", order.subRec.fieldB);
        assertEquals("CCC", order.fieldC);

    }

    // *************************************************************************
    // ROUTES
    // *************************************************************************

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        RouteBuilder routeBuilder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                BindyDataFormat bindy = new BindyDataFormat();
                bindy.setClassType(BindySimpleFixedLengthWithLinkTest.Order.class);
                bindy.setLocale("en");
                bindy.setType(BindyType.Fixed);

                from(URI_DIRECT_UNMARSHALL)
                        .unmarshal().bindy(BindyType.Fixed, BindySimpleFixedLengthWithLinkTest.Order.class)
                        .to(URI_MOCK_UNMARSHALL_RESULT);
            }
        };

        return routeBuilder;
    }

    // *************
    // DATA FORMATS
    // *************
    @FixedLengthRecord
    public static class Order {
        // 'AAA'
        @DataField(pos = 1, length = 3)
        private String fieldA;

        @Link
        private SubRec subRec;

        // 'CCC'
        @DataField(pos = 3, length = 3)
        private String fieldC;

        public String getFieldA() {
            return fieldA;
        }

        public void setFieldA(String fieldA) {
            this.fieldA = fieldA;
        }

        public String getFieldC() {
            return fieldC;
        }

        public void setFieldC(String fieldC) {
            this.fieldC = fieldC;
        }

        public SubRec getSubRec() {
            return subRec;
        }

        public void setSubRec(SubRec subRec) {
            this.subRec = subRec;
        }

    }

    @Link
    @FixedLengthRecord
    public static class SubRec {

        @DataField(pos = 2, length = 3)
        private String fieldB;

        public String getFieldB() {
            return fieldB;
        }

        public void setFieldB(String fieldB) {
            this.fieldB = fieldB;
        }

    }
}