//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.06 um 11:08:45 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für FailuresPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FailuresPipeline">
 *   &lt;complexContent>
 *     &lt;extension base="{}Pipeline">
 *       &lt;attribute name="failureType" use="required" type="{}FailureType" />
 *       &lt;attribute name="checkURL" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="checkThumbURL" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="checkPageSourceCode" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
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
     * Ruft den Wert der failureType-Eigenschaft ab.
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
     * Legt den Wert der failureType-Eigenschaft fest.
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
     * Ruft den Wert der checkURL-Eigenschaft ab.
     * 
     */
    public boolean isCheckURL() {
        return checkURL;
    }

    /**
     * Legt den Wert der checkURL-Eigenschaft fest.
     * 
     */
    public void setCheckURL(boolean value) {
        this.checkURL = value;
    }

    /**
     * Ruft den Wert der checkThumbURL-Eigenschaft ab.
     * 
     */
    public boolean isCheckThumbURL() {
        return checkThumbURL;
    }

    /**
     * Legt den Wert der checkThumbURL-Eigenschaft fest.
     * 
     */
    public void setCheckThumbURL(boolean value) {
        this.checkThumbURL = value;
    }

    /**
     * Ruft den Wert der checkPageSourceCode-Eigenschaft ab.
     * 
     */
    public boolean isCheckPageSourceCode() {
        return checkPageSourceCode;
    }

    /**
     * Legt den Wert der checkPageSourceCode-Eigenschaft fest.
     * 
     */
    public void setCheckPageSourceCode(boolean value) {
        this.checkPageSourceCode = value;
    }

}
