package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.configurexml.MaleSocketXml;

import org.jdom2.Element;


/**
 * Provides the functionality for configuring ActionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public abstract class AbstractManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();
    
    
    /**
     * Store data for a MaleSocket
     *
     * @param maleSocket the socket to store
     * @return Element containing the complete info
     */
    public Element storeMaleSocket(MaleSocket maleSocket) {
        Element element = new Element("MaleSocket");
        
        Base m = maleSocket;
        while (m instanceof MaleSocket) {
            MaleSocket ms = (MaleSocket) m;
            
            try {
                Element e = ConfigXmlManager.elementFromObject(ms);
                if (e != null) {
                    element.addContent(e);
                } else {
                    throw new RuntimeException("Cannot load xml configurator for " + ms.getClass().getName());
                }
            } catch (RuntimeException e) {
                log.error("Error storing maleSocket: {}", e, e);
            }
            
            m = ms.getObject();
        }
        
        return (element);
    }

    /**
     * Utility method to load the individual DigitalActionBean objects. If
     * there's no additional info needed for a specific action type, invoke
     * this with the parent of the set of DigitalActionBean elements.
     *
     * @param element Element containing the MaleSocket element to load.
     * @param maleSocket the socket to load
     */
    public void loadMaleSocket(Element element, MaleSocket maleSocket) {
        
        Map<String, Map.Entry<MaleSocketXml, Element>> maleSocketXmlClasses = new HashMap<>();
        
        Element elementMaleSocket = element.getChild("MaleSocket");
        if (elementMaleSocket == null) {
            throw new IllegalArgumentException("maleSocket is null");
        }
        
        List<Element> children = elementMaleSocket.getChildren();
        log.debug("Found " + children.size() + " male sockets");  // NOI18N
        
        for (Element e : children) {
            
            String className = e.getAttribute("class").getValue();
//            log.error("className: " + className);
            
            Class<?> clazz = xmlClasses.get(className);
            
            if (clazz == null) {
                try {
                    clazz = Class.forName(className);
                    xmlClasses.put(className, clazz);
                } catch (ClassNotFoundException ex) {
                    log.error("cannot load class " + className, ex);
                }
            }
            
            if (clazz != null) {
                Constructor<?> c = null;
                try {
                    c = clazz.getConstructor();
                } catch (NoSuchMethodException | SecurityException ex) {
                    log.error("cannot create constructor", ex);
                }
                
                if (c != null) {
                    try {
                        MaleSocketXml o = (MaleSocketXml)c.newInstance();
                        
                        Map.Entry<MaleSocketXml, Element> entry =
                                new HashMap.SimpleEntry<>(o, e);
                        maleSocketXmlClasses.put(className, entry);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        log.error("cannot create object", ex);
                    }
                }
            }
        }
        
        Base m = maleSocket;
        while (m instanceof MaleSocket) {
            MaleSocket ms = (MaleSocket) m;
            
            String cName = ConfigXmlManager.adapterName(ms);
            Map.Entry<MaleSocketXml, Element> entry = maleSocketXmlClasses.get(cName);
            
            try {
                entry.getKey().load(entry.getValue(), ms);
            } catch (RuntimeException ex) {
                log.error("Error storing maleSocket: {}", ex, ex);
            }

            m = ms.getObject();
        }
        
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractManagerXml.class);
}
