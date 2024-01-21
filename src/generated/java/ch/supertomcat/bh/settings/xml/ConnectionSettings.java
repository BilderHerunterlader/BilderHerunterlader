//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.01.21 at 04:23:47 AM CET 
//


package ch.supertomcat.bh.settings.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConnectionSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConnectionSettings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="maxConnections" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="maxConnectionsPerHost" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="connectTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="socketTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="connectionRequestTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="proxy" type="{}ProxySettings"/&gt;
 *         &lt;element name="userAgent" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="browserCookiesMode" type="{}BrowserCookiesMode"/&gt;
 *         &lt;element name="browserCookiesOpera" type="{}BrowserCookiesSetting"/&gt;
 *         &lt;element name="browserCookiesOperaNew" type="{}BrowserCookiesSetting"/&gt;
 *         &lt;element name="browserCookiesFirefox" type="{}BrowserCookiesSetting"/&gt;
 *         &lt;element name="browserCookiesPaleMoon" type="{}BrowserCookiesSetting"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectionSettings", propOrder = {
    "maxConnections",
    "maxConnectionsPerHost",
    "connectTimeout",
    "socketTimeout",
    "connectionRequestTimeout",
    "proxy",
    "userAgent",
    "browserCookiesMode",
    "browserCookiesOpera",
    "browserCookiesOperaNew",
    "browserCookiesFirefox",
    "browserCookiesPaleMoon"
})
public class ConnectionSettings {

    @XmlElement(defaultValue = "32")
    protected int maxConnections;
    @XmlElement(defaultValue = "8")
    protected int maxConnectionsPerHost;
    @XmlElement(defaultValue = "60000")
    protected int connectTimeout;
    @XmlElement(defaultValue = "60000")
    protected int socketTimeout;
    @XmlElement(defaultValue = "60000")
    protected int connectionRequestTimeout;
    @XmlElement(required = true)
    protected ProxySettings proxy;
    @XmlElement(required = true, defaultValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:5.1) Goanna/20220507 PaleMoon/31.0.0")
    protected String userAgent;
    @XmlElement(required = true, defaultValue = "NO_COOKIES")
    @XmlSchemaType(name = "string")
    protected BrowserCookiesMode browserCookiesMode;
    @XmlElement(required = true)
    protected BrowserCookiesSetting browserCookiesOpera;
    @XmlElement(required = true)
    protected BrowserCookiesSetting browserCookiesOperaNew;
    @XmlElement(required = true)
    protected BrowserCookiesSetting browserCookiesFirefox;
    @XmlElement(required = true)
    protected BrowserCookiesSetting browserCookiesPaleMoon;

    /**
     * Gets the value of the maxConnections property.
     * 
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets the value of the maxConnections property.
     * 
     */
    public void setMaxConnections(int value) {
        this.maxConnections = value;
    }

    /**
     * Gets the value of the maxConnectionsPerHost property.
     * 
     */
    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    /**
     * Sets the value of the maxConnectionsPerHost property.
     * 
     */
    public void setMaxConnectionsPerHost(int value) {
        this.maxConnectionsPerHost = value;
    }

    /**
     * Gets the value of the connectTimeout property.
     * 
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the value of the connectTimeout property.
     * 
     */
    public void setConnectTimeout(int value) {
        this.connectTimeout = value;
    }

    /**
     * Gets the value of the socketTimeout property.
     * 
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the value of the socketTimeout property.
     * 
     */
    public void setSocketTimeout(int value) {
        this.socketTimeout = value;
    }

    /**
     * Gets the value of the connectionRequestTimeout property.
     * 
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Sets the value of the connectionRequestTimeout property.
     * 
     */
    public void setConnectionRequestTimeout(int value) {
        this.connectionRequestTimeout = value;
    }

    /**
     * Gets the value of the proxy property.
     * 
     * @return
     *     possible object is
     *     {@link ProxySettings }
     *     
     */
    public ProxySettings getProxy() {
        return proxy;
    }

    /**
     * Sets the value of the proxy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProxySettings }
     *     
     */
    public void setProxy(ProxySettings value) {
        this.proxy = value;
    }

    /**
     * Gets the value of the userAgent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the value of the userAgent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserAgent(String value) {
        this.userAgent = value;
    }

    /**
     * Gets the value of the browserCookiesMode property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserCookiesMode }
     *     
     */
    public BrowserCookiesMode getBrowserCookiesMode() {
        return browserCookiesMode;
    }

    /**
     * Sets the value of the browserCookiesMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserCookiesMode }
     *     
     */
    public void setBrowserCookiesMode(BrowserCookiesMode value) {
        this.browserCookiesMode = value;
    }

    /**
     * Gets the value of the browserCookiesOpera property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public BrowserCookiesSetting getBrowserCookiesOpera() {
        return browserCookiesOpera;
    }

    /**
     * Sets the value of the browserCookiesOpera property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public void setBrowserCookiesOpera(BrowserCookiesSetting value) {
        this.browserCookiesOpera = value;
    }

    /**
     * Gets the value of the browserCookiesOperaNew property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public BrowserCookiesSetting getBrowserCookiesOperaNew() {
        return browserCookiesOperaNew;
    }

    /**
     * Sets the value of the browserCookiesOperaNew property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public void setBrowserCookiesOperaNew(BrowserCookiesSetting value) {
        this.browserCookiesOperaNew = value;
    }

    /**
     * Gets the value of the browserCookiesFirefox property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public BrowserCookiesSetting getBrowserCookiesFirefox() {
        return browserCookiesFirefox;
    }

    /**
     * Sets the value of the browserCookiesFirefox property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public void setBrowserCookiesFirefox(BrowserCookiesSetting value) {
        this.browserCookiesFirefox = value;
    }

    /**
     * Gets the value of the browserCookiesPaleMoon property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public BrowserCookiesSetting getBrowserCookiesPaleMoon() {
        return browserCookiesPaleMoon;
    }

    /**
     * Sets the value of the browserCookiesPaleMoon property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserCookiesSetting }
     *     
     */
    public void setBrowserCookiesPaleMoon(BrowserCookiesSetting value) {
        this.browserCookiesPaleMoon = value;
    }

}
