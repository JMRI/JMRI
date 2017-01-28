package jmri.configurexml;

import org.jdom2.Element;

/**
 * Testing dummy class used in jmri.configurexml.ConfigXmlManagerTest
 *
 * Needs to be in the jmri package to drive the auto-class resolution methods
 *
 * @author Bob Jacobsen Copyright 2017
 */
 
class ConfigXmlHandleXml implements XmlAdapter {
    public boolean load(Element e) throws Exception { return true; }
    public boolean load(Element shared, Element perNode) throws Exception { return true; }
    public boolean loadDeferred() { return false; }
    public void load(Element e, Object o) throws Exception { return; }
    public void load(Element shared, Element perNode, Object o) throws Exception { return; }
    public Element store(Object o) { return null; }
    public Element store(Object o, boolean shared) { 
        throw new IllegalArgumentException("for testing");
    }
    public int loadOrder() { return 3; }
    
    
    public void creationErrorEncountered(
            String description,
            String systemName,
            String userName,
            Throwable exception) throws JmriConfigureXmlException {
    }
 }
 
