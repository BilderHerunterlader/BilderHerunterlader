//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package ch.supertomcat.bh.rules.xml;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.supertomcat.bh.rules.xml package. 
 * <p>An ObjectFactory allows you to programmatically 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.supertomcat.bh.rules.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RuleDefinition }
     * 
     * @return
     *     the new instance of {@link RuleDefinition }
     */
    public RuleDefinition createRuleDefinition() {
        return new RuleDefinition();
    }

    /**
     * Create an instance of {@link URLPipeline }
     * 
     * @return
     *     the new instance of {@link URLPipeline }
     */
    public URLPipeline createURLPipeline() {
        return new URLPipeline();
    }

    /**
     * Create an instance of {@link FailuresPipeline }
     * 
     * @return
     *     the new instance of {@link FailuresPipeline }
     */
    public FailuresPipeline createFailuresPipeline() {
        return new FailuresPipeline();
    }

    /**
     * Create an instance of {@link FilenamePipeline }
     * 
     * @return
     *     the new instance of {@link FilenamePipeline }
     */
    public FilenamePipeline createFilenamePipeline() {
        return new FilenamePipeline();
    }

    /**
     * Create an instance of {@link FilenameDownloadSelectionPipeline }
     * 
     * @return
     *     the new instance of {@link FilenameDownloadSelectionPipeline }
     */
    public FilenameDownloadSelectionPipeline createFilenameDownloadSelectionPipeline() {
        return new FilenameDownloadSelectionPipeline();
    }

    /**
     * Create an instance of {@link Restriction }
     * 
     * @return
     *     the new instance of {@link Restriction }
     */
    public Restriction createRestriction() {
        return new Restriction();
    }

    /**
     * Create an instance of {@link Pipeline }
     * 
     * @return
     *     the new instance of {@link Pipeline }
     */
    public Pipeline createPipeline() {
        return new Pipeline();
    }

    /**
     * Create an instance of {@link RuleRegex }
     * 
     * @return
     *     the new instance of {@link RuleRegex }
     */
    public RuleRegex createRuleRegex() {
        return new RuleRegex();
    }

    /**
     * Create an instance of {@link URLRegexPipeline }
     * 
     * @return
     *     the new instance of {@link URLRegexPipeline }
     */
    public URLRegexPipeline createURLRegexPipeline() {
        return new URLRegexPipeline();
    }

    /**
     * Create an instance of {@link VarRuleRegex }
     * 
     * @return
     *     the new instance of {@link VarRuleRegex }
     */
    public VarRuleRegex createVarRuleRegex() {
        return new VarRuleRegex();
    }

    /**
     * Create an instance of {@link URLJavascriptPipeline }
     * 
     * @return
     *     the new instance of {@link URLJavascriptPipeline }
     */
    public URLJavascriptPipeline createURLJavascriptPipeline() {
        return new URLJavascriptPipeline();
    }

}
