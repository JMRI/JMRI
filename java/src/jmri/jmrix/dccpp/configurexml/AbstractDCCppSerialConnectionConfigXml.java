package jmri.jmrix.dccpp.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.dccpp.AbstractDCCppSerialConnectionConfig;

/**
 * Abstract base class to Handle XML persistance of layout connections 
 * by persistening an DCC++Serial Adapter (and connections). Note this is
 * named as the XML version of an AbstractDCCppSerialConnectionConfig object,
 * but it's actually persisting the DCC++ Serial Adapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 * @author Mark Underwood Copyright: Copyright (c) 2015
 *
 * Based on AbstractXNetSerialConnectionConfigXml by Paul Bender
 */
public abstract class AbstractDCCppSerialConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public AbstractDCCppSerialConnectionConfigXml() {
        super();
    }
    
    @Override
    protected void getInstance(Object object) {
        adapter=((AbstractDCCppSerialConnectionConfig) object).getAdapter();
    }

}
