package jmri.jmrix.tams;

import jmri.JmriException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TamsSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i){
       return "TS" + i;
    }

    @Test
    @Override
    public void testMakeSystemName() {
        Assert.assertEquals("TS10:10", l.makeSystemName("10:10"));
    }

    @Override
    @Test
    public void testCreateSystemName() throws JmriException {
        Assert.assertEquals(l.makeSystemName("10:10"),
                l.createSystemName("10:10", l.getSystemPrefix()));
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tc);  
        l = new TamsSensorManager(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsSensorManagerTest.class);

}
