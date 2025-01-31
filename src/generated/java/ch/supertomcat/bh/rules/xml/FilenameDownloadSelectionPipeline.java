//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.rules.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FilenameDownloadSelectionPipeline complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FilenameDownloadSelectionPipeline">
 *   <complexContent>
 *     <extension base="{}Pipeline">
 *       <attribute name="mode" use="required" type="{}FilenameDownloadSelectionMode" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
     * Gets the value of the mode property.
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
     * Sets the value of the mode property.
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
