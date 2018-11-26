package jmri.jmrit.operations.locations;

import jmri.util.JUnitUtil;
import org.junit.Before;

/**
 * Tests for the YardmasterPanel class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class YardmasterPanelTest extends jmri.jmrit.operations.CommonConductorYardmasterPanelTest {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        p = new YardmasterPanel();
    }

}
