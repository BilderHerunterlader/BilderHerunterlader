//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.02 um 01:20:54 AM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für URLJavascriptPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="URLJavascriptPipeline">
 *   &lt;complexContent>
 *     &lt;extension base="{}URLPipeline">
 *       &lt;sequence>
 *         &lt;element name="javascriptCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "URLJavascriptPipeline", propOrder = {
    "javascriptCode"
})
public class URLJavascriptPipeline
    extends URLPipeline
{

    @XmlElement(required = true)
    protected String javascriptCode;

    /**
     * Ruft den Wert der javascriptCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavascriptCode() {
        return javascriptCode;
    }

    /**
     * Legt den Wert der javascriptCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavascriptCode(String value) {
        this.javascriptCode = value;
    }

}
