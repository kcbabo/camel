package org.apache.camel.component.dozer.example;

/**
 * Custom mapping class with one operation.
 */
public class CustomMapper {

    public Object mapCustomer(String source) {
        return "mapCustomer:" + source;
    }
}
