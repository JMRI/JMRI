package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultMaleStringExpressionSocket;
import jmri.jmrit.logixng.implementation.DefaultStringExpressionManager;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.ThreadingUtil;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring ExpressionManagers
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultStringExpressionManagerXml extends AbstractManagerXml {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();
    
    public DefaultStringExpressionManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a LogixManager
     *
     * @param o Object to store, of type LogixManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element expressions = new Element("LogixNGStringExpressions");
        setStoreElementClass(expressions);
        StringExpressionManager tm = (StringExpressionManager) o;
        if (tm != null) {
            if (tm.getNamedBeanSet().isEmpty()) return null;
            for (MaleStringExpressionSocket expression : tm.getNamedBeanSet()) {
                log.debug("expression system name is " + expression.getSystemName());  // NOI18N
//                log.error("expression system name is " + expression.getSystemName() + ", " + expression.getLongDescription());  // NOI18N
                try {
                    List<Element> elements = new ArrayList<>();
                    // The male socket may be embedded in other male sockets
                    MaleStringExpressionSocket a = expression;
                    while (!(a instanceof DefaultMaleStringExpressionSocket)) {
                        elements.add(storeMaleSocket(a));
                        a = (MaleStringExpressionSocket) a.getObject();
                    }
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(a.getObject());
                    if (e != null) {
                        for (Element ee : elements) e.addContent(ee);
//                        e.addContent(storeMaleSocket(expression));
                        expressions.addContent(e);
                    } else {
                        throw new RuntimeException("Cannot load xml configurator for " + a.getObject().getClass().getName());
                    }
                } catch (RuntimeException e) {
                    log.error("Error storing action: {}", e, e);
                }
            }
        }
        return (expressions);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param expressions The top-level element being created
     */
    public void setStoreElementClass(Element expressions) {
        expressions.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    /**
     * Create a StringExpressionManager object of the correct class, then
     * register and fill it.
     *
     * @param sharedExpression  Shared top level Element to unpack.
     * @param perNodeExpression Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedExpression, Element perNodeExpression) {
        // create the master object
        replaceExpressionManager();
        // load individual sharedLogix
        loadExpressions(sharedExpression);
        return true;
    }

    /**
     * Utility method to load the individual Logix objects. If there's no
     * additional info needed for a specific logix type, invoke this with the
     * parent of the set of Logix elements.
     *
     * @param expressions Element containing the Logix elements to load.
     */
    public void loadExpressions(Element expressions) {
        
        List<Element> expressionList = expressions.getChildren();  // NOI18N
        log.debug("Found " + expressionList.size() + " actions");  // NOI18N

        for (int i = 0; i < expressionList.size(); i++) {
            
            String className = expressionList.get(i).getAttribute("class").getValue();
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
                        
                        MaleSocket oldLastItem = InstanceManager.getDefault(StringExpressionManager.class).getLastRegisteredMaleSocket();
                        o.load(expressionList.get(i), null);
                        
                        // Load male socket data if a new bean has been registered
                        MaleSocket newLastItem = InstanceManager.getDefault(StringExpressionManager.class).getLastRegisteredMaleSocket();
                        if (newLastItem != oldLastItem) loadMaleSocket(expressionList.get(i), newLastItem);
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
     * Replace the current LogixManager, if there is one, with one newly created
     * during a load operation. This is skipped if they are of the same absolute
     * type.
     */
    protected void replaceExpressionManager() {
        if (InstanceManager.getDefault(StringExpressionManager.class).getClass().getName()
                .equals(DefaultStringExpressionManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(StringExpressionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(StringExpressionManager.class));
            }

        }


        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultStringExpressionManager pManager = DefaultStringExpressionManager.instance();
            InstanceManager.store(pManager, StringExpressionManager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_STRING_EXPRESSIONS);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(StringExpressionManager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultStringExpressionManagerXml.class);
}
