//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.19 um 01:18:26 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="urlPattern" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="useContentDisposition" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="reducePathLength" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="reduceFilenameLength" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="referrerMode" type="{}ReferrerMode"/&gt;
 *         &lt;element name="customReferrer" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="downloadReferrerMode" type="{}ReferrerMode"/&gt;
 *         &lt;element name="downloadCustomReferrer" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="duplicateRemoveMode" type="{}DuplicateRemoveMode"/&gt;
 *         &lt;element name="sendCookies" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="pipes" type="{}URLPipeline" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="failuresPipes" type="{}FailuresPipeline" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="filenamePipeline" type="{}FilenamePipeline"/&gt;
 *         &lt;element name="filenameDownloadSelectionPipeline" type="{}FilenameDownloadSelectionPipeline"/&gt;
 *         &lt;element name="restriction" type="{}Restriction"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="redirect" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="resend" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "urlPattern",
    "useContentDisposition",
    "reducePathLength",
    "reduceFilenameLength",
    "referrerMode",
    "customReferrer",
    "downloadReferrerMode",
    "downloadCustomReferrer",
    "duplicateRemoveMode",
    "sendCookies",
    "pipes",
    "failuresPipes",
    "filenamePipeline",
    "filenameDownloadSelectionPipeline",
    "restriction"
})
@XmlRootElement(name = "ruleDefinition")
public class RuleDefinition {

