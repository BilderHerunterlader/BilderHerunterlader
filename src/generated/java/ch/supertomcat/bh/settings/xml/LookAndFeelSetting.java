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
 * <p>Java class for LookAndFeelSetting.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LookAndFeelSetting"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="LAF_DEFAULT"/&gt;
 *     &lt;enumeration value="LAF_OS"/&gt;
 *     &lt;enumeration value="LAF_METAL"/&gt;
 *     &lt;enumeration value="LAF_WINDOWS"/&gt;
 *     &lt;enumeration value="LAF_WINDOWS_CLASSIC"/&gt;
 *     &lt;enumeration value="LAF_MOTIF"/&gt;
 *     &lt;enumeration value="LAF_GTK"/&gt;
 *     &lt;enumeration value="LAF_MACOS"/&gt;
 *     &lt;enumeration value="LAF_NIMBUS"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "LookAndFeelSetting")
@XmlEnum
public enum LookAndFeelSetting {

    LAF_DEFAULT,
    LAF_OS,
    LAF_METAL,
    LAF_WINDOWS,
    LAF_WINDOWS_CLASSIC,
    LAF_MOTIF,
    LAF_GTK,
    LAF_MACOS,
    LAF_NIMBUS;

    public String value() {
        return name();
    }

    public static LookAndFeelSetting fromValue(String v) {
        return valueOf(v);
    }

}
