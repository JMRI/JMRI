package jmri.implementation;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalSystem;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SignalHeadSignalMast implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * updated to JUnit4 2016
 */
public class SignalHeadSignalMastTest {

    @Test
    public void testSetup() {
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalHeadManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1"));
    }

    @Test
    public void testTwoNameOneHeadCtorOK() {
        new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user");
    }

    @Test
    public void testHeld() {
        SignalMast m = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user");

        Assert.assertTrue(!m.getHeld());

        m.setHeld(true);
        Assert.assertTrue(m.getHeld());
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getHeld());

        m.setHeld(false);
        Assert.assertTrue(!m.getHeld());
        Assert.assertTrue(!InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getHeld());

    }

    @Test
    public void testLit() {
        SignalMast m = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user");

        Assert.assertTrue(m.getLit());

        m.setLit(false);
        Assert.assertTrue(!m.getLit());
        Assert.assertTrue(!InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getLit());

        m.setLit(true);
        Assert.assertTrue(m.getLit());
        Assert.assertTrue(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getLit());

    }

    @Test
    public void testTwoNameSe8cHeadCtorOK() {
        // create the SE8c heads 
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH:SE8C:\"255\";\"256\"") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH:SE8C:\"257\";\"258\"") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );

        // test uses those        
        new SignalHeadSignalMast("IF$shsm:AAR-1946:PL-2-high(IH:SE8C:\"255\";\"256\")(IH:SE8C:\"257\";\"258\")", "user");
    }

    @Test
    public void testOneNameOneHeadCtorOK() {
        new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)");
    }

    @Test
    public void testOldTwoNameCtorOK() {
        new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
    }

    @Test
    public void testOldOneNameCtorOK() {
        new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1");
    }

    @Test
    public void testOldOneNameCtorFailNoSystem() {
        try {
            new SignalHeadSignalMast("IF$shsm:notanaspect:one-searchlight:IH1");
            Assert.fail("should have thrown exception");
        } catch (IllegalArgumentException e1) {
            jmri.util.JUnitAppender.assertErrorMessage("Did not find signal definition: notanaspect");
        } catch (Exception e2) {
            Assert.fail("wrong exception: " + e2);
        }
    }

    @Test
    public void testAspects() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        Assert.assertEquals("check clear", "Clear", s.getAspect());
        s.setAspect("Stop");
        Assert.assertEquals("check stop", "Stop", s.getAspect());
    }

    @Test
    public void testAspectAttributes() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        Assert.assertEquals("../../../resources/icons/smallschematics/aspects/AAR-1946/SL-1-high-abs/rule-281.gif",
                s.getAppearanceMap().getProperty("Clear", "imagelink"));
    }

    @Test
    public void testAspectNotSet() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        Assert.assertNull("check null", s.getAspect());
    }

    @Test
    public void testAspectFail() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");

        try {
            s.setAspect("Not An Aspect, I Hope");
            Assert.fail("should have thrown exception");
        } catch (IllegalArgumentException e1) {
            jmri.util.JUnitAppender.assertWarnMessage("attempting to set invalid aspect: Not An Aspect, I Hope on mast: user");
        } catch (Exception e2) {
            Assert.fail("wrong exception: " + e2);
        }

        Assert.assertEquals("check clear", "Clear", s.getAspect()); // unchanged after failed request
    }

    @Test
    public void testConfigureOneSearchLight() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        Assert.assertEquals("check green", SignalHead.GREEN,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getAppearance());

        s.setAspect("Approach");
        Assert.assertEquals("check yellow", SignalHead.YELLOW,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getAppearance());
    }

    @Test
    public void testConfigureTwoSearchLight() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2", "user");

        s.setAspect("Clear");
        Assert.assertEquals("Clear head 1 green", SignalHead.GREEN,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getAppearance());
        Assert.assertEquals("Clear head 2 red", SignalHead.RED,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2").getAppearance());

        s.setAspect("Diverging Approach");
        Assert.assertEquals("Diverging Approach head 1 red", SignalHead.RED,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1").getAppearance());
        Assert.assertEquals("Diverging Approach head 2 yellow", SignalHead.YELLOW,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2").getAppearance());
    }

    @Test
    public void testOneSearchLightViaManager() {
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$shsm:basic:one-searchlight:IH2");

        s.setAspect("Clear");
        Assert.assertEquals("check green", SignalHead.GREEN,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2").getAppearance());

        s.setAspect("Approach");
        Assert.assertEquals("check yellow", SignalHead.YELLOW,
                InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2").getAppearance());
    }

    @Test
    public void testSignalSystemLink() {
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$shsm:basic:one-searchlight:IH2");

        SignalSystem sy = s.getSignalSystem();
        Assert.assertNotNull(sy);

        Assert.assertEquals("Proceed", s.getSignalSystem().getProperty("Clear", "indication"));
    }

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH3") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadSignalMastTest.class);
}
