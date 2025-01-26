//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.rules.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for FilenameMode</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="FilenameMode">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="CONTAINER_URL_FILENAME_PART"/>
 *     <enumeration value="CONTAINER_URL"/>
 *     <enumeration value="THUMBNAIL_URL_FILENAME_PART"/>
 *     <enumeration value="THUMBNAIL_URL"/>
 *     <enumeration value="CONTAINER_PAGE_SOURCECODE"/>
 *     <enumeration value="DOWNLOAD_URL"/>
 *     <enumeration value="DOWNLOAD_URL_FILENAME_PART"/>
 *     <enumeration value="LAST_CONTAINER_URL_FILENAME_PART"/>
 *     <enumeration value="LAST_CONTAINER_URL"/>
 *     <enumeration value="FIRST_CONTAINER_PAGE_SOURCECODE"/>
 *     <enumeration value="LAST_CONTAINER_PAGE_SOURCECODE"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
