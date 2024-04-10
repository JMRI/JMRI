package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultDigitalActionManager;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalActionSocket;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.ThreadingUtil;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring ActionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultDigitalActionManagerXml extends AbstractManagerXml {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();

    public DefaultDigitalActionManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a DigitalActionManager
     *
     * @param o Object to store, of type DigitalActionManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element actions = new Element("LogixNGDigitalActions");
        setStoreElementClass(actions);
        DigitalActionManager tm = (DigitalActionManager) o;
        if (tm != null) {
            if (tm.getNamedBeanSet().isEmpty()) return null;
            for (MaleDigitalActionSocket action : tm.getNamedBeanSet()) {
                log.debug("action system name is {}", action.getSystemName() );  // NOI18N
                try {
                    List<Element> elements = new ArrayList<>();
                    // The male socket may be embedded in other male sockets
                    MaleDigitalActionSocket a = action;
                    while (!(a instanceof DefaultMaleDigitalActionSocket)) {
                        elements.add(storeMaleSocket(a));
                        a = (MaleDigitalActionSocket) a.getObject();
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
     * Create a DigitalActionManager object of the correct class, then register
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
     * Utility method to load the individual DigitalActionBean objects. If
     * there's no additional info needed for a specific action type, invoke
     * this with the parent of the set of DigitalActionBean elements.
     *
     * @param actions Element containing the DigitalActionBean elements to load.
     */
    public void loadActions(Element actions) {

        List<Element> actionList = actions.getChildren();  // NOI18N
        log.debug("Found {} actions", actionList.size() );  // NOI18N

        for (int i = 0; i < actionList.size(); i++) {

            String className = actionList.get(i).getAttribute("class").getValue();
//            log.error("className: " + className);

            Class<?> clazz = xmlClasses.get(className);

            if (clazz == null) {
                try {
                    className = jmri.configurexml.ConfigXmlManager.currentClassName(className);
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

                        MaleSocket oldLastItem = InstanceManager.getDefault(DigitalActionManager.class).getLastRegisteredMaleSocket();
                        o.load(actionList.get(i), null);

                        // Load male socket data if a new bean has been registered
                        MaleSocket newLastItem = InstanceManager.getDefault(DigitalActionManager.class).getLastRegisteredMaleSocket();
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
     * Replace the current DigitalActionManager, if there is one, with one newly
     * created during a load operation. This is skipped if they are of the same
     * absolute type.
     */
    protected void replaceActionManager() {
        if (InstanceManager.getDefault(jmri.jmrit.logixng.DigitalActionManager.class).getClass().getName()
                .equals(DefaultDigitalActionManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalActionManager.class));
            }

        }

        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultDigitalActionManager pManager = DefaultDigitalActionManager.instance();
            InstanceManager.store(pManager, DigitalActionManager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_ACTIONS);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.logixng.DigitalActionManager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultDigitalActionManagerXml.class);
}
