package jmri.jmrit.logixng.digital.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.implementation.DefaultDigitalActionManager;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.util.ThreadingUtil;
import jmri.util.ThreadingUtil.ThreadAction;

/**
 * Provides the functionality for configuring ActionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultDigitalActionManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();
    
    public DefaultDigitalActionManagerXml() {
    }

    private DigitalActionBean getAction(DigitalActionBean action) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        Field f = action.getClass().getDeclaredField("_action");
        f.setAccessible(true);
        return (DigitalActionBean) f.get(action);
    }
    
    /**
     * Default implementation for storing the contents of a DigitalActionManager
     *
     * @param o Object to store, of type DigitalActionManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element actions = new Element("logixngDigitalActions");
        setStoreElementClass(actions);
        DigitalActionManager tm = (DigitalActionManager) o;
        if (tm != null) {
            for (DigitalActionBean action : tm.getNamedBeanSet()) {
                log.debug("action system name is " + action.getSystemName());  // NOI18N
//                log.error("action system name is " + action.getSystemName() + ", " + action.getLongDescription());  // NOI18N
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(getAction(action));
                    if (e != null) {
                        actions.addContent(e);
                    }
                } catch (Exception e) {
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
        log.debug("Found " + actionList.size() + " actions");  // NOI18N

        for (int i = 0; i < actionList.size(); i++) {
            
            String className = actionList.get(i).getAttribute("class").getValue();
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
                        AbstractNamedBeanManagerConfigXML o = (AbstractNamedBeanManagerConfigXML)c.newInstance();
                        
                        o.load(actionList.get(i), null);
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

    private final static Logger log = LoggerFactory.getLogger(DefaultDigitalActionManagerXml.class);
}
