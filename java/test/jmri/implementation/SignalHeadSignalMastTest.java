package jmri.implementation;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalSystem;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SignalHeadSignalMast implementation
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * updated to JUnit4 2016
 * updated to JUnit5 2024
 */
public class SignalHeadSignalMastTest {

    @Test
    public void testSetup() {
        assertNotNull(InstanceManager.getDefault(SignalHeadManager.class));
        assertNotNull(InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1"));
    }

    @Test
    public void testTwoNameOneHeadCtorOK() {
        assertNotNull(
            new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user"));
    }

    @Test
    public void testHeld() {
        SignalMast m = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user");

        assertFalse(m.getHeld());

        m.setHeld(true);
        assertTrue(m.getHeld());
        SignalHead ih1 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        assertNotNull(ih1);
        assertTrue( ih1.getHeld() );

        m.setHeld(false);
        assertFalse( m.getHeld());
        assertFalse( ih1.getHeld());

    }

    @Test
    public void testLit() {
        SignalMast m = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)", "user");

        assertTrue(m.getLit());

        m.setLit(false);
        assertFalse( m.getLit());
        SignalHead ih1 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        assertNotNull( ih1 );
        assertFalse( ih1.getLit());

        m.setLit(true);
        assertTrue(m.getLit());
        assertTrue(ih1.getLit());

    }

    @Test
    public void testTwoNameSe8cHeadCtorOK() {
        // create the SE8c heads 
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH:SE8C:\"255\";\"256\"") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH:SE8C:\"257\";\"258\"") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );

        // test uses those
        assertNotNull(
        new SignalHeadSignalMast("IF$shsm:AAR-1946:PL-2-high(IH:SE8C:\"255\";\"256\")(IH:SE8C:\"257\";\"258\")", "user"));
    }

    @Test
    public void testOneNameOneHeadCtorOK() {
        assertNotNull(
            new SignalHeadSignalMast("IF$shsm:basic:one-searchlight(IH1)"));
    }

    @Test
    public void testOldTwoNameCtorOK() {
        assertNotNull(
            new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user"));
    }

    @Test
    public void testOldOneNameCtorOK() {
        assertNotNull(
            new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1"));
    }

    @Test
    public void testOldOneNameCtorFailNoSystem() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            new SignalHeadSignalMast("IF$shsm:notanaspect:one-searchlight:IH1").setHeld(true),
            "should have thrown exception");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("notanaspect"));
        jmri.util.JUnitAppender.assertErrorMessage("Did not find signal definition: notanaspect");
    }

    @Test
    public void testAspects() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        assertEquals( "Clear", s.getAspect(), "check clear");
        s.setAspect("Stop");
        assertEquals( "Stop", s.getAspect(), "check stop");
    }

    @Test
    public void testAspectAttributes() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        assertEquals("../../../resources/icons/smallschematics/aspects/AAR-1946/SL-1-high-abs/rule-281.gif",
                s.getAppearanceMap().getProperty("Clear", "imagelink"));
    }

    @Test
    public void testAspectNotSet() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        assertNull( s.getAspect(), "check null");
    }

    @Test
    public void testAspectFail() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");

        Exception ex = assertThrows( IllegalArgumentException.class, () ->
            s.setAspect("Not An Aspect, I Hope"));
        assertNotNull( ex );
        assertTrue(ex.getMessage().contains("Not An Aspect, I Hope"));
        jmri.util.JUnitAppender.assertWarnMessage("attempting to set invalid aspect: Not An Aspect, I Hope on mast: user");

        assertEquals( "Clear", s.getAspect(), "check clear unchanged after failed request");
    }

    @Test
    public void testConfigureOneSearchLight() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");

        s.setAspect("Clear");
        SignalHead ih1 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        assertNotNull( ih1 );
        assertEquals( SignalHead.GREEN, ih1.getAppearance(), "check green");

        s.setAspect("Approach");
        assertEquals( SignalHead.YELLOW, ih1.getAppearance(), "check yellow");
    }

    @Test
    public void testConfigureTwoSearchLight() {
        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2", "user");

        s.setAspect("Clear");
        SignalHead ih1 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH1");
        assertNotNull( ih1 );
        SignalHead ih2 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH2");
        assertNotNull( ih2 );
        assertEquals( SignalHead.GREEN,ih1.getAppearance(), "Clear head 1 green");
        assertEquals( SignalHead.RED, ih2.getAppearance(), "Clear head 2 red");

        s.setAspect("Diverging Approach");
        assertEquals( SignalHead.RED, ih1.getAppearance(), "Diverging Approach head 1 red");
        assertEquals( SignalHead.YELLOW, ih2.getAppearance(), "Diverging Approach head 2 yellow");
    }

    @Test
    public void testOneSearchLightViaManager() {
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$shsm:basic:one-searchlight:IH2");

        SignalHead ih2 = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("IH2");
        assertNotNull( ih2 );

        s.setAspect("Clear");
        assertEquals( SignalHead.GREEN, ih2.getAppearance(), "check green");

        s.setAspect("Approach");
        assertEquals( SignalHead.YELLOW,
                ih2.getAppearance(), "check yellow");
    }

    @Test
    public void testSignalSystemLink() {
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$shsm:basic:one-searchlight:IH2");

        SignalSystem sy = s.getSignalSystem();
        assertNotNull(sy);

        assertEquals( s.getSignalSystem().getProperty("Clear", "indication"), "Proceed");
    }

    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSignalHeadManager();
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH3") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadSignalMastTest.class);
}
