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

import java.io.InputStream;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.util.ResourceHelper;
import org.dozer.DozerBeanMapper;

/**
 * Represents a Dozer endpoint.
 */
public class DozerEndpoint extends DefaultEndpoint {

    private DozerConfiguration configuration;
    private DozerBeanMapper mapper;

    /**
     * Create a new TransformEndpoint.
     * @param endpointUri The uri of the Camel endpoint.
     * @param component The {@link DozerComponent}.
     * @param configuration endpoint configuration
     */
    public DozerEndpoint(String endpointUri, Component component, DozerConfiguration configuration) throws Exception {
        super(endpointUri, component);
        this.configuration = configuration;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new DozerProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException(
                "Consumer not supported for Transform endpoints");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public synchronized DozerBeanMapper getMapper() throws Exception {
        if (mapper == null) {
            initDozer();
        }
        return mapper;
    }

    public DozerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DozerConfiguration configuration) {
        this.configuration = configuration;
    }

    private void initDozer() throws Exception {
        mapper = new DozerBeanMapper();
        InputStream mapStream = null;
        try {
            mapStream = ResourceHelper.resolveResourceAsInputStream(
                    getCamelContext().getClassResolver(), configuration.getMappingFile());
            if (mapStream == null) {
                throw new Exception("Unable to resolve Dozer config: " + configuration.getMappingFile());
            }
            mapper.addMapping(mapStream);
        } finally {
            if (mapStream != null) {
                mapStream.close();
            }
        }
    }
}
