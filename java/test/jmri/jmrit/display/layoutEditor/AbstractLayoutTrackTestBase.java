package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Base for testing objects inheriting from LayoutTrack
 * <p>
 *
 * @author Paul Bender Copyright 2018
 * @author Bob Jacobsen Copyright 2018
 */
abstract public class AbstractLayoutTrackTestBase {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
