package jmri.configurexml;

import org.jdom.Element;

/**
 * Abstract class to provide basic error handling for XmlAdapter
 *
 * @author Bob Jacobsen  Copyright (c) 2009
 * @version $Revision: 1.1 $
 * @see XmlAdapter
 */

public abstract class AbstractXmlAdapter implements XmlAdapter {
    
    /**
     * Provide common error handling.
     * 
     * Simple implementation just sends message to 
     * standard logging
     *
     * @param text description of error encountered
     * @param systemName System name of bean being handled, may be null
     * @param userName used name of the bean being handled, may be null
     * @param Exception Any exception being handled in the processing, may be null
     * @throws JmriConfigureXmlException in place for later expansion;
     *         should be propagated upward to higher-level error handling
     */
    public void creationErrorEncountered (
                String description, 
                String systemName, 
                String userName, 
                Exception exception) throws JmriConfigureXmlException
    {
        String m = description;
        if (systemName!=null) m += "; System name \""+systemName+"\"";
        if (userName!=null) m += "; User name \""+userName+"\"";
        if (exception!=null) m += "; Exception: "+exception.toString();
        
        log.warn(m);
    }
                
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractXmlAdapter.class.getName());
}