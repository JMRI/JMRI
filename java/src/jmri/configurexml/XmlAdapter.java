package jmri.configurexml;

import org.jdom2.Element;

/**
 * Interface assumed during configuration operations.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision$
 * @see ConfigXmlManager
 */
public interface XmlAdapter {

    /**
     * Create a set of configured objects from their XML description
     *
     * @param e Top-level XML element containing the description
     * @throws Exception when a error prevents creating the objects as as
     *                   required by the input XML.
     * @return true if successful
     * @deprecated use {@link #load(org.jdom2.Element, org.jdom2.Element)}
     */
    @Deprecated
    public boolean load(Element e) throws Exception;

    /**
     * Create a set of configured objects from their XML description.
     *
     * @param shared  Top-level XML element containing the common, multi-node
     *                elements of the description
     * @param perNode Top-level XML element containing the private, single-node
     *                elements of the description
     * @throws Exception when a error prevents creating the objects as as
     *                   required by the input XML.
     * @return true if successful
     */
    public boolean load(Element shared, Element perNode) throws Exception;

    /**
     * Determine if this set of configured objects should be loaded after basic
     * GUI construction is completed
     *
     * @return true to defer loading
     * @since 2.11.2
     */
    public boolean loadDeferred();

    /**
     * Create a set of configured objects from their XML description, using an
     * auxiliary object.
     * <P>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param e Top-level XML element containing the description
     * @param o Implementation-specific Object needed for the conversion
     * @throws Exception when a error prevents creating the objects as as
     *                   required by the input XML.
     * @deprecated use {@link #load(org.jdom2.Element, org.jdom2.Element, java.lang.Object)
     * }
     */
    @Deprecated
    public void load(Element e, Object o) throws Exception;

    /**
     * Create a set of configured objects from their XML description, using an
     * auxiliary object.
     * <P>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param shared  Top-level XML element containing the common description
     * @param perNode Top-level XML element containing the per-node description
     * @param o       Implementation-specific Object needed for the conversion
     * @throws Exception when a error prevents creating the objects as as
     *                   required by the input XML.
     */
    public void load(Element shared, Element perNode, Object o) throws Exception;

    /**
     * Store the object in XML
     *
     * @param o The object to be recorded. Specific XmlAdapter implementations
     *          will require this to be of a specific type; that binding is done
     *          in ConfigXmlManager.
     * @return The XML representation Element
     * @deprecated use {@link #store(java.lang.Object, boolean) }
     */
    @Deprecated
    public Element store(Object o);

    /**
     * Store the object in XML
     *
     * @param o      The object to be recorded. Specific XmlAdapter
     *               implementations will require this to be of a specific type;
     *               that binding is done in ConfigXmlManager.
     * @param shared true if the returned element should be the common XML and
     *               false if the returned element should be per-node.
     * @return The XML representation Element
     */
    public Element store(Object o, boolean shared);

    public int loadOrder();

    /**
     * Invoke common handling of errors that happen during the "load" process.
     *
     * This is part of the interface to ensure that all the necessary classes
     * provide it; eventually it will be coupled to a reporting mechanism of
     * some sort.
     *
     * @param description description of error encountered
     * @param systemName  System name of bean being handled, may be null
     * @param userName    used name of the bean being handled, may be null
     * @param exception   Any exception being handled in the processing, may be
     *                    null
     * @throws JmriConfigureXmlException in place for later expansion; should be
     *                                   propagated upward to higher-level error
     *                                   handling
     */
    public void creationErrorEncountered(
            String description,
            String systemName,
            String userName,
            Throwable exception) throws JmriConfigureXmlException;
}
