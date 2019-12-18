package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.AbstractSerialPortController;
import jmri.util.ThreadingUtil;

import javax.swing.JPanel;

/**
 * Base tests for SerialConnectionConfigXml objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractSerialConnectionConfigXmlTestBase extends AbstractConnectionConfigXmlTestBase {

    @Test
    @Override
    public void storeTest(){
        Assume.assumeNotNull(cc);
        cc.loadDetails(new JPanel());
        // load details MAY produce an error message if no ports are found.
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        Element e = xmlAdapter.store(cc);
        Assert.assertNotNull("XML Element Produced", e); 
        if(e.getAttribute("class")!=null){
           Assert.assertEquals("class", xmlAdapter.getClass().getName(), e.getAttribute("class").getValue());
        }
        validateCommonDetails(cc, e);
        validateConnectionDetails(cc, e);
    }

    @Test(timeout=5000)
    @Override
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
        Assume.assumeNotNull(cc);
        // reset the profile manager for this test, so it can run independently.
        jmri.util.JUnitUtil.resetProfileManager();
        // This test requires a configure manager.
        jmri.util.JUnitUtil.initConfigureManager();
        // Running this on the UI thread fixes some ConcurrentModificationExceptions errors.
        ThreadingUtil.runOnGUI(()->{
            cc.loadDetails(new JPanel());
            cc.setDisabled(true); // so we don't try to start the connection on load.
        });
        // load details MAY produce an error message if no ports are found.
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        Element e = xmlAdapter.store(cc);
        //load what we just produced.
        xmlAdapter.load(e, e);
    }

    /**
     * { @inheritdoc }
     */
    @Override
    protected void validateConnectionDetails(ConnectionConfig cc, Element e){
       Assume.assumeNotNull(cc.getAdapter());
       // Serial ports have port names and baud rates.
       AbstractSerialPortController spc = (AbstractSerialPortController) cc.getAdapter();
       if(spc.getCurrentPortName()!=null) {
          Assert.assertEquals("port", spc.getCurrentPortName(), e.getAttribute("port").getValue());
       } else {
          Assert.assertEquals("port", Bundle.getMessage("noneSelected"), e.getAttribute("port").getValue());
       }
       if(spc.getCurrentBaudNumber()!=null) {
          Assert.assertEquals("speed", spc.getCurrentBaudNumber(), e.getAttribute("speed").getValue());
          // speed is not stored as I18N formatted string but as int string
       } else {
          Assert.assertEquals("speed", Bundle.getMessage("noneSelected"), e.getAttribute("speed").getValue());
       }
    }

}