    @XmlElement(required = true)
    protected String urlPattern;
    protected boolean useContentDisposition;
    protected boolean reducePathLength;
    protected boolean reduceFilenameLength;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ReferrerMode referrerMode;
    @XmlElement(required = true)
    protected String customReferrer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ReferrerMode downloadReferrerMode;
    @XmlElement(required = true)
    protected String downloadCustomReferrer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DuplicateRemoveMode duplicateRemoveMode;
    protected boolean sendCookies;
    protected List<URLPipeline> pipes;
    protected List<FailuresPipeline> failuresPipes;
    @XmlElement(required = true)
    protected FilenamePipeline filenamePipeline;
    @XmlElement(required = true)
    protected FilenameDownloadSelectionPipeline filenameDownloadSelectionPipeline;
    @XmlElement(required = true)
    protected Restriction restriction;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "redirect", required = true)
    protected boolean redirect;
    @XmlAttribute(name = "resend", required = true)
    protected boolean resend;

    /**
     * Ruft den Wert der urlPattern-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlPattern() {
        return urlPattern;
    }

    /**
     * Legt den Wert der urlPattern-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlPattern(String value) {
        this.urlPattern = value;
    }

    /**
     * Ruft den Wert der useContentDisposition-Eigenschaft ab.
     * 
     */
    public boolean isUseContentDisposition() {
        return useContentDisposition;
    }

    /**
     * Legt den Wert der useContentDisposition-Eigenschaft fest.
     * 
     */
    public void setUseContentDisposition(boolean value) {
        this.useContentDisposition = value;
    }

    /**
     * Ruft den Wert der reducePathLength-Eigenschaft ab.
     * 
     */
    public boolean isReducePathLength() {
        return reducePathLength;
    }

    /**
     * Legt den Wert der reducePathLength-Eigenschaft fest.
     * 
     */
    public void setReducePathLength(boolean value) {
        this.reducePathLength = value;
    }

    /**
     * Ruft den Wert der reduceFilenameLength-Eigenschaft ab.
     * 
     */
    public boolean isReduceFilenameLength() {
        return reduceFilenameLength;
    }

    /**
     * Legt den Wert der reduceFilenameLength-Eigenschaft fest.
     * 
     */
    public void setReduceFilenameLength(boolean value) {
        this.reduceFilenameLength = value;
    }

    /**
     * Ruft den Wert der referrerMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReferrerMode }
     *     
     */
    public ReferrerMode getReferrerMode() {
        return referrerMode;
    }

    /**
     * Legt den Wert der referrerMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferrerMode }
     *     
     */
    public void setReferrerMode(ReferrerMode value) {
        this.referrerMode = value;
    }

    /**
     * Ruft den Wert der customReferrer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomReferrer() {
        return customReferrer;
    }

    /**
     * Legt den Wert der customReferrer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomReferrer(String value) {
        this.customReferrer = value;
    }

    /**
     * Ruft den Wert der downloadReferrerMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReferrerMode }
     *     
     */
    public ReferrerMode getDownloadReferrerMode() {
        return downloadReferrerMode;
    }

    /**
     * Legt den Wert der downloadReferrerMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferrerMode }
     *     
     */
    public void setDownloadReferrerMode(ReferrerMode value) {
        this.downloadReferrerMode = value;
    }

    /**
     * Ruft den Wert der downloadCustomReferrer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownloadCustomReferrer() {
        return downloadCustomReferrer;
    }

    /**
     * Legt den Wert der downloadCustomReferrer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownloadCustomReferrer(String value) {
        this.downloadCustomReferrer = value;
    }

    /**
     * Ruft den Wert der duplicateRemoveMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DuplicateRemoveMode }
     *     
     */
    public DuplicateRemoveMode getDuplicateRemoveMode() {
        return duplicateRemoveMode;
    }

    /**
     * Legt den Wert der duplicateRemoveMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DuplicateRemoveMode }
     *     
     */
    public void setDuplicateRemoveMode(DuplicateRemoveMode value) {
        this.duplicateRemoveMode = value;
    }

    /**
     * Ruft den Wert der sendCookies-Eigenschaft ab.
     * 
     */
    public boolean isSendCookies() {
        return sendCookies;
    }

    /**
     * Legt den Wert der sendCookies-Eigenschaft fest.
     * 
     */
    public void setSendCookies(boolean value) {
        this.sendCookies = value;
    }

    /**
     * Gets the value of the pipes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pipes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPipes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link URLPipeline }
     * 
     * 
     */
    public List<URLPipeline> getPipes() {
        if (pipes == null) {
            pipes = new ArrayList<URLPipeline>();
        }
        return this.pipes;
    }

    /**
     * Gets the value of the failuresPipes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the failuresPipes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFailuresPipes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FailuresPipeline }
     * 
     * 
     */
    public List<FailuresPipeline> getFailuresPipes() {
        if (failuresPipes == null) {
            failuresPipes = new ArrayList<FailuresPipeline>();
        }
        return this.failuresPipes;
    }

    /**
     * Ruft den Wert der filenamePipeline-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FilenamePipeline }
     *     
     */
    public FilenamePipeline getFilenamePipeline() {
        return filenamePipeline;
    }

    /**
     * Legt den Wert der filenamePipeline-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FilenamePipeline }
     *     
     */
    public void setFilenamePipeline(FilenamePipeline value) {
        this.filenamePipeline = value;
    }

    /**
     * Ruft den Wert der filenameDownloadSelectionPipeline-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FilenameDownloadSelectionPipeline }
     *     
     */
    public FilenameDownloadSelectionPipeline getFilenameDownloadSelectionPipeline() {
        return filenameDownloadSelectionPipeline;
    }

    /**
     * Legt den Wert der filenameDownloadSelectionPipeline-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FilenameDownloadSelectionPipeline }
     *     
     */
    public void setFilenameDownloadSelectionPipeline(FilenameDownloadSelectionPipeline value) {
        this.filenameDownloadSelectionPipeline = value;
    }

    /**
     * Ruft den Wert der restriction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Restriction }
     *     
     */
    public Restriction getRestriction() {
        return restriction;
    }

    /**
     * Legt den Wert der restriction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Restriction }
     *     
     */
    public void setRestriction(Restriction value) {
        this.restriction = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der redirect-Eigenschaft ab.
     * 
     */
    public boolean isRedirect() {
        return redirect;
    }

    /**
     * Legt den Wert der redirect-Eigenschaft fest.
     * 
     */
    public void setRedirect(boolean value) {
        this.redirect = value;
    }

    /**
     * Ruft den Wert der resend-Eigenschaft ab.
     * 
     */
    public boolean isResend() {
        return resend;
    }

    /**
     * Legt den Wert der resend-Eigenschaft fest.
     * 
     */
    public void setResend(boolean value) {
        this.resend = value;
    }

}
