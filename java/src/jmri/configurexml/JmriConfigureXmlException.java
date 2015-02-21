// JmriConfigureXmlException.java
package jmri.configurexml;

/**
 * Base for JMRI-specific ConfigureXml exceptions. No functionality, just used
 * to confirm type-safety.
 *
 * @author	Bob Jacobsen Copyright (C) 2009, 2010
 * @version	$Revision$
 */
public class JmriConfigureXmlException extends jmri.JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -1321961285382362044L;

    public JmriConfigureXmlException(String s, Throwable t) {
        super(s, t);
    }

    public JmriConfigureXmlException(String s) {
        super(s);
    }

    public JmriConfigureXmlException() {
        super();
    }
}

/* @(#)JmriConfigureXmlException.java */
