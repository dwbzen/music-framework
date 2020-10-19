//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.18 at 07:56:41 PM CEST 
//


package org.audiveris.proxymusic;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * The measure-repeat type is used for both single and multiple measure repeats. The text of the element indicates the number of measures to be repeated in a single pattern. The slashes attribute specifies the number of slashes to use in the repeat sign. It is 1 if not specified. Both the start and the stop of the measure-repeat must be specified. The text of the element is ignored when the type is stop.
 * 	
 * The measure-repeat element specifies a notation style for repetitions. The actual music being repeated needs to be repeated within the MusicXML file. This element specifies the notation that indicates the repeat.
 * 
 * <p>Java class for measure-repeat complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="measure-repeat">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>positive-integer-or-empty">
 *       &lt;attribute name="type" use="required" type="{}start-stop" />
 *       &lt;attribute name="slashes" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "measure-repeat", propOrder = {
    "value"
})
public class MeasureRepeat {

    @XmlValue
    protected java.lang.String value;
    @XmlAttribute(name = "type", required = true)
    protected StartStop type;
    @XmlAttribute(name = "slashes")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger slashes;

    /**
     * The positive-integer-or-empty values can be either a positive integer or an empty string.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link StartStop }
     *     
     */
    public StartStop getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link StartStop }
     *     
     */
    public void setType(StartStop value) {
        this.type = value;
    }

    /**
     * Gets the value of the slashes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSlashes() {
        return slashes;
    }

    /**
     * Sets the value of the slashes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSlashes(BigInteger value) {
        this.slashes = value;
    }

}
