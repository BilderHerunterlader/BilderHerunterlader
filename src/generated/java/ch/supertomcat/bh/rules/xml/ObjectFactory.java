//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.07.02 um 01:20:54 AM CEST 
//


package ch.supertomcat.bh.rules.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.supertomcat.bh.rules.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Rule_QNAME = new QName("", "rule");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.supertomcat.bh.rules.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RuleDefinition }
     * 
     */
    public RuleDefinition createRuleDefinition() {
        return new RuleDefinition();
    }

    /**
     * Create an instance of {@link FilenameDownloadSelectionPipeline }
     * 
     */
    public FilenameDownloadSelectionPipeline createFilenameDownloadSelectionPipeline() {
        return new FilenameDownloadSelectionPipeline();
    }

    /**
     * Create an instance of {@link URLRegexPipeline }
     * 
     */
    public URLRegexPipeline createURLRegexPipeline() {
        return new URLRegexPipeline();
    }

    /**
     * Create an instance of {@link URLPipeline }
     * 
     */
    public URLPipeline createURLPipeline() {
        return new URLPipeline();
    }

    /**
     * Create an instance of {@link RuleRegex }
     * 
     */
    public RuleRegex createRuleRegex() {
        return new RuleRegex();
    }

    /**
     * Create an instance of {@link Pipeline }
     * 
     */
    public Pipeline createPipeline() {
        return new Pipeline();
    }

    /**
     * Create an instance of {@link Restriction }
     * 
     */
    public Restriction createRestriction() {
        return new Restriction();
    }

    /**
     * Create an instance of {@link URLJavascriptPipeline }
     * 
     */
    public URLJavascriptPipeline createURLJavascriptPipeline() {
        return new URLJavascriptPipeline();
    }

    /**
     * Create an instance of {@link FailuresPipeline }
     * 
     */
    public FailuresPipeline createFailuresPipeline() {
        return new FailuresPipeline();
    }

    /**
     * Create an instance of {@link FilenamePipeline }
     * 
     */
    public FilenamePipeline createFilenamePipeline() {
        return new FilenamePipeline();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RuleDefinition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "rule")
    public JAXBElement<RuleDefinition> createRule(RuleDefinition value) {
        return new JAXBElement<RuleDefinition>(_Rule_QNAME, RuleDefinition.class, null, value);
    }

}
