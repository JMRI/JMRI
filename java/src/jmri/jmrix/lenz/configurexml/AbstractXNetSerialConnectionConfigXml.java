package jmri.jmrix.lenz.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig;

/**
 * Abstract base class to Handle XML persistance of layout connections 
 * by persistening an XPressNetSerial Adapter (and connections). Note this is
 * named as the XML version of an AbstraxtXNetSerialConnectionConfig object,
 * but it's actually persisting the XPressNet Serial Adapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 * @version $Revision$
 */
abstract public class AbstractXNetSerialConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public AbstractXNetSerialConnectionConfigXml() {
        super();
    }
    
    @Override
    protected void getInstance(Object object) {
        adapter=((AbstractXNetSerialConnectionConfig) object).getAdapter();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractXNetSerialConnectionConfigXml.class.getName());

}
