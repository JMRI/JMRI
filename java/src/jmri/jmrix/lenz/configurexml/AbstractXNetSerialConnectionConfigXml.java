package jmri.jmrix.lenz.configurexml;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig;

/**
 * Abstract base class to Handle XML persistance of layout connections 
 * by persistening an XpressNetSerial Adapter (and connections). Note this is
 * named as the XML version of an AbstraxtXNetSerialConnectionConfig object,
 * but it's actually persisting the XpressNet Serial Adapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 */
abstract public class AbstractXNetSerialConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public AbstractXNetSerialConnectionConfigXml() {
        super();
    }
    
    @Override
    protected void getInstance(Object object) {
        adapter=((AbstractXNetSerialConnectionConfig) object).getAdapter();
    }

}
