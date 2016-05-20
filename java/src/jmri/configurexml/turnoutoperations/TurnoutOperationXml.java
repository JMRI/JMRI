/**
 * 
 */
package jmri.configurexml.turnoutoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

import jmri.TurnoutOperation;
import jmri.util.StringUtil;

/**
 * Superclass for save/restore of TurnoutOperation subclasses in XML.
 * @author John Harper	Copyright 2005
 *
 */
public abstract class TurnoutOperationXml extends jmri.configurexml.AbstractXmlAdapter {

	/**
	 * inherited methods
	 * @see jmri.configurexml.XmlAdapter#load(org.jdom.Element)
	 */
	public boolean load(Element e) throws Exception {
		loadOne(e);
		return true;
	}
	
	public abstract TurnoutOperation loadOne(Element e);
	
	/**
	 * Load one operation, using the appropriate adapter
	 * @param e	element for operation
	 */
	public static TurnoutOperation loadOperation(Element e) {
		TurnoutOperation result = null;
		String className = e.getAttributeValue("class");
		if (className==null) {
			log.error("class name missing in turnout operation \""+e+"\"");
		} else {
		    try {
                Class<?> adapterClass = Class.forName(className);
                if (adapterClass != null) {
                    TurnoutOperationXml adapter = (TurnoutOperationXml)adapterClass.newInstance();
                    result = adapter.loadOne(e);
                    if (result.getName().charAt(0)=='*') {
                        result.setNonce(true);
                    }
                }
            } catch (ClassNotFoundException e1) {
                log.error("while creating TurnoutOperation", e1);
                return null;
            } catch (IllegalAccessException e2) {
                log.error("while creating CommonTurnoutOperation", e2);
                return null;
            } catch (InstantiationException e3) {
                log.error("while creating TurnoutOperation", e3);
                return null;
            }
		}
		return result;
	}

	/**
	 * @see jmri.configurexml.XmlAdapter#load(org.jdom.Element, java.lang.Object)
	 */
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    /**
     * common part of store - create the element and store the name and the class
     * @param	o	TurnoutOperation object
     * @return	partially filled element
     */
    public Element store(Object o) {
    	TurnoutOperation myOp = (TurnoutOperation)o;
    	Element elem = new Element("operation");
    	elem.setAttribute("name", myOp.getName());
    	elem.setAttribute("class", this.getClass().getName());
    	return elem;
    }

	/**
	 * Given an instance of a concrete subclass of
	 * the TurnoutOperation class, looks for a corresponding ...Xml
	 * class and creates an instance of it. If anything goes wrong (no such
	 * class, wrong constructors, instantiation error, ....) just return null
	 * @param op	operation for which configurator is required
	 * @return	the configurator
	 */
    static public TurnoutOperationXml getAdapter(TurnoutOperation op) {
    	TurnoutOperationXml adapter = null;
    	String[] fullOpNameComponents = jmri.util.StringUtil.split(op.getClass().getName(),".");
    	String[] myNameComponents =
    		new String[]{"jmri","configurexml","turnoutoperations","TurnoutOperationXml"};
    	myNameComponents[myNameComponents.length-1] = 
    		fullOpNameComponents[fullOpNameComponents.length-1];
    	String fullConfigName = StringUtil.join(myNameComponents, ".") + "Xml";
    	try {
    		Class<?> configClass = Class.forName(fullConfigName);
    		adapter = (TurnoutOperationXml)configClass.newInstance();
    	} catch (Throwable e) {
    	}		// too many to list!
    	if (adapter==null) {
    		log.warn("could not create adapter class "+fullConfigName);
    	}
    	return adapter;
    }
    
    static Logger log = LoggerFactory.getLogger(TurnoutOperationXml.class.getName());
}
