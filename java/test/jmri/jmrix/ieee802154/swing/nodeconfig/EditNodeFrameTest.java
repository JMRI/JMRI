package jmri.jmrix.ieee802154.swing.nodeconfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.jmrix.ieee802154.IEEE802154Node;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditNodeFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        IEEE802154TrafficController tc = new IEEE802154TrafficController() {
            @Override
            public void setInstance() {
            }
            @Override
            protected AbstractMRReply newReply() {
                return null;
            }
            @Override
            public IEEE802154Node newNode() {
                return new IEEE802154Node(){
                     @Override
                     public AbstractMRMessage createInitPacket(){
                         return null;
                     }
                     @Override
                     public AbstractMRMessage createOutPacket(){
                         return null;
                     }
                     @Override
                     public boolean getSensorsActive(){
                         return false;
                     }
                     @Override
                     public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l){
                         return false;
                     }
                     @Override
                     public void resetTimeout(AbstractMRMessage m){
                     }
                };
            }
        };
        EditNodeFrame t = new EditNodeFrame(tc,tc.newNode());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EditNodeFrameTest.class.getName());

}
