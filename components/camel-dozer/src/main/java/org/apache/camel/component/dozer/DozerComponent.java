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

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.converter.dozer.DozerBeanMapperConfiguration;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.util.ReflectionHelper;
import org.dozer.config.GlobalSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DozerComponent extends UriEndpointComponent {
    
    public static final String CONTEXT_ID = "CamelDozerContextId";
    
    private static final Logger LOG = LoggerFactory.getLogger(DozerComponent.class);

    public DozerComponent() {
        super(DozerEndpoint.class);
        initDozerSettings();
    }
    
    public DozerComponent(CamelContext context) {
        super(context, DozerEndpoint.class);
        initDozerSettings();
    }
    
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        DozerConfiguration config = new DozerConfiguration();
        config.setName(remaining);
        config.setMappingConfiguration(getAndRemoveOrResolveReferenceParameter(
                parameters, "mappingConfiguration", DozerBeanMapperConfiguration.class));
        setProperties(config, parameters);
        
        // Validate endpoint parameters
        if (config.getTargetModel() == null) {
            throw new IllegalArgumentException("The targetModel parameter is required for dozer endpoints");
        }
        return new DozerEndpoint(uri, this, config);
    }
    
    private void initDozerSettings() {
        try {
            GlobalSettings settings = GlobalSettings.getInstance();
            LOG.info("Configuring GlobalSettings to enable EL");
            Field field = settings.getClass().getDeclaredField("elEnabled");
            ReflectionHelper.setField(field, settings, true);
        } catch (NoSuchFieldException nsfEx) {
            throw new IllegalStateException("Failed to enable EL in global Dozer settings", nsfEx);
        }
    }
}
