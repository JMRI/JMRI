package jmri.configurexml;

import org.jdom.Element;

/**
 * Interface assumed during configuration operations.
 *
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.1 $
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
     * Store the
     * @param o The object to be recorded.  Specific XmlAdapter
     *          implementations will require this to be of a specific
     *          type; that binding is done in ConfigXmlManager.
     * @return The XML representation Element
     */
    public Element store(Object o);
}