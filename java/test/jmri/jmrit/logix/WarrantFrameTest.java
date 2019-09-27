package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new WarrantFrame(new Warrant("IW0", "AllTestWarrant"));
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantFrameTest.class);
}
