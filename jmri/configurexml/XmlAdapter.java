package jmri.configurexml;

import org.jdom.Element;

/**
 * Interface assumed during configuration operations.
 *
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.3 $
 * @see ConfigXmlManager
 */

public interface XmlAdapter {
    /**
     * Create a set of configured objects from their
     * XML description
     * @param e Top-level XML element containing the description
     */
    public void load(Element e);

    /**
     * Create a set of configured objects from their
     * XML description, using an auxiliary object.
     * <P>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param e Top-level XML element containing the description
     * @param o Implementation-specific Object needed for the conversion
     */
    public void load(Element e, Object o);

    /**
     * Store the
     * @param o The object to be recorded.  Specific XmlAdapter
     *          implementations will require this to be of a specific
     *          type; that binding is done in ConfigXmlManager.
     * @return The XML representation Element
     */
    public Element store(Object o);
}