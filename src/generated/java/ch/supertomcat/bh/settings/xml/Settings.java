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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="directorySettings" type="{}DirectorySettings"/&gt;
 *         &lt;element name="connectionSettings" type="{}ConnectionSettings"/&gt;
 *         &lt;element name="guiSettings" type="{}GUISettings"/&gt;
 *         &lt;element name="hostsSettings" type="{}HostsSettings"/&gt;
 *         &lt;element name="downloadSettings" type="{}DownloadSettings"/&gt;
 *         &lt;element name="keywordsSettings" type="{}KeywordsSettings"/&gt;
 *         &lt;element name="checkForUpdatesOnStart" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="checkClipboard" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="webExtensionPort" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="backupDbOnStart" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="defragDBOnStart" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="defragMinFilesize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="threadCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="logLevel" type="{}LogLevelSetting"/&gt;
 *         &lt;element name="hosterSettings" type="{}HosterSettings" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "directorySettings",
    "connectionSettings",
    "guiSettings",
    "hostsSettings",
    "downloadSettings",
    "keywordsSettings",
    "checkForUpdatesOnStart",
    "checkClipboard",
    "webExtensionPort",
    "backupDbOnStart",
    "defragDBOnStart",
    "defragMinFilesize",
    "threadCount",
    "logLevel",
    "hosterSettings"
})
@XmlRootElement(name = "settings")
public class Settings {

    @XmlElement(required = true)
    protected DirectorySettings directorySettings;
    @XmlElement(required = true)
    protected ConnectionSettings connectionSettings;
    @XmlElement(required = true)
    protected GUISettings guiSettings;
    @XmlElement(required = true)
    protected HostsSettings hostsSettings;
    @XmlElement(required = true)
    protected DownloadSettings downloadSettings;
    @XmlElement(required = true)
    protected KeywordsSettings keywordsSettings;
    @XmlElement(defaultValue = "false")
    protected boolean checkForUpdatesOnStart;
    @XmlElement(defaultValue = "false")
    protected boolean checkClipboard;
    @XmlElement(defaultValue = "35990")
    protected int webExtensionPort;
    @XmlElement(defaultValue = "true")
    protected boolean backupDbOnStart;
    @XmlElement(defaultValue = "true")
    protected boolean defragDBOnStart;
    @XmlElement(defaultValue = "5000000")
    protected long defragMinFilesize;
    @XmlElement(defaultValue = "1")
    protected int threadCount;
    @XmlElement(required = true, defaultValue = "INFO")
    @XmlSchemaType(name = "string")
    protected LogLevelSetting logLevel;
    protected List<HosterSettings> hosterSettings;

    /**
     * Gets the value of the directorySettings property.
     * 
     * @return
     *     possible object is
     *     {@link DirectorySettings }
     *     
     */
    public DirectorySettings getDirectorySettings() {
        return directorySettings;
    }

    /**
     * Sets the value of the directorySettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectorySettings }
     *     
     */
    public void setDirectorySettings(DirectorySettings value) {
        this.directorySettings = value;
    }

    /**
     * Gets the value of the connectionSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionSettings }
     *     
     */
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    /**
     * Sets the value of the connectionSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionSettings }
     *     
     */
    public void setConnectionSettings(ConnectionSettings value) {
        this.connectionSettings = value;
    }

    /**
     * Gets the value of the guiSettings property.
     * 
     * @return
     *     possible object is
     *     {@link GUISettings }
     *     
     */
    public GUISettings getGuiSettings() {
        return guiSettings;
    }

    /**
     * Sets the value of the guiSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link GUISettings }
     *     
     */
    public void setGuiSettings(GUISettings value) {
        this.guiSettings = value;
    }

    /**
     * Gets the value of the hostsSettings property.
     * 
     * @return
     *     possible object is
     *     {@link HostsSettings }
     *     
     */
    public HostsSettings getHostsSettings() {
        return hostsSettings;
    }

