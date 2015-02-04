package org.apache.camel.component.dozer;

import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

@UriParams
public class DozerConfiguration {
    
    public static final String DEFAULT_MAPPING_PATH = "dozerBeanMapping.xml";

    @UriPath
    private String name;
    @UriParam
    private String marshalId;
    @UriParam
    private String unmarshalId;
    @UriParam
    private String sourceModel;
    @UriParam
    private String targetModel;
    @UriParam(defaultValue = DEFAULT_MAPPING_PATH)
    private String mappingFile;
    
    public DozerConfiguration() {
        setMappingFile(DEFAULT_MAPPING_PATH);
    }
    
    public String getMarshalId() {
        return marshalId;
    }

    public void setMarshalId(String marshalId) {
        this.marshalId = marshalId;
    }

    public String getUnmarshalId() {
        return unmarshalId;
    }

    public void setUnmarshalId(String unmarshalId) {
        this.unmarshalId = unmarshalId;
    }

    public String getSourceModel() {
        return sourceModel;
    }

    public void setSourceModel(String sourceModel) {
        this.sourceModel = sourceModel;
    }

    public String getTargetModel() {
        return targetModel;
    }

    public void setTargetModel(String targetModel) {
        this.targetModel = targetModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }
}
