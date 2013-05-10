//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.09.11 at 03:55:46 PM EDT 
//


package com.hpccsystems.salt.jaxb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.hpccsystems.salt.jaxb package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.hpccsystems.salt.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SaltSpecification }
     * 
     */
    public SaltSpecification createSaltSpecification() {
        return new SaltSpecification();
    }

    /**
     * Create an instance of {@link SaltSpecification.HygieneRuleMap.Map }
     * 
     */
    public SaltSpecification.HygieneRuleMap.Map createSaltSpecificationHygieneRuleMapMap() {
        return new SaltSpecification.HygieneRuleMap.Map();
    }

    /**
     * Create an instance of {@link SaltSpecification.MatchingRuleMap }
     * 
     */
    public SaltSpecification.MatchingRuleMap createSaltSpecificationMatchingRuleMap() {
        return new SaltSpecification.MatchingRuleMap();
    }

    /**
     * Create an instance of {@link HygieneRule }
     * 
     */
    public HygieneRule createHygieneRule() {
        return new HygieneRule();
    }

    /**
     * Create an instance of {@link SaltSpecification.MatchingRuleMap.Map }
     * 
     */
    public SaltSpecification.MatchingRuleMap.Map createSaltSpecificationMatchingRuleMapMap() {
        return new SaltSpecification.MatchingRuleMap.Map();
    }

    /**
     * Create an instance of {@link FieldDef }
     * 
     */
    public FieldDef createFieldDef() {
        return new FieldDef();
    }

    /**
     * Create an instance of {@link MatchingRule }
     * 
     */
    public MatchingRule createMatchingRule() {
        return new MatchingRule();
    }

    /**
     * Create an instance of {@link MatchingRule.Fuzzy }
     * 
     */
    public MatchingRule.Fuzzy createMatchingRuleFuzzy() {
        return new MatchingRule.Fuzzy();
    }

    /**
     * Create an instance of {@link MatchingRule.Context }
     * 
     */
    public MatchingRule.Context createMatchingRuleContext() {
        return new MatchingRule.Context();
    }

    /**
     * Create an instance of {@link SaltSpecification.HygieneRules }
     * 
     */
    public SaltSpecification.HygieneRules createSaltSpecificationHygieneRules() {
        return new SaltSpecification.HygieneRules();
    }

    /**
     * Create an instance of {@link SaltSpecification.HygieneRuleMap }
     * 
     */
    public SaltSpecification.HygieneRuleMap createSaltSpecificationHygieneRuleMap() {
        return new SaltSpecification.HygieneRuleMap();
    }

    /**
     * Create an instance of {@link SaltSpecification.MatchingRules }
     * 
     */
    public SaltSpecification.MatchingRules createSaltSpecificationMatchingRules() {
        return new SaltSpecification.MatchingRules();
    }

    /**
     * Create an instance of {@link SaltSpecification.Fields }
     * 
     */
    public SaltSpecification.Fields createSaltSpecificationFields() {
        return new SaltSpecification.Fields();
    }

    
    public ConceptDef createConceptDef(){
    	return new ConceptDef();
    }
    public ConceptDef.ConceptFields createConceptDefFields(){
    	return new ConceptDef.ConceptFields();
    }
    
}