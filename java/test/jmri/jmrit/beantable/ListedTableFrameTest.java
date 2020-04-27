package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ListedTableFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()) {
           jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
           frame = new ListedTableFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ListedTableFrameTest.class);

}
