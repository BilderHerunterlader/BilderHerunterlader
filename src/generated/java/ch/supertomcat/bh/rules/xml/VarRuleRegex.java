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
 * <p>Java class for VarRuleRegex complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="VarRuleRegex">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="regexp" type="{}RuleRegex" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="variableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VarRuleRegex", propOrder = {
    "regexp"
})
public class VarRuleRegex {

    protected List<RuleRegex> regexp;
    @XmlAttribute(name = "variableName", required = true)
    protected String variableName;

    /**
     * Gets the value of the regexp property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regexp property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRegexp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RuleRegex }
     * </p>
     * 
     * 
     * @return
     *     The value of the regexp property.
     */
    public List<RuleRegex> getRegexp() {
        if (regexp == null) {
            regexp = new ArrayList<>();
        }
        return this.regexp;
    }

    /**
     * Gets the value of the variableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Sets the value of the variableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVariableName(String value) {
        this.variableName = value;
    }

}
