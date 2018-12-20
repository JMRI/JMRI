package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for testing objects inheriting from LayoutTrack
 * <p>
 *
 * @author Paul Bender Copyright 2018
 */
public class AbstractLayoutTrackTestBase {

    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
