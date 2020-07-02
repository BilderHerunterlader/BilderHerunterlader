//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.02 um 01:20:54 AM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für DuplicateRemoveMode.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="DuplicateRemoveMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DEFAULT"/>
 *     &lt;enumeration value="CONTAINER_URL_ONLY"/>
 *     &lt;enumeration value="CONTAINER_URL_AND_THUMBNAIL_URL"/>
 *     &lt;enumeration value="CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST"/>
 *     &lt;enumeration value="CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST"/>
 *     &lt;enumeration value="CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST"/>
 *     &lt;enumeration value="CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DuplicateRemoveMode")
@XmlEnum
public enum DuplicateRemoveMode {

    DEFAULT,
    CONTAINER_URL_ONLY,
    CONTAINER_URL_AND_THUMBNAIL_URL,
    CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST,
    CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST,
    CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST,
    CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST;

    public String value() {
        return name();
    }

    public static DuplicateRemoveMode fromValue(String v) {
        return valueOf(v);
    }

}
