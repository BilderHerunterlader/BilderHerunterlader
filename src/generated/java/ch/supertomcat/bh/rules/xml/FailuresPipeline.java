//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.19 um 01:18:26 PM CEST 
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
