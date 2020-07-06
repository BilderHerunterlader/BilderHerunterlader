//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.06 um 10:52:58 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für FilenameDownloadSelectionPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FilenameDownloadSelectionPipeline">
 *   &lt;complexContent>
 *     &lt;extension base="{}Pipeline">
 *       &lt;attribute name="mode" use="required" type="{}FilenameDownloadSelectionMode" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilenameDownloadSelectionPipeline")
public class FilenameDownloadSelectionPipeline
    extends Pipeline
{

    @XmlAttribute(name = "mode", required = true)
    protected FilenameDownloadSelectionMode mode;

    /**
     * Ruft den Wert der mode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FilenameDownloadSelectionMode }
     *     
     */
    public FilenameDownloadSelectionMode getMode() {
        return mode;
    }

    /**
     * Legt den Wert der mode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FilenameDownloadSelectionMode }
     *     
     */
    public void setMode(FilenameDownloadSelectionMode value) {
        this.mode = value;
    }

}
