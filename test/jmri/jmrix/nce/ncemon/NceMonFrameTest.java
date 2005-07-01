/**
 * NceMonFrameTest.java
 *
 * Description:	    JUnit tests for the NceProgrammer class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.nce.ncemon;

import jmri.*;

import java.util.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.ncemon.NceMonFrame;

import jmri.jmrix.nce.*;

public class NceMonFrameTest extends TestCase {
    
    public void testCreate() {
        NceMonFrame f = new NceMonFrame();
    }
    
    
// Following are timing-specific, occasionally fail, so commented out    
/*     public void testMsg() { */
/*         NceMessage m = new NceMessage(3); */
/*         m.setBinary(false); */
/*         m.setOpCode('L'); */
/*         m.setElement(1, '0'); */
/*         m.setElement(2, 'A'); */
/*          */
/*         NceMonFrame f = new NceMonFrame(); */
/*          */
/*         f.message(m); */
/*          */
/*         Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length()); */
/*         Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText()); */
/*     } */
/*      */
/*     public void testReply() { */
/*         NceReply m = new NceReply(); */
/*         m.setBinary(false); */
/*         m.setOpCode('C'); */
/*         m.setElement(1, 'o'); */
/*         m.setElement(2, ':'); */
/*          */
/*         NceMonFrame f = new NceMonFrame(); */
/*          */
/*         f.reply(m); */
/*          */
/*         Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText()); */
/*         Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length()); */
/*     } */
    
    public void testWrite() throws JmriException {
        
        // infrastructure objects
        NceInterfaceScaffold t = new NceInterfaceScaffold();
        
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
        public Vector outbound = new Vector();  // public OK here, so long as this is a test class
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
    
    public NceMonFrameTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceMonFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceMonFrameTest.class);
        return suite;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMonFrameTest.class.getName());
    
}
