package jmri.jmrit.operations.trains;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.Before;

/**
 * Tests for the TrainConductorPanel class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainConductorPanelTest extends jmri.jmrit.operations.CommonConductorYardmasterPanelTest {

    @Before
    @Override
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        p = new TrainConductorPanel();
    }

}
