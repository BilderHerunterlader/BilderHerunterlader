//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.02.29 at 08:44:08 PM CET 
//


package ch.supertomcat.bh.settings.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostsSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostsSettings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="rulesBeforeClasses" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="deactivations" type="{}HostDeactivationSetting" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostsSettings", propOrder = {
    "rulesBeforeClasses",
    "deactivations"
})
public class HostsSettings {

    @XmlElement(defaultValue = "false")
    protected boolean rulesBeforeClasses;
    protected List<HostDeactivationSetting> deactivations;

    /**
     * Gets the value of the rulesBeforeClasses property.
     * 
     */
    public boolean isRulesBeforeClasses() {
        return rulesBeforeClasses;
    }

    /**
     * Sets the value of the rulesBeforeClasses property.
     * 
     */
    public void setRulesBeforeClasses(boolean value) {
        this.rulesBeforeClasses = value;
    }

    /**
     * Gets the value of the deactivations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deactivations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeactivations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostDeactivationSetting }
     * 
     * 
     */
    public List<HostDeactivationSetting> getDeactivations() {
        if (deactivations == null) {
            deactivations = new ArrayList<HostDeactivationSetting>();
        }
        return this.deactivations;
    }

}
