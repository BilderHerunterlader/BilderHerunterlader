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
 * <p>Java-Klasse für FailureType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="FailureType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="COMPLETE"/>
 *     &lt;enumeration value="SLEEPING"/>
 *     &lt;enumeration value="FAILED_FILE_TEMPORARY_OFFLINE"/>
 *     &lt;enumeration value="FAILED_FILE_NOT_EXIST"/>
 *     &lt;enumeration value="FAILED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FailureType")
@XmlEnum
public enum FailureType {

    COMPLETE,
    SLEEPING,
    FAILED_FILE_TEMPORARY_OFFLINE,
    FAILED_FILE_NOT_EXIST,
    FAILED;

    public String value() {
        return name();
    }

    public static FailureType fromValue(String v) {
        return valueOf(v);
    }

}
