package jmri.jmrit.beantable.signalmast;

import jmri.implementation.SignalSystemTestUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author	Bob Jacobsen Copyright 2014
 */
public class AddSignalMastPanelTest {

    @Test
    public void testDefaultSystems() {
        AddSignalMastPanel a = new AddSignalMastPanel();

        // check that "Basic Model Signals" (basic directory) system is present
        boolean found = false;
        for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
            if (a.sigSysBox.getItemAt(i).equals("Basic Model Signals")) {
                found = true;
            }
        }
        Assert.assertTrue("did not find Basic Model Signals", found);
    }

    @Test
    public void testSearch() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();

            AddSignalMastPanel a = new AddSignalMastPanel();

            // check that mock (test) system is present
            boolean found = false;
            for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
                if (a.sigSysBox.getItemAt(i).equals(SignalSystemTestUtil.getMockUserName())) {
                    found = true;
                }
            }
            Assert.assertTrue("did not find JUnit Test Signals", found);
        } catch (Exception e) {
            Assert.fail("testSearch exception thrown: " + e.getCause().getMessage());
        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
