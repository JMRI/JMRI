package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to provide basic error handling for XmlAdapter
 *
 * @author Bob Jacobsen  Copyright (c) 2009
 * @version $Revision$
 * @see XmlAdapter
 */

public abstract class AbstractXmlAdapter implements XmlAdapter {
    
    /**
     * Provide common handling of errors that
     * happen during the "load" process.
     * 
     * Simple implementation just sends message to 
     * standard logging; needs to be given a plug-in
     * structure for e.g. posting a Swing dialog, etc.
     *
     * @param description description of error encountered
     * @param systemName System name of bean being handled, may be null
     * @param userName used name of the bean being handled, may be null
     * @param exception Any exception being handled in the processing, may be null
     * @throws JmriConfigureXmlException in place for later expansion;
     *         should be propagated upward to higher-level error handling
     */
    public void creationErrorEncountered (
                String description, 
                String systemName, 
                String userName, 
                Throwable exception) throws JmriConfigureXmlException
    {
        ConfigXmlManager.creationErrorEncountered(
                null, null,
                description, systemName, userName, exception
        );
    }

    /**
     * Determine if this set of configured objects should
     * be loaded after basic GUI construction is completed.
     * <p>
     * Default behaviour is to load when requested.
     * Classes that should wait until basic GUI is constructed
     * should override this method and return true
     * @return true to defer loading
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    public boolean loadDeferred() {
        return false;
    }
    
    /**
    * Used for determining which order to load items from XML files in.
    */
    public int loadOrder(){
        return 50;
    }
     
    private ConfigXmlManager c;
    public void setConfigXmlManager(ConfigXmlManager c) { this.c = c; }
    protected ConfigXmlManager getConfigXmlManager() { return c; }

    static Logger log = LoggerFactory.getLogger(AbstractXmlAdapter.class.getName());
}
