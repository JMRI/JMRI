package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetPhysicalLocationActionTest {

    @Test
    public void testCTor() {
        jmri.implementation.AbstractReporter r = new jmri.implementation.AbstractReporter("foo","bar") {
           @Override
           public int getState() {
              return state;
           }

           @Override
           public void setState(int s) {
              state = s;
           }
           int state = 0;
        };

        SetPhysicalLocationAction t = new SetPhysicalLocationAction("Test",r);
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

    // private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationActionTest.class);

}
