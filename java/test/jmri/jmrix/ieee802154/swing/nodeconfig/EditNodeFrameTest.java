package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditNodeFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        IEEE802154TrafficController tc = new IEEE802154TrafficController() {
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
        if(!GraphicsEnvironment.isHeadless()){
           frame = new EditNodeFrame(tc,tc.newNode());
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditNodeFrameTest.class);

}
