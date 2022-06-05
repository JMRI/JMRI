package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.implementation.DefaultModuleManager;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.ThreadingUtil;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring DefaultModuleManager
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultModuleManagerXml extends AbstractManagerXml {

    private final Map<String, Class<?>> xmlClasses = new HashMap<>();

    public DefaultModuleManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a ModuleManager
     *
     * @param o Object to store, of type LogixManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element expressions = new Element("LogixNGModules");
        setStoreElementClass(expressions);
        DefaultModuleManager tm = (DefaultModuleManager) o;
        if (tm != null) {
            if (tm.getNamedBeanSet().isEmpty()) return null;
            for (Module module : tm.getNamedBeanSet()) {
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(module);
                    if (e != null) {
                        expressions.addContent(e);
                    }
                } catch (RuntimeException e) {
                    log.error("Error storing action: {}", e, e);
                }
            }
        }
        return expressions;
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
     * Create a ModuleManager object of the correct class, then
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
        loadTables(sharedExpression);
        return true;
    }

    /**
     * Utility method to load the individual Logix objects. If there's no
     * additional info needed for a specific logix type, invoke this with the
     * parent of the set of Logix elements.
     *
     * @param expressions Element containing the Logix elements to load.
     */
    public void loadTables(Element expressions) {

        List<Element> expressionList = expressions.getChildren();  // NOI18N
        log.debug("Found {} tables", expressionList.size() );  // NOI18N

        for (int i = 0; i < expressionList.size(); i++) {

            String className = expressionList.get(i).getAttribute("class").getValue();
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
                        o.load(expressionList.get(i), null);
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
     * Replace the current ModuleManager, if there is one, with one newly created
     * during a load operation. This is skipped if they are of the same absolute
     * type.
     */
    protected void replaceExpressionManager() {
        if (InstanceManager.getDefault(ModuleManager.class).getClass().getName()
                .equals(DefaultModuleManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(ModuleManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(ModuleManager.class));
            }

        }

        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultModuleManager pManager = DefaultModuleManager.instance();
            InstanceManager.store(pManager, ModuleManager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_MODULES);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(ModuleManager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleManagerXml.class);
}
