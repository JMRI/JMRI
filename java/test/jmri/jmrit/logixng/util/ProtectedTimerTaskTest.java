package jmri.jmrit.logixng.util;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull(t, "not null");
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtectedTimerTaskTest.class);
}
