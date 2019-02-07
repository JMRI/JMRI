package jmri.jmrix.can.cbus;

// import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.IdTag;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    // tag unseen = 2
    // tag seen = 3

    @Test
    public void respondToCanReply(){
        
        CbusReporter r4 = new CbusReporter(4,tcis,"Reporter 4");
        CbusReporter r5 = new CbusReporter(5,tcis,"Reporter 5");
        Assert.assertNotNull("exists",r);
        
        Assert.assertEquals("state unset",IdTag.UNKNOWN,r.getState());
        
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
        
        Assert.assertEquals("r4 state set",IdTag.SEEN,r4.getState());
        Assert.assertNotNull("r4 report set",r4.getCurrentReport());
        Assert.assertEquals("r5 state unset",IdTag.UNKNOWN,r5.getState());
        Assert.assertEquals("r5 report unset",null,r5.getCurrentReport());
        
        IdTag tagA = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getByTagID("30393130AB");
        Assert.assertNotNull("exists",tagA);
        
        m.setElement(2, 0x05); // ev lo
        r4.reply(m);
        r5.reply(m);

        Assert.assertEquals("r5 tag seen",IdTag.SEEN,r5.getState());
        Assert.assertNotNull("r5 report set",r5.getCurrentReport());
        Assert.assertEquals("r4 tag gone",IdTag.UNSEEN,r4.getState());
        Assert.assertEquals("r4 report unset",null,r4.getCurrentReport());
        
        r.dispose();
    }

    // CanSystemConnectionMemo memo;
    TrafficControllerScaffold tcis;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        //    memo = new CanSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences());
        //  jmri.InstanceManager.setDefault(jmri.ReporterManager.class,new CbusReporterManager(memo));
        tcis = new TrafficControllerScaffold();
        r = new CbusReporter(1,tcis,"Reporter1");
    }
    

    @After
    public void tearDown() {
        r.dispose();
        r = null;
        tcis = null;
        JUnitUtil.tearDown();
    }

     private final static Logger log = LoggerFactory.getLogger(CbusReporterTest.class);

}
