//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.19 um 01:18:26 PM CEST 
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
 * &lt;simpleType name="FilenameMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CONTAINER_URL_FILENAME_PART"/&gt;
 *     &lt;enumeration value="CONTAINER_URL"/&gt;
 *     &lt;enumeration value="THUMBNAIL_URL_FILENAME_PART"/&gt;
 *     &lt;enumeration value="THUMBNAIL_URL"/&gt;
 *     &lt;enumeration value="CONTAINER_PAGE_SOURCECODE"/&gt;
 *     &lt;enumeration value="DOWNLOAD_URL"/&gt;
 *     &lt;enumeration value="DOWNLOAD_URL_FILENAME_PART"/&gt;
 *     &lt;enumeration value="LAST_CONTAINER_URL_FILENAME_PART"/&gt;
 *     &lt;enumeration value="LAST_CONTAINER_URL"/&gt;
 *     &lt;enumeration value="FIRST_CONTAINER_PAGE_SOURCECODE"/&gt;
 *     &lt;enumeration value="LAST_CONTAINER_PAGE_SOURCECODE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
