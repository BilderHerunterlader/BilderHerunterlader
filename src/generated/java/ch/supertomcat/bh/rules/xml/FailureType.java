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
 * <p>Java-Klasse für FailureType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="FailureType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="COMPLETE"/&gt;
 *     &lt;enumeration value="SLEEPING"/&gt;
 *     &lt;enumeration value="FAILED_FILE_TEMPORARY_OFFLINE"/&gt;
 *     &lt;enumeration value="FAILED_FILE_NOT_EXIST"/&gt;
 *     &lt;enumeration value="FAILED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
