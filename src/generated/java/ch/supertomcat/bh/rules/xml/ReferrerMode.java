//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.06 um 10:52:58 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ReferrerMode.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="ReferrerMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NO_REFERRER"/>
 *     &lt;enumeration value="LAST_CONTAINER_URL"/>
 *     &lt;enumeration value="FIRST_CONTAINER_URL"/>
 *     &lt;enumeration value="ORIGIN_PAGE"/>
 *     &lt;enumeration value="CUSTOM"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReferrerMode")
@XmlEnum
public enum ReferrerMode {

    NO_REFERRER,
    LAST_CONTAINER_URL,
    FIRST_CONTAINER_URL,
    ORIGIN_PAGE,
    CUSTOM;

    public String value() {
        return name();
    }

    public static ReferrerMode fromValue(String v) {
        return valueOf(v);
    }

}
