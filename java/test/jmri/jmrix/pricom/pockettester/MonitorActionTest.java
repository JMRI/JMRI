package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MonitorActionTest {

    @Test
    public void testCTor() {
        MonitorAction t = new MonitorAction(){
            @Override
            void connect(DataListener l){
            }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DataSourceActionTest.class);

}
