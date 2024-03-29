//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.01.24 at 09:48:27 PM CET 
//


package ch.supertomcat.bh.rules.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for URLRegexPipeline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="URLRegexPipeline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}URLPipeline"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="varRegexp" type="{}VarRuleRegex" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
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
@XmlType(name = "URLRegexPipeline", propOrder = {
    "varRegexp"
})
public class URLRegexPipeline
    extends URLPipeline
{

    protected List<VarRuleRegex> varRegexp;
    @XmlAttribute(name = "mode", required = true)
    protected URLRegexPipelineMode mode;
    @XmlAttribute(name = "urlMode")
    protected URLMode urlMode;

    /**
     * Gets the value of the varRegexp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the varRegexp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVarRegexp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VarRuleRegex }
     * 
     * 
     */
    public List<VarRuleRegex> getVarRegexp() {
        if (varRegexp == null) {
            varRegexp = new ArrayList<VarRuleRegex>();
        }
        return this.varRegexp;
    }

    /**
     * Gets the value of the mode property.
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
     * Sets the value of the mode property.
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
     * Gets the value of the urlMode property.
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
     * Sets the value of the urlMode property.
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
