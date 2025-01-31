//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.settings.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SubDirSetting complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="SubDirSetting">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="min" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         <element name="max" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         <element name="resMinWidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         <element name="resMinHeight" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         <element name="resMaxWidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         <element name="resMaxHeight" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubDirSetting", propOrder = {
    "name",
    "min",
    "max",
    "resMinWidth",
    "resMinHeight",
    "resMaxWidth",
    "resMaxHeight"
})
public class SubDirSetting {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(defaultValue = "0")
    protected long min;
    @XmlElement(defaultValue = "0")
    protected long max;
    @XmlElement(defaultValue = "0")
    protected int resMinWidth;
    @XmlElement(defaultValue = "0")
    protected int resMinHeight;
    @XmlElement(defaultValue = "0")
    protected int resMaxWidth;
    @XmlElement(defaultValue = "0")
    protected int resMaxHeight;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the min property.
     * 
     */
    public long getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     */
    public void setMin(long value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public long getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(long value) {
        this.max = value;
    }

    /**
     * Gets the value of the resMinWidth property.
     * 
     */
    public int getResMinWidth() {
        return resMinWidth;
    }

    /**
     * Sets the value of the resMinWidth property.
     * 
     */
    public void setResMinWidth(int value) {
        this.resMinWidth = value;
    }

    /**
     * Gets the value of the resMinHeight property.
     * 
     */
    public int getResMinHeight() {
        return resMinHeight;
    }

    /**
     * Sets the value of the resMinHeight property.
     * 
     */
    public void setResMinHeight(int value) {
        this.resMinHeight = value;
    }

    /**
     * Gets the value of the resMaxWidth property.
     * 
     */
    public int getResMaxWidth() {
        return resMaxWidth;
    }

    /**
     * Sets the value of the resMaxWidth property.
     * 
     */
    public void setResMaxWidth(int value) {
        this.resMaxWidth = value;
    }

    /**
     * Gets the value of the resMaxHeight property.
     * 
     */
    public int getResMaxHeight() {
        return resMaxHeight;
    }

    /**
     * Sets the value of the resMaxHeight property.
     * 
     */
    public void setResMaxHeight(int value) {
        this.resMaxHeight = value;
    }

}
