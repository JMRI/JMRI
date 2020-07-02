package jmri.jmrit.ussctc;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * Tests for Follower classes in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class FollowerTest {

    @Test
    public void testCreate() {
        Follower f = new Follower("12", "34", false, "56");
        
        Assert.assertEquals("12", f.getOutputName());
        Assert.assertEquals("34", f.getSensorName());
        Assert.assertEquals(false, f.getInvert());
        Assert.assertEquals("56", f.getVetoName());
    }

    @Test
    public void testInstantiate() {
        Follower f = new Follower("12", "34", false, "56");
        f.instantiate();        
    }

    @Test
    public void testCreateRep() throws jmri.JmriException {
        JUnitUtil.initRouteManager();
        Follower f = new Follower("12", "34", false, "56");
        f.instantiate();
        new Follower("12");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
