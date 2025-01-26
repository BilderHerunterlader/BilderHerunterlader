//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.settings.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for AllowedFilenameCharacters</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="AllowedFilenameCharacters">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="ASCII_ONLY"/>
 *     <enumeration value="ASCII_UMLAUT"/>
 *     <enumeration value="ALL"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
