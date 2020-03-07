package jmri.jmrix.can.cbus;

import jmri.IdTag;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }
    
    private TrafficControllerScaffold tcis;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        r = new CbusReporter(1, tcis, "Test");
    }

    @After
    @Override
    public void tearDown() {
        tcis = null;
        r = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
    
    @Test
    public void respondToCanReply(){
        
        // a new tag provided by Reporter4 then moves to Reporter 5
        CbusReporter r4 = new CbusReporter(4,tcis,"Reporter 4");
        CbusReporter r5 = new CbusReporter(5,tcis,"Reporter 5");

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_DDES);
        m.setElement(1, 0x00); // ev hi
        m.setElement(2, 0x04); // ev lo
        m.setElement(3, 0x30); // tag1
        m.setElement(4, 0x39); // tag2
        m.setElement(5, 0x31); // tag3
        m.setElement(6, 0x30); // tag4
        m.setElement(7, 0xAB); // tag5
        r4.reply(m);
        r5.reply(m);
        
        // tag unseen = 2
        // tag seen = 3

        Assert.assertEquals("r4 state set",IdTag.SEEN,r4.getState());
        Assert.assertNotNull("r4 report set",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset",IdTag.UNKNOWN,r5.getState());
        Assert.assertEquals("r5 report unset",null,r5.getCurrentReport());

        m.setElement(2, 0x05); // ev lo
        r4.reply(m);
        r5.reply(m);

        Assert.assertEquals("r5 tag seen",IdTag.SEEN,r5.getState());
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());
        Assert.assertEquals("r4 tag gone",IdTag.UNSEEN,r4.getState());
        Assert.assertEquals("r4 report unset",null,r4.getCurrentReport());
        
        CanReply m2 = new CanReply(tcis.getCanid());
        m2.setNumDataElements(8);
        m2.setElement(0, CbusConstants.CBUS_ACDAT);
        m2.setElement(1, 0x00); // ev hi
        m2.setElement(2, 0x04); // ev lo
        m2.setElement(3, 0x30); // tag1
        m2.setElement(4, 0x39); // tag2
        m2.setElement(5, 0x31); // tag3
        m2.setElement(6, 0x30); // tag4
        m2.setElement(7, 0xAB); // tag5
        
        r4.reply(m2);
        r5.reply(m2);
        
        Assert.assertEquals("r4 state set CBUS_ACDAT",IdTag.SEEN,r4.getState());
        Assert.assertNotNull("r4 report set CBUS_ACDAT",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset CBUS_ACDAT",IdTag.UNSEEN,r5.getState());
        Assert.assertEquals("r5 report unset CBUS_ACDAT",null,r5.getCurrentReport());
        
        m2.setElement(2, 0x05); // ev lo
        
        m2.setExtended(true);
        r5.reply(m2);
        Assert.assertEquals("r5 state unset extended",IdTag.UNSEEN,r5.getState());
        
        m2.setExtended(false);
        m2.setRtr(true);
        r5.reply(m2);
        Assert.assertEquals("r5 state unset rtr",IdTag.UNSEEN,r5.getState());
        
        m2.setRtr(false);
        m2.setElement(0, 0x05); // random OPC not related to reporters
        r5.reply(m2);
        Assert.assertEquals("r5 state unset random opc",IdTag.UNSEEN,r5.getState());
        
        m2.setElement(0, CbusConstants.CBUS_DDES); // put it back
        r5.reply(m2);
        Assert.assertEquals("r5 state set ok after incorrect msgs",IdTag.SEEN,r5.getState());
        
        Assert.assertEquals("r4 state unseen",IdTag.UNSEEN,r4.getState());
        
        CanMessage m3 = new CanMessage(tcis.getCanid());
        m3.setNumDataElements(8);
        m3.setElement(0, CbusConstants.CBUS_ACDAT);
        m3.setElement(1, 0x00); // ev hi
        m3.setElement(2, 0x04); // ev lo
        m3.setElement(3, 0x30); // tag1
        m3.setElement(4, 0x39); // tag2
        m3.setElement(5, 0x31); // tag3
        m3.setElement(6, 0x30); // tag4
        m3.setElement(7, 0xAB); // tag5
        
        r4.message(m3);
        
        Assert.assertEquals("r4 seen after CBUS_ACDAT outgoing message",IdTag.SEEN,r4.getState());
        
        r.dispose();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusReporterTest.class);

}
