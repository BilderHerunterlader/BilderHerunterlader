//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.01.22 at 09:37:34 PM CET 
//


package ch.supertomcat.bh.settings.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProgressDisplayMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProgressDisplayMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="PROGRESSBAR_PERCENT"/&gt;
 *     &lt;enumeration value="PROGRESSBAR_SIZE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ProgressDisplayMode")
@XmlEnum
public enum ProgressDisplayMode {

    PROGRESSBAR_PERCENT,
    PROGRESSBAR_SIZE;

    public String value() {
        return name();
    }

    public static ProgressDisplayMode fromValue(String v) {
        return valueOf(v);
    }

}
