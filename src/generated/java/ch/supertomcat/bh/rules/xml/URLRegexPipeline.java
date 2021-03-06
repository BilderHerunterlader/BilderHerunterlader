//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.19 um 01:18:26 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r URLRegexPipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="URLRegexPipeline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}URLPipeline"&gt;
 *       &lt;attribute name="mode" use="required" type="{}URLRegexPipelineMode" /&gt;
 *       &lt;attribute name="urlMode" type="{}URLMode" default="CONTAINER_URL" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "URLRegexPipeline")
public class URLRegexPipeline
    extends URLPipeline
{

    @XmlAttribute(name = "mode", required = true)
    protected URLRegexPipelineMode mode;
    @XmlAttribute(name = "urlMode")
    protected URLMode urlMode;

    /**
     * Ruft den Wert der mode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link URLRegexPipelineMode }
     *     
     */
    public URLRegexPipelineMode getMode() {
        return mode;
    }

    /**
     * Legt den Wert der mode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link URLRegexPipelineMode }
     *     
     */
    public void setMode(URLRegexPipelineMode value) {
        this.mode = value;
    }

    /**
     * Ruft den Wert der urlMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link URLMode }
     *     
     */
    public URLMode getUrlMode() {
        if (urlMode == null) {
            return URLMode.CONTAINER_URL;
        } else {
            return urlMode;
        }
    }

    /**
     * Legt den Wert der urlMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link URLMode }
     *     
     */
    public void setUrlMode(URLMode value) {
        this.urlMode = value;
    }

}
