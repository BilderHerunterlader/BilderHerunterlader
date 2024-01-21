//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.01.21 at 04:23:47 AM CET 
//


package ch.supertomcat.bh.settings.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DirectorySettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DirectorySettings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="savePath" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="rememberLastUsedPath" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="lastUsedImportPath" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="lastUsedExportPath" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="autoTargetDir" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="autoTargetDirMode" type="{}AutoTargetDirMode"/&gt;
 *         &lt;element name="subDirsEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="subDirsResolutionMode" type="{}SubdirsResolutionMode"/&gt;
 *         &lt;element name="subDirSettings" type="{}SubDirSetting" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DirectorySettings", propOrder = {
    "savePath",
    "rememberLastUsedPath",
    "lastUsedImportPath",
    "lastUsedExportPath",
    "autoTargetDir",
    "autoTargetDirMode",
    "subDirsEnabled",
    "subDirsResolutionMode",
    "subDirSettings"
})
public class DirectorySettings {

    @XmlElement(required = true)
    protected String savePath;
    @XmlElement(defaultValue = "false")
    protected boolean rememberLastUsedPath;
    @XmlElement(required = true, nillable = true)
    protected String lastUsedImportPath;
    @XmlElement(required = true, nillable = true)
    protected String lastUsedExportPath;
    @XmlElement(defaultValue = "false")
    protected boolean autoTargetDir;
    @XmlElement(required = true, defaultValue = "BY_TITLE")
    @XmlSchemaType(name = "string")
    protected AutoTargetDirMode autoTargetDirMode;
    @XmlElement(defaultValue = "false")
    protected boolean subDirsEnabled;
    @XmlElement(required = true, defaultValue = "RESOLUTION_ONLY_LOWER")
    @XmlSchemaType(name = "string")
    protected SubdirsResolutionMode subDirsResolutionMode;
    protected List<SubDirSetting> subDirSettings;

    /**
     * Gets the value of the savePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSavePath() {
        return savePath;
    }

    /**
     * Sets the value of the savePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSavePath(String value) {
        this.savePath = value;
    }

    /**
     * Gets the value of the rememberLastUsedPath property.
     * 
     */
    public boolean isRememberLastUsedPath() {
        return rememberLastUsedPath;
    }

    /**
     * Sets the value of the rememberLastUsedPath property.
     * 
     */
    public void setRememberLastUsedPath(boolean value) {
        this.rememberLastUsedPath = value;
    }

    /**
     * Gets the value of the lastUsedImportPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastUsedImportPath() {
        return lastUsedImportPath;
    }

    /**
     * Sets the value of the lastUsedImportPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastUsedImportPath(String value) {
        this.lastUsedImportPath = value;
    }

    /**
     * Gets the value of the lastUsedExportPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastUsedExportPath() {
        return lastUsedExportPath;
    }

    /**
     * Sets the value of the lastUsedExportPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastUsedExportPath(String value) {
        this.lastUsedExportPath = value;
    }

    /**
     * Gets the value of the autoTargetDir property.
     * 
     */
    public boolean isAutoTargetDir() {
        return autoTargetDir;
    }

    /**
     * Sets the value of the autoTargetDir property.
     * 
     */
    public void setAutoTargetDir(boolean value) {
        this.autoTargetDir = value;
    }

    /**
     * Gets the value of the autoTargetDirMode property.
     * 
     * @return
     *     possible object is
     *     {@link AutoTargetDirMode }
     *     
     */
    public AutoTargetDirMode getAutoTargetDirMode() {
        return autoTargetDirMode;
    }

    /**
     * Sets the value of the autoTargetDirMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AutoTargetDirMode }
     *     
     */
    public void setAutoTargetDirMode(AutoTargetDirMode value) {
        this.autoTargetDirMode = value;
    }

    /**
     * Gets the value of the subDirsEnabled property.
     * 
     */
    public boolean isSubDirsEnabled() {
        return subDirsEnabled;
    }

    /**
     * Sets the value of the subDirsEnabled property.
     * 
     */
    public void setSubDirsEnabled(boolean value) {
        this.subDirsEnabled = value;
    }

    /**
     * Gets the value of the subDirsResolutionMode property.
     * 
     * @return
     *     possible object is
     *     {@link SubdirsResolutionMode }
     *     
     */
    public SubdirsResolutionMode getSubDirsResolutionMode() {
        return subDirsResolutionMode;
    }

    /**
     * Sets the value of the subDirsResolutionMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubdirsResolutionMode }
     *     
     */
    public void setSubDirsResolutionMode(SubdirsResolutionMode value) {
        this.subDirsResolutionMode = value;
    }

    /**
     * Gets the value of the subDirSettings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subDirSettings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubDirSettings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubDirSetting }
     * 
     * 
     */
    public List<SubDirSetting> getSubDirSettings() {
        if (subDirSettings == null) {
            subDirSettings = new ArrayList<SubDirSetting>();
        }
        return this.subDirSettings;
    }

}
