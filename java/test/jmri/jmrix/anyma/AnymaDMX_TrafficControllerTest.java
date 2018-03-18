package jmri.jmrix.anyma;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AnymaDMX_TrafficController class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_TrafficControllerTest {

    @Test
    public void ConstructorTest() {
        AnymaDMX_TrafficController atc = new AnymaDMX_TrafficController();
        Assert.assertNotNull("ConnectionConfig constructor", atc);
        atc.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
