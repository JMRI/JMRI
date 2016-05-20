/**
 * NceMonPanelTest.java
 *
 * Description:	    JUnit tests for the NceProgrammer class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.nce.ncemon;

import org.apache.log4j.Logger;
import java.util.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.ncemon.NceMonPanel;
import jmri.jmrix.nce.NceTrafficController;

import jmri.jmrix.nce.*;

public class NceMonPanelTest extends TestCase {

	NceInterfaceScaffold controller;  // holds dummy NceTrafficController for testing

    public void testCreate() {
        controller = new NceInterfaceScaffold();
        NceMonPanel f = new NceMonPanel();
        Assert.assertNotNull("exists", f );
    }
    
    
// Following are timing-specific, occasionally fail, so commented out    
/*     public void testMsg() { */
/*         NceMessage m = new NceMessage(3); */
/*         m.setBinary(false); */
/*         m.setOpCode('L'); */
/*         m.setElement(1, '0'); */
/*         m.setElement(2, 'A'); */
/*          */
/*         NceMonPanel f = new NceMonPanel(); */
/*          */
/*         f.message(m); */
/*          */
/*         Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getPanelText().length()); */
/*         Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getPanelText()); */
/*     } */
/*      */
/*     public void testReply() { */
/*         NceReply m = new NceReply(); */
/*         m.setBinary(false); */
/*         m.setOpCode('C'); */
/*         m.setElement(1, 'o'); */
/*         m.setElement(2, ':'); */
/*          */
/*         NceMonPanel f = new NceMonPanel(); */
/*          */
/*         f.reply(m); */
/*          */
/*         Assert.assertEquals("display", "rep: \"Co:\"\n", f.getPanelText()); */
/*         Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getPanelText().length()); */
/*     } */
    
    public void testWrite(){
        
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        Assert.assertNotNull("exists", t );
    }
    
    // service internal class to handle transmit/receive for tests
    class NceInterfaceScaffold extends NceTrafficController {
        public NceInterfaceScaffold() {
        }
        
        // override some NceInterfaceController methods for test purposes
        
        public boolean status() { return true;
        }
        
        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class
        public void sendNceMessage(NceMessage m, jmri.jmrix.nce.NceListener l) {
            if (log.isDebugEnabled()) log.debug("sendNceMessage ["+m+"]");
            // save a copy
            outbound.addElement(m);
        }
        
        // test control member functions
        
        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage (NceMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) log.debug("sendTestMessage    ["+m+"]");
            notifyMessage(m, null);
            return;
        }
        protected void sendTestReply (NceReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) log.debug("sendTestReply    ["+m+"]");
            notifyReply(m, null);
            return;
        }
        
        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }
        
    }
    
    // from here down is testing infrastructure
    
    public NceMonPanelTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceMonPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceMonPanelTest.class);
        return suite;
    }
    
    static Logger log = Logger.getLogger(NceMonPanelTest.class.getName());
    
}
