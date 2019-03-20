package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import jmri.util.ThreadingUtil;

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
        Element e = xmlAdapter.store(cc);
        Assert.assertNotNull("XML Element Produced", e);
        if (e.getAttribute("class")!=null) {
           Assert.assertEquals("class", xmlAdapter.getClass().getName(), e.getAttribute("class").getValue());
        }
        validateCommonDetails(cc, e);
        validateConnectionDetails(cc, e);
    }

    @Test(timeout=5000)
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
        Assume.assumeNotNull(cc);
        // reset the profile manager for this test, so it can run independently.
        jmri.util.JUnitUtil.resetProfileManager();
        // This test requires a configure manager.
        jmri.util.JUnitUtil.initConfigureManager();
        // Running this on the UI thread fixes some ConcurrentModificationExceptions errors.
        ThreadingUtil.runOnGUI(()-> {
                    cc.loadDetails(new JPanel());
                    cc.setDisabled(true); // so we don't try to start the connection on load.
                });
        Element e = xmlAdapter.store(cc);
        try {
           //load what we just produced.
           xmlAdapter.load(e,e);
        } catch (java.util.ConcurrentModificationException cme) {
           cme.printStackTrace();
           throw cme;
        }
    }

    /**
     * Validate the common details for ConnectionConfig match the values in 
     * the xml element.
     *
     * @param cc connection configuration object
     * @param e Element object.
     */
    protected void validateCommonDetails(ConnectionConfig cc,Element e){
       Assume.assumeNotNull(cc.getAdapter());
       if (cc.getAdapter().getSystemConnectionMemo()!=null) {
          Assert.assertNotNull(cc.getAdapter().getSystemConnectionMemo().getUserName());
          Assert.assertEquals("UserName", cc.getAdapter().getSystemConnectionMemo().getUserName(), e.getAttribute("userName").getValue());
          Assert.assertEquals("SystemPrefix", cc.getAdapter().getSystemConnectionMemo().getSystemPrefix(), e.getAttribute("systemPrefix").getValue());
       } 
       if (cc.getAdapter().getManufacturer() != null) {
          Assert.assertEquals("Manufacturer", cc.getAdapter().getManufacturer(),e.getAttribute("manufacturer").getValue());
       }
       Assert.assertEquals("disabled", cc.getAdapter().getDisabled(), e.getAttribute("disabled").getValue().equals("yes"));
    }

    /**
     * Validate the connection specific details for ConnectionConfig match 
     * the values in the xml element.
     *
     * @param cc connection configuration object
     * @param e Element object.
     */
    protected void validateConnectionDetails(ConnectionConfig cc, Element e){
       // this implementation doesn't check any details.
    }

}
