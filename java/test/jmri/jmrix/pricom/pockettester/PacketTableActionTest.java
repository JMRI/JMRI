package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PacketTableActionTest {

    @Test
    public void testCTor() {
        PacketTableAction t = new PacketTableAction(){
            @Override
            void connect(DataListener l){
            }
        };
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DataSourceActionTest.class);

}
