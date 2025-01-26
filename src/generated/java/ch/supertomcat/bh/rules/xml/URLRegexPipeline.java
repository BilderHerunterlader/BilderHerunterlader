//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.rules.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for URLRegexPipeline complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="URLRegexPipeline">
 *   <complexContent>
 *     <extension base="{}URLPipeline">
 *       <sequence>
 *         <element name="varRegexp" type="{}VarRuleRegex" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="mode" use="required" type="{}URLRegexPipelineMode" />
 *       <attribute name="urlMode" type="{}URLMode" default="CONTAINER_URL" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the varRegexp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getVarRegexp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VarRuleRegex }
     * </p>
     * 
     * 
     * @return
     *     The value of the varRegexp property.
     */
    public List<VarRuleRegex> getVarRegexp() {
        if (varRegexp == null) {
            varRegexp = new ArrayList<>();
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
