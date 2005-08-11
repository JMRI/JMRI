package jmri.configurexml;

import org.jdom.Element;

/**
 * Interface assumed during configuration operations.
 *
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.4 $
 * @see ConfigXmlManager
 */

public interface XmlAdapter {
    /**
     * Create a set of configured objects from their
     * XML description
     * @param e Top-level XML element containing the description
     * @throws Exception when a error prevents creating the objects as
     *          as required by the input XML.  
     */
    public void load(Element e) throws Exception;

    /**
     * Create a set of configured objects from their
     * XML description, using an auxiliary object.
     * <P>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param e Top-level XML element containing the description
     * @param o Implementation-specific Object needed for the conversion
     * @throws Exception when a error prevents creating the objects as
     *          as required by the input XML.  
     */
    public void load(Element e, Object o) throws Exception;

    /**
     * Store the
     * @param o The object to be recorded.  Specific XmlAdapter
     *          implementations will require this to be of a specific
     *          type; that binding is done in ConfigXmlManager.
     * @return The XML representation Element
     */
    public Element store(Object o);
}