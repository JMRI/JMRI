package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultDigitalBooleanActionManager;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalBooleanActionSocket;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.ThreadingUtil;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring ActionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultDigitalBooleanActionManagerXml extends AbstractManagerXml {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();
    
    public DefaultDigitalBooleanActionManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a DigitalBooleanActionManager
     *
     * @param o Object to store, of type DigitalBooleanActionManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element actions = new Element("LogixNGDigitalBooleanActions");
        setStoreElementClass(actions);
        DigitalBooleanActionManager tm = (DigitalBooleanActionManager) o;
        if (tm != null) {
            if (tm.getNamedBeanSet().isEmpty()) return null;
            for (MaleDigitalBooleanActionSocket action : tm.getNamedBeanSet()) {
                log.debug("action system name is {}", action.getSystemName());  // NOI18N
                try {
                    List<Element> elements = new ArrayList<>();
                    // The male socket may be embedded in other male sockets
                    MaleDigitalBooleanActionSocket a = action;
                    while (!(a instanceof DefaultMaleDigitalBooleanActionSocket)) {
                        elements.add(storeMaleSocket(a));
                        a = (MaleDigitalBooleanActionSocket) a.getObject();
                    }
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(a.getObject());
                    if (e != null) {
                        for (Element ee : elements) e.addContent(ee);
                        actions.addContent(e);
                    } else {
                        throw new RuntimeException("Cannot load xml configurator for " + a.getObject().getClass().getName());
                    }
                } catch (RuntimeException e) {
                    log.error("Error storing action: {}", e, e);
                }
            }
        }
        return (actions);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param actions The top-level element being created
     */
    public void setStoreElementClass(Element actions) {
        actions.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    /**
     * Create a DigitalBooleanActionManager object of the correct class, then register
     * and fill it.
     *
     * @param sharedAction  Shared top level Element to unpack.
     * @param perNodeAction Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedAction, Element perNodeAction) {
        // create the master object
        replaceActionManager();
        // load individual sharedAction
        loadActions(sharedAction);
        return true;
    }

    /**
     * Utility method to load the individual DigitalBooleanActionBean objects. If
     * there's no additional info needed for a specific action type, invoke
     * this with the parent of the set of DigitalBooleanActionBean elements.
     *
     * @param actions Element containing the DigitalBooleanActionBean elements to load.
     */
    public void loadActions(Element actions) {
        
        List<Element> actionList = actions.getChildren();  // NOI18N
        log.debug("Found {} actions", actionList.size());  // NOI18N

        for (int i = 0; i < actionList.size(); i++) {
            
            String className = actionList.get(i).getAttribute("class").getValue();
//            log.error("className: " + className);
            
            Class<?> clazz = xmlClasses.get(className);
            
            if (clazz == null) {
                try {
                    clazz = Class.forName(className);
                    xmlClasses.put(className, clazz);
                } catch (ClassNotFoundException ex) {
                    log.error("cannot load class {}", className, ex);
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
                        AbstractNamedBeanManagerConfigXML o = (AbstractNamedBeanManagerConfigXML)c.newInstance();
                        
                        MaleSocket oldLastItem = InstanceManager.getDefault(DigitalBooleanActionManager.class).getLastRegisteredMaleSocket();
                        o.load(actionList.get(i), null);
                        
                        // Load male socket data if a new bean has been registered
                        MaleSocket newLastItem = InstanceManager.getDefault(DigitalBooleanActionManager.class).getLastRegisteredMaleSocket();
                        if (newLastItem != oldLastItem) loadMaleSocket(actionList.get(i), newLastItem);
                        else throw new RuntimeException("No new bean has been added. This class: "+getClass().getName());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        log.error("cannot create object", ex);
                    } catch (JmriConfigureXmlException ex) {
                        log.error("cannot load action", ex);
                    }
                }
            }
        }
    }

    /**
     * Replace the current DigitalBooleanActionManager, if there is one, with one newly
     * created during a load operation. This is skipped if they are of the same
     * absolute type.
     */
    protected void replaceActionManager() {
        if (InstanceManager.getDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class).getClass().getName()
                .equals(DefaultDigitalBooleanActionManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class));
            }

        }

        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultDigitalBooleanActionManager pManager = DefaultDigitalBooleanActionManager.instance();
            InstanceManager.store(pManager, DigitalBooleanActionManager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_BOOLEAN_ACTIONS);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultDigitalBooleanActionManagerXml.class);
}
