//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.29 um 02:02:31 AM CEST 
//


package ch.supertomcat.bh.update.sources.httpxml.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für VersionDescription complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="VersionDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mainDE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mainEN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionDescription", propOrder = {
    "mainDE",
    "mainEN"
})
public class VersionDescription {

    @XmlElement(required = true)
    protected String mainDE;
    @XmlElement(required = true)
    protected String mainEN;

    /**
     * Ruft den Wert der mainDE-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainDE() {
        return mainDE;
    }

    /**
     * Legt den Wert der mainDE-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainDE(String value) {
        this.mainDE = value;
    }

    /**
     * Ruft den Wert der mainEN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainEN() {
        return mainEN;
    }

    /**
     * Legt den Wert der mainEN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainEN(String value) {
        this.mainEN = value;
    }

}
