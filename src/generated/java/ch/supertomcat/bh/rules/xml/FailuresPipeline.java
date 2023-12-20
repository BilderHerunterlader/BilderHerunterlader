//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.12.19 at 09:16:25 PM CET 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FailuresPipeline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FailuresPipeline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}Pipeline"&gt;
 *       &lt;attribute name="failureType" use="required" type="{}FailureType" /&gt;
 *       &lt;attribute name="checkURL" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="checkThumbURL" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="checkPageSourceCode" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FailuresPipeline")
public class FailuresPipeline
    extends Pipeline
{

    @XmlAttribute(name = "failureType", required = true)
    protected FailureType failureType;
    @XmlAttribute(name = "checkURL", required = true)
    protected boolean checkURL;
    @XmlAttribute(name = "checkThumbURL", required = true)
    protected boolean checkThumbURL;
    @XmlAttribute(name = "checkPageSourceCode", required = true)
    protected boolean checkPageSourceCode;

    /**
     * Gets the value of the failureType property.
     * 
     * @return
     *     possible object is
     *     {@link FailureType }
     *     
     */
    public FailureType getFailureType() {
        return failureType;
    }

    /**
     * Sets the value of the failureType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FailureType }
     *     
     */
    public void setFailureType(FailureType value) {
        this.failureType = value;
    }

    /**
     * Gets the value of the checkURL property.
     * 
     */
    public boolean isCheckURL() {
        return checkURL;
    }

    /**
     * Sets the value of the checkURL property.
     * 
     */
    public void setCheckURL(boolean value) {
        this.checkURL = value;
    }

    /**
     * Gets the value of the checkThumbURL property.
     * 
     */
    public boolean isCheckThumbURL() {
        return checkThumbURL;
    }

    /**
     * Sets the value of the checkThumbURL property.
     * 
     */
    public void setCheckThumbURL(boolean value) {
        this.checkThumbURL = value;
    }

    /**
     * Gets the value of the checkPageSourceCode property.
     * 
     */
    public boolean isCheckPageSourceCode() {
        return checkPageSourceCode;
    }

    /**
     * Sets the value of the checkPageSourceCode property.
     * 
     */
    public void setCheckPageSourceCode(boolean value) {
        this.checkPageSourceCode = value;
    }

}
