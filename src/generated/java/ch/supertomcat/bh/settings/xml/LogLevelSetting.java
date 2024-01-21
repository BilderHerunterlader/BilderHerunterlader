//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.01.21 at 04:23:47 AM CET 
//


package ch.supertomcat.bh.settings.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LogLevelSetting.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LogLevelSetting"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="TRACE"/&gt;
 *     &lt;enumeration value="DEBUG"/&gt;
 *     &lt;enumeration value="INFO"/&gt;
 *     &lt;enumeration value="WARN"/&gt;
 *     &lt;enumeration value="ERROR"/&gt;
 *     &lt;enumeration value="FATAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "LogLevelSetting")
@XmlEnum
public enum LogLevelSetting {

    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL;

    public String value() {
        return name();
    }

    public static LogLevelSetting fromValue(String v) {
        return valueOf(v);
    }

}
