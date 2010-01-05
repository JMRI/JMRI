package jmri.configurexml;

/**
 * Abstract class to provide basic error handling for XmlAdapter
 *
 * @author Bob Jacobsen  Copyright (c) 2009
 * @version $Revision: 1.3 $
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
                org.apache.log4j.Level level,
                String description, 
                String systemName, 
                String userName, 
                Throwable exception) throws JmriConfigureXmlException
    {
        if (c!=null) {
            c.creationErrorEncountered(
                null, null,
                level, description, systemName, userName, exception
            );
        } else {
            // report locally; deprecated
            String m = description;
            if (systemName!=null) m += "; System name \""+systemName+"\"";
            if (userName!=null) m += "; User name \""+userName+"\"";
            if (exception!=null) m += "; Exception: "+exception.toString();
        
            log.log(level, m);
        }
    }
     
    private ConfigXmlManager c;
    public void setConfigXmlManager(ConfigXmlManager c) { this.c = c; }
    protected ConfigXmlManager getConfigXmlManager() { return c; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractXmlAdapter.class.getName());
}