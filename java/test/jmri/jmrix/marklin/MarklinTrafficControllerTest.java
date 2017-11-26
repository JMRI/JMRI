package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * MarklinTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.marklin.MarklinTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class MarklinTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new MarklinTrafficController();
    }
    
    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
