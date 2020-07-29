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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="setuprelease" type="{}SetupRelease"/>
 *         &lt;element name="main" type="{}MainVersion"/>
 *         &lt;element name="description" type="{}VersionDescription"/>
 *         &lt;element name="redirects" type="{}Redirects"/>
 *         &lt;element name="hoster" type="{}Hoster"/>
 *         &lt;element name="changelog" type="{}ChangeLog"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "setuprelease",
    "main",
    "description",
    "redirects",
    "hoster",
    "changelog"
})
@XmlRootElement(name = "updates")
public class Updates {

    @XmlElement(required = true)
    protected SetupRelease setuprelease;
    @XmlElement(required = true)
    protected MainVersion main;
    @XmlElement(required = true)
    protected VersionDescription description;
    @XmlElement(required = true)
    protected Redirects redirects;
    @XmlElement(required = true)
    protected Hoster hoster;
    @XmlElement(required = true)
    protected ChangeLog changelog;

    /**
     * Ruft den Wert der setuprelease-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SetupRelease }
     *     
     */
    public SetupRelease getSetuprelease() {
        return setuprelease;
    }

    /**
     * Legt den Wert der setuprelease-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SetupRelease }
     *     
     */
    public void setSetuprelease(SetupRelease value) {
        this.setuprelease = value;
    }

    /**
     * Ruft den Wert der main-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MainVersion }
     *     
     */
    public MainVersion getMain() {
        return main;
    }

    /**
     * Legt den Wert der main-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MainVersion }
     *     
     */
    public void setMain(MainVersion value) {
        this.main = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VersionDescription }
     *     
     */
    public VersionDescription getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionDescription }
     *     
     */
    public void setDescription(VersionDescription value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der redirects-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Redirects }
     *     
     */
    public Redirects getRedirects() {
        return redirects;
    }

    /**
     * Legt den Wert der redirects-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Redirects }
     *     
     */
    public void setRedirects(Redirects value) {
        this.redirects = value;
    }

    /**
     * Ruft den Wert der hoster-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Hoster }
     *     
     */
    public Hoster getHoster() {
        return hoster;
    }

    /**
     * Legt den Wert der hoster-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Hoster }
     *     
     */
    public void setHoster(Hoster value) {
        this.hoster = value;
    }

    /**
     * Ruft den Wert der changelog-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ChangeLog }
     *     
     */
    public ChangeLog getChangelog() {
        return changelog;
    }

    /**
     * Legt den Wert der changelog-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeLog }
     *     
     */
    public void setChangelog(ChangeLog value) {
        this.changelog = value;
    }

}
