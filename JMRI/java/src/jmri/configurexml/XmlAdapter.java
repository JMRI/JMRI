package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.configurexml.swing.DialogErrorHandler;
import org.jdom2.Element;

/**
 * Interface assumed during configuration operations.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @see ConfigXmlManager
 */
public interface XmlAdapter {

    /**
     * Create a set of configured objects from their XML description
     *
     * @param e Top-level XML element containing the description
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML.
     * @return true if successful
     */
    public boolean load(Element e) throws JmriConfigureXmlException;

    /**
     * Create a set of configured objects from their XML description.
     *
     * @param shared  Top-level XML element containing the common, multi-node
     *                elements of the description
     * @param perNode Top-level XML element containing the private, single-node
     *                elements of the description
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     * @return true if successful
     */
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException;

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
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    public void load(Element e, Object o) throws JmriConfigureXmlException;

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
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    public void load(Element shared, Element perNode, Object o) throws JmriConfigureXmlException;

    /**
     * Store the object in XML
     *
     * @param o The object to be recorded. Specific XmlAdapter implementations
     *          will require this to be of a specific type; that binding is done
     *          in ConfigXmlManager.
     * @return The XML representation Element
     */
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
     * @deprecated since 4.7.2; use
     * {@link #handleException(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Exception)}
     * instead
     */
    @Deprecated
    public void creationErrorEncountered(
            String description,
            String systemName,
            String userName,
            Exception exception) throws JmriConfigureXmlException;

    /**
     * Provide a simple handler for errors.
     *
     * Calls the configured {@link jmri.configurexml.ErrorHandler} with an
     * {@link jmri.configurexml.ErrorMemo} created using the provided
     * parameters.
     *
     * @param description description of error encountered
     * @param operation   the operation being performed, may be null
     * @param systemName  system name of bean being handled, may be null
     * @param userName    user name of the bean being handled, may be null
     * @param exception   Any exception being handled in the processing, may be
     *                    null
     * @throws JmriConfigureXmlException in place for later expansion; should be
     *                                   propagated upward to higher-level error
     *                                   handling
     */
    public void handleException(
            @Nonnull String description,
            @Nullable String operation,
            @Nullable String systemName,
            @Nullable String userName,
            @Nullable Exception exception) throws JmriConfigureXmlException;

    /**
     * Set the error handler that will handle any errors encountered while
     * parsing the XML. If not specified, the default error handler will be
     * used.
     *
     * @param errorHandler the error handler or null to ignore parser errors
     */
    public void setExceptionHandler(ErrorHandler errorHandler);

    /**
     * Get the current error handler.
     *
     * @return the error handler or null if no error handler is assigned
     */
    public ErrorHandler getExceptionHandler();

    /**
     * Get the default error handler.
     *
     * @return the default error handler
     */
    public static ErrorHandler getDefaultExceptionHandler() {
        if (GraphicsEnvironment.isHeadless()) {
            return new ErrorHandler();
        }
        return new DialogErrorHandler();
    }
}
