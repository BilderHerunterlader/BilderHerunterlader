//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.02 um 01:20:54 AM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für URLPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="URLPipeline">
 *   &lt;complexContent>
 *     &lt;extension base="{}Pipeline">
 *       &lt;attribute name="waitBeforeExecute" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="urlDecodeResult" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="sendCookies" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "URLPipeline")
@XmlSeeAlso({
    URLRegexPipeline.class,
    URLJavascriptPipeline.class
})
public class URLPipeline
    extends Pipeline
{

    @XmlAttribute(name = "waitBeforeExecute", required = true)
    protected int waitBeforeExecute;
    @XmlAttribute(name = "urlDecodeResult", required = true)
    protected boolean urlDecodeResult;
    @XmlAttribute(name = "sendCookies", required = true)
    protected boolean sendCookies;

    /**
     * Ruft den Wert der waitBeforeExecute-Eigenschaft ab.
     * 
     */
    public int getWaitBeforeExecute() {
        return waitBeforeExecute;
    }

    /**
     * Legt den Wert der waitBeforeExecute-Eigenschaft fest.
     * 
     */
    public void setWaitBeforeExecute(int value) {
        this.waitBeforeExecute = value;
    }

    /**
     * Ruft den Wert der urlDecodeResult-Eigenschaft ab.
     * 
     */
    public boolean isUrlDecodeResult() {
        return urlDecodeResult;
    }

    /**
     * Legt den Wert der urlDecodeResult-Eigenschaft fest.
     * 
     */
    public void setUrlDecodeResult(boolean value) {
        this.urlDecodeResult = value;
    }

    /**
     * Ruft den Wert der sendCookies-Eigenschaft ab.
     * 
     */
    public boolean isSendCookies() {
        return sendCookies;
    }

    /**
     * Legt den Wert der sendCookies-Eigenschaft fest.
     * 
     */
    public void setSendCookies(boolean value) {
        this.sendCookies = value;
    }

}
