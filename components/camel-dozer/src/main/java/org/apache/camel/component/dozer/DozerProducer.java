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
package org.apache.camel.component.dozer;

import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.processor.MarshalProcessor;
import org.apache.camel.processor.UnmarshalProcessor;
import org.apache.camel.spi.DataFormat;

/**
 * The Transform producer.
 */
public class DozerProducer extends DefaultProducer {
    private DozerEndpoint endpoint;
    private UnmarshalProcessor unmarshaller;
    private MarshalProcessor marshaller;
    private LiteralMapper literalMapper;

    /**
     * Create a new producer for transform endpoints.
     * @param endpoint endpoint requiring a producer
     */
    public DozerProducer(DozerEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        literalMapper = new LiteralMapper();
        literalMapper.setLiteral("");
    }

    @Override
    public void process(Exchange exchange) throws Exception {  
        // Unmarshal the source content only if an unmarshaller is configured.
        if (endpoint.getConfiguration().getUnmarshalId() != null) {
            resolveUnmarshaller(exchange, endpoint.getConfiguration().getUnmarshalId())
                .process(exchange);
        }
        
        // Load the target model class and convert the body to that type to 
        // trigger the Dozer mapping.
        Class<?> targetModel = Class.forName(endpoint.getConfiguration().getTargetModel());
        Object sourceObject = exchange.hasOut() 
                ? exchange.getOut().getBody() : exchange.getIn().getBody();
        Object targetObject = endpoint.getMapper().map(sourceObject, targetModel);
        endpoint.getMapper().map(literalMapper, targetObject);
        exchange.getIn().setBody(targetObject);
        
        // Marshal the source content only if a marshaller is configured.
        if (endpoint.getConfiguration().getMarshalId() != null) {
            resolveMarshaller(exchange, endpoint.getConfiguration().getMarshalId())
                .process(exchange);
        }
    }
    
    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (unmarshaller != null) {
            unmarshaller.stop();
        }
        if (marshaller != null) {
            marshaller.stop();
        }
    }
    
    @Override
    protected void doShutdown() throws Exception {
        super.doShutdown();
        if (unmarshaller != null) {
            unmarshaller.shutdown();
        }
        if (marshaller != null) {
            marshaller.shutdown();
        }
    }
    
    /**
     * Find and configure an unmarshaller for the specified data format.
     */
    private synchronized UnmarshalProcessor resolveUnmarshaller(
            Exchange exchange, String dataFormatId) throws Exception {
        
        if (unmarshaller == null) {
            DataFormat dataFormat = DataFormatDefinition.getDataFormat(
                    exchange.getUnitOfWork().getRouteContext(), null, dataFormatId);
            if (dataFormat == null) {
                throw new Exception("Unable to resolve data format for unmarshalling: " + dataFormatId);
            }
            
            // Wrap the data format in a processor and start/configure it.  
            // Stop/shutdown is handled when the corresponding methods are 
            // called on this producer.
            unmarshaller = new UnmarshalProcessor(dataFormat);
            if (unmarshaller instanceof CamelContextAware) {
                ((CamelContextAware)unmarshaller).setCamelContext(exchange.getContext());
            }
            unmarshaller.start();
        }
        return unmarshaller;
    }
    
    /**
     * Find and configure an unmarshaller for the specified data format.
     */
    private synchronized MarshalProcessor resolveMarshaller(
            Exchange exchange, String dataFormatId) throws Exception {
        
        if (marshaller == null) {
            DataFormat dataFormat = DataFormatDefinition.getDataFormat(
                    exchange.getUnitOfWork().getRouteContext(), null, dataFormatId);
            if (dataFormat == null) {
                throw new Exception("Unable to resolve data format for marshalling: " + dataFormatId);
            }
            
            // Wrap the data format in a processor and start/configure it.  
            // Stop/shutdown is handled when the corresponding methods are 
            // called on this producer.
            marshaller = new MarshalProcessor(dataFormat);
            if (marshaller instanceof CamelContextAware) {
                ((CamelContextAware)marshaller).setCamelContext(exchange.getContext());
            }
            marshaller.start();
        }
        return marshaller;
    }
    
}
