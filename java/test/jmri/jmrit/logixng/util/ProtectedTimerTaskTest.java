package jmri.jmrit.logixng.util;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ProtectedTimerTask
 * 
 * @author Daniel Bergqvist 2020
 */
public class ProtectedTimerTaskTest {

    @Test
    public void testCtor() {
        ProtectedTimerTask t = new ProtectedTimerTask(){
            @Override
            public void execute() {
                // Do nothing
            }
        };
        Assert.assertNotNull("not null", t);
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
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtectedTimerTaskTest.class);
}
