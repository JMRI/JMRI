
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;


public class LocoNetSlotTest extends TestCase {

    public LocoNetSlotTest(String s) {
	super(s);
    }

    public void testGetSlotSend()  {
        SlotManager slotmanager = new SlotManager();
        SlotListener p2=  new SlotListener(){
                public void notifyChangedSlot(LocoNetSlot l) {}
        };
        slotmanager.slotFromLocoAddress(21, p2);
        Assert.assertEquals("slot request message",
			    "bf 0 15 0 ",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    LocoNetInterfaceScaffold lnis;
    protected void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        log4jfixtureInst.setUp();
    }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
