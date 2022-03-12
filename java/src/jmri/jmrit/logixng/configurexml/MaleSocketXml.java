package jmri.jmrit.logixng.configurexml;

import jmri.jmrit.logixng.*;

import org.jdom2.Element;

/**
 * Handle XML configuration for MaleSocketXML objects.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface MaleSocketXml {

    public boolean load(Element maleSocketElement, MaleSocket maleSocket);
    
}
