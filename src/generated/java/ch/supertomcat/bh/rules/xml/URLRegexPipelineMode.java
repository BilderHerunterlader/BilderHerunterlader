//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.06 um 11:08:45 PM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für URLRegexPipelineMode.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="URLRegexPipelineMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CONTAINER_OR_THUMBNAIL_URL"/>
 *     &lt;enumeration value="CONTAINER_PAGE_SOURCECODE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "URLRegexPipelineMode")
@XmlEnum
public enum URLRegexPipelineMode {

    CONTAINER_OR_THUMBNAIL_URL,
    CONTAINER_PAGE_SOURCECODE;

    public String value() {
        return name();
    }

    public static URLRegexPipelineMode fromValue(String v) {
        return valueOf(v);
    }

}
