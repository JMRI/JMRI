package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import javax.swing.JPanel;

/**
 * Base tests for Simulator ConnectionConfigXml objects.
 * The AbstractSimulatorConnectionConfig is currently based on serial objects. 
 * most of the simulators save objects using classes derived directly from 
 * AbstractConnectionConfigXml.  This eventually needs to change.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractSimulatorConnectionConfigXmlTestBase extends AbstractConnectionConfigXmlTestBase {

    @Test
    @Override
    public void storeTest(){
        Assume.assumeNotNull(cc);
        cc.loadDetails(new JPanel());
        // load details MAY produce an error message if no ports are found.
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        Element e = xmlAdapter.store(cc);
        Assert.assertNotNull("XML Element Produced",e); 
        validateCommonDetails(cc,e);
        validateConnectionDetails(cc,e);
    }
}
