//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.18 at 07:56:41 PM CEST 
//


package org.audiveris.proxymusic;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for circular-arrow.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="circular-arrow">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="clockwise"/>
 *     &lt;enumeration value="anticlockwise"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "circular-arrow")
@XmlEnum
public enum CircularArrow {

    @XmlEnumValue("clockwise")
    CLOCKWISE("clockwise"),
    @XmlEnumValue("anticlockwise")
    ANTICLOCKWISE("anticlockwise");
    private final java.lang.String value;

    CircularArrow(java.lang.String v) {
        value = v;
    }

    public java.lang.String value() {
        return value;
    }

    public static CircularArrow fromValue(java.lang.String v) {
        for (CircularArrow c: CircularArrow.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
