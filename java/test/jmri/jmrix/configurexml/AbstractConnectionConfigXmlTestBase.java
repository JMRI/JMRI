package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import javax.swing.JPanel;

/**
 * Base tests for ConnectionConfigXml objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractConnectionConfigXmlTestBase extends jmri.configurexml.AbstractXmlAdapterTestBase {

    protected ConnectionConfig cc = null;

    @Test
    public void getInstanceTest() {
        ((AbstractConnectionConfigXml)xmlAdapter).getInstance();
    }

    @Test
    public void storeTest(){
        Assume.assumeNotNull(cc);
        cc.loadDetails(new JPanel());
        // load details MAY produce an error message if no ports are found.
        // this is technically only required for serial ConnectionConfig objects
        // but until we track down the non-serial ones generating the message
        // we will suppress it here.
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        Element e = xmlAdapter.store(cc);
        Assert.assertNotNull("XML Element Produced",e); 
    }

}
