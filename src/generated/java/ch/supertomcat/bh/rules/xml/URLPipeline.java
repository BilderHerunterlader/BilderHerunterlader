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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für URLPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="URLPipeline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}Pipeline"&gt;
 *       &lt;attribute name="waitBeforeExecute" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="urlDecodeResult" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="javascriptDecodeResult" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="sendCookies" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
    @XmlAttribute(name = "javascriptDecodeResult")
    protected Boolean javascriptDecodeResult;
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
     * Ruft den Wert der javascriptDecodeResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isJavascriptDecodeResult() {
        return javascriptDecodeResult;
    }

    /**
     * Legt den Wert der javascriptDecodeResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setJavascriptDecodeResult(Boolean value) {
        this.javascriptDecodeResult = value;
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