    /**
     * Sets the value of the hostsSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostsSettings }
     *     
     */
    public void setHostsSettings(HostsSettings value) {
        this.hostsSettings = value;
    }

    /**
     * Gets the value of the downloadSettings property.
     * 
     * @return
     *     possible object is
     *     {@link DownloadSettings }
     *     
     */
    public DownloadSettings getDownloadSettings() {
        return downloadSettings;
    }

    /**
     * Sets the value of the downloadSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link DownloadSettings }
     *     
     */
    public void setDownloadSettings(DownloadSettings value) {
        this.downloadSettings = value;
    }

    /**
     * Gets the value of the keywordsSettings property.
     * 
     * @return
     *     possible object is
     *     {@link KeywordsSettings }
     *     
     */
    public KeywordsSettings getKeywordsSettings() {
        return keywordsSettings;
    }

    /**
     * Sets the value of the keywordsSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeywordsSettings }
     *     
     */
    public void setKeywordsSettings(KeywordsSettings value) {
        this.keywordsSettings = value;
    }

    /**
     * Gets the value of the checkForUpdatesOnStart property.
     * 
     */
    public boolean isCheckForUpdatesOnStart() {
        return checkForUpdatesOnStart;
    }

    /**
     * Sets the value of the checkForUpdatesOnStart property.
     * 
     */
    public void setCheckForUpdatesOnStart(boolean value) {
        this.checkForUpdatesOnStart = value;
    }

    /**
     * Gets the value of the checkClipboard property.
     * 
     */
    public boolean isCheckClipboard() {
        return checkClipboard;
    }

    /**
     * Sets the value of the checkClipboard property.
     * 
     */
    public void setCheckClipboard(boolean value) {
        this.checkClipboard = value;
    }

    /**
     * Gets the value of the webExtensionPort property.
     * 
     */
    public int getWebExtensionPort() {
        return webExtensionPort;
    }

    /**
     * Sets the value of the webExtensionPort property.
     * 
     */
    public void setWebExtensionPort(int value) {
        this.webExtensionPort = value;
    }

    /**
     * Gets the value of the backupDbOnStart property.
     * 
     */
    public boolean isBackupDbOnStart() {
        return backupDbOnStart;
    }

    /**
     * Sets the value of the backupDbOnStart property.
     * 
     */
    public void setBackupDbOnStart(boolean value) {
        this.backupDbOnStart = value;
    }

    /**
     * Gets the value of the defragDBOnStart property.
     * 
     */
    public boolean isDefragDBOnStart() {
        return defragDBOnStart;
    }

    /**
     * Sets the value of the defragDBOnStart property.
     * 
     */
    public void setDefragDBOnStart(boolean value) {
        this.defragDBOnStart = value;
    }

    /**
     * Gets the value of the defragMinFilesize property.
     * 
     */
    public long getDefragMinFilesize() {
        return defragMinFilesize;
    }

    /**
     * Sets the value of the defragMinFilesize property.
     * 
     */
    public void setDefragMinFilesize(long value) {
        this.defragMinFilesize = value;
    }

    /**
     * Gets the value of the threadCount property.
     * 
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Sets the value of the threadCount property.
     * 
     */
    public void setThreadCount(int value) {
        this.threadCount = value;
    }

    /**
     * Gets the value of the logLevel property.
     * 
     * @return
     *     possible object is
     *     {@link LogLevelSetting }
     *     
     */
    public LogLevelSetting getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the value of the logLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogLevelSetting }
     *     
     */
    public void setLogLevel(LogLevelSetting value) {
        this.logLevel = value;
    }

    /**
     * Gets the value of the hosterSettings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hosterSettings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHosterSettings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HosterSettings }
     * 
     * 
     */
    public List<HosterSettings> getHosterSettings() {
        if (hosterSettings == null) {
            hosterSettings = new ArrayList<HosterSettings>();
        }
        return this.hosterSettings;
    }

}
