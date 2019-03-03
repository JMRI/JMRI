package jmri.configurexml.turnoutoperations;

import jmri.TurnoutOperation;
import jmri.util.StringUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for save/restore of TurnoutOperation subclasses in XML.
 *
 * @author John Harper Copyright 2005
 *
 */
public abstract class TurnoutOperationXml extends jmri.configurexml.AbstractXmlAdapter {

    @Override
    public boolean load(Element shared, Element perNode) {
        loadOne(shared);
        return true;
    }

    public abstract TurnoutOperation loadOne(Element e);

    /**
     * Load one operation, using the appropriate adapter
     *
     * @param e element for operation
     * @return the loaded TurnoutOperation or null if unable to load from e
     */
    public static TurnoutOperation loadOperation(Element e) {
        TurnoutOperation result = null;
        String className = e.getAttributeValue("class");
        if (className == null) {
            log.error("class name missing in turnout operation \"" + e + "\"");
        } else {
            log.debug("loadOperation for class {}", className);
            try {
                Class<?> adapterClass = Class.forName(className);
                TurnoutOperationXml adapter = (TurnoutOperationXml) adapterClass.getDeclaredConstructor().newInstance();
                result = adapter.loadOne(e);
                if (result.getName().charAt(0) == '*') {
                    result.setNonce(true);
                }
            } catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e1) {
                log.error("while creating TurnoutOperation", e1);
                return null;
            } catch (IllegalAccessException e2) {
                log.error("while creating CommonTurnoutOperation", e2);
                return null;
            }
        }
        return result;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * common part of store - create the element and store the name and the
     * class
     *
     * @param o TurnoutOperation object
     * @return partially filled element
     */
    @Override
    public Element store(Object o) {
        TurnoutOperation myOp = (TurnoutOperation) o;
        Element elem = new Element("operation");
        elem.setAttribute("name", myOp.getName());
        elem.setAttribute("class", this.getClass().getName());
        return elem;
    }

    /**
     * Given an instance of a concrete subclass of the TurnoutOperation class,
     * looks for a corresponding ...Xml class and creates an instance of it. If
     * anything goes wrong (no such class, wrong constructors, instantiation
     * error, ....) just return null
     *
     * @param op operation for which configurator is required
     * @return the configurator
     */
    static public TurnoutOperationXml getAdapter(TurnoutOperation op) {
        TurnoutOperationXml adapter = null;
        String[] fullOpNameComponents = op.getClass().getName().split("\\.");
        log.debug("getAdapter found class name {}", op.getClass().getName());
        String[] myNameComponents
                = new String[]{"jmri", "configurexml", "turnoutoperations", "TurnoutOperationXml"};
        myNameComponents[myNameComponents.length - 1]
                = fullOpNameComponents[fullOpNameComponents.length - 1];
        String fullConfigName = String.join(".", myNameComponents) + "Xml";
        log.debug("getAdapter looks for {}", fullConfigName);
        try {
            Class<?> configClass = Class.forName(fullConfigName);
            adapter = (TurnoutOperationXml) configClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            log.error("exception in getAdapter", e);
        }
        if (adapter == null) {
            log.warn("could not create adapter class " + fullConfigName);
        }
        return adapter;
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperationXml.class);
}
