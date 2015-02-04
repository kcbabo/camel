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

import java.lang.reflect.Method;

import org.dozer.ConfigurableCustomConverter;

public class CustomMapper implements ConfigurableCustomConverter {
    
    private String mappingClass;
    private String mappingOperation;
    
    @Override
    public Object convert(Object existingDestinationFieldValue, 
            Object sourceFieldValue, 
            Class<?> destinationClass,
            Class<?> sourceClass) {
        return mapCustom(sourceFieldValue);
    }
    
    public String getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(String mappingClass) {
        this.mappingClass = mappingClass;
    }
    
    public String getMappingOperation() {
        return mappingOperation;
    }

    public void setMappingOperation(String mappingOperation) {
        this.mappingOperation = mappingOperation;
    }

    @Override
    public void setParameter(String parameter) {
        String[] params = parameter.split(",");
        mappingClass = params[0];
        mappingOperation = params.length > 1 ? params[1] : null;
    }

    private Object mapCustom(Object source) {
        Class<?> customClass;
        Object customMapObj;
        Method mapMethod = null;
        
        try {
            customClass = Class.forName(mappingClass);
            customMapObj = customClass.newInstance();
             
            // If a specific mapping operation has been supplied use that
            if (mappingOperation != null) {
                mapMethod = customClass.getMethod(mappingOperation, source.getClass());
            } else {
                for (Method m : customClass.getMethods()) {
                    if (m.getReturnType() != null && m.getParameterTypes().length == 1) {
                        mapMethod = m;
                        break;
                    }
                }
            }
        } catch (Exception cnfEx) {
            throw new RuntimeException("Failed to load custom mapping", cnfEx);
        }
        
        // Verify that we found a matching method
        if (mapMethod == null) {
            throw new RuntimeException("No eligible custom mapping methods in " + mappingClass);
        }
        
        // Invoke the custom mapping method
        try {
            return mapMethod.invoke(customMapObj, source);
        } catch (Exception ex) {
            throw new RuntimeException("Error while invoking custom mapping", ex);
        }
    }
}
