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
 * <p>Java-Klasse für FilenameMode.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="FilenameMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CONTAINER_URL_FILENAME_PART"/>
 *     &lt;enumeration value="CONTAINER_URL"/>
 *     &lt;enumeration value="THUMBNAIL_URL_FILENAME_PART"/>
 *     &lt;enumeration value="THUMBNAIL_URL"/>
 *     &lt;enumeration value="CONTAINER_PAGE_SOURCECODE"/>
 *     &lt;enumeration value="DOWNLOAD_URL"/>
 *     &lt;enumeration value="DOWNLOAD_URL_FILENAME_PART"/>
 *     &lt;enumeration value="LAST_CONTAINER_URL_FILENAME_PART"/>
 *     &lt;enumeration value="LAST_CONTAINER_URL"/>
 *     &lt;enumeration value="FIRST_CONTAINER_PAGE_SOURCECODE"/>
 *     &lt;enumeration value="LAST_CONTAINER_PAGE_SOURCECODE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FilenameMode")
@XmlEnum
public enum FilenameMode {

    CONTAINER_URL_FILENAME_PART,
    CONTAINER_URL,
    THUMBNAIL_URL_FILENAME_PART,
    THUMBNAIL_URL,
    CONTAINER_PAGE_SOURCECODE,
    DOWNLOAD_URL,
    DOWNLOAD_URL_FILENAME_PART,
    LAST_CONTAINER_URL_FILENAME_PART,
    LAST_CONTAINER_URL,
    FIRST_CONTAINER_PAGE_SOURCECODE,
    LAST_CONTAINER_PAGE_SOURCECODE;

    public String value() {
        return name();
    }

    public static FilenameMode fromValue(String v) {
        return valueOf(v);
    }

}
