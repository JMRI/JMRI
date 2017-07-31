package jmri.jmrix.acela;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AcelaSensorTest {

    @Test
    public void testCTor() {
        AcelaSensor t = new AcelaSensor("AS1");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test2StringCTor() {
        AcelaSensor t = new AcelaSensor("AS1","test");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AcelaSensorTest.class.getName());

}
