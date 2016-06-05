package com.morenkov.restlogger.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author emorenkov
 */
@Entity
public class ApplicationProperty {

    @Id
    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "property_value")
    private String propertyValue;


    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
