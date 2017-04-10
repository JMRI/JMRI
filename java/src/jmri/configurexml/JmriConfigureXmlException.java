package jmri.configurexml;

/**
 * Base for JMRI-specific ConfigureXml exceptions. No functionality, just used
 * to confirm type-safety.
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010
 */
public class JmriConfigureXmlException extends jmri.JmriException {

    public JmriConfigureXmlException(String s, Throwable t) {
        super(s, t);
    }

    public JmriConfigureXmlException(String s) {
        super(s);
    }

    public JmriConfigureXmlException(Exception s) {
        super(s);
    }

    public JmriConfigureXmlException() {
        super();
    }
}

