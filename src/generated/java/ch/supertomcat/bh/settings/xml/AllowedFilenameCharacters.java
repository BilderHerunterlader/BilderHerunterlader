//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.02.29 at 08:44:08 PM CET 
//


package ch.supertomcat.bh.settings.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AllowedFilenameCharacters.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AllowedFilenameCharacters"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ASCII_ONLY"/&gt;
 *     &lt;enumeration value="ASCII_UMLAUT"/&gt;
 *     &lt;enumeration value="ALL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AllowedFilenameCharacters")
@XmlEnum
public enum AllowedFilenameCharacters {

    ASCII_ONLY,
    ASCII_UMLAUT,
    ALL;

    public String value() {
        return name();
    }

    public static AllowedFilenameCharacters fromValue(String v) {
        return valueOf(v);
    }

}
