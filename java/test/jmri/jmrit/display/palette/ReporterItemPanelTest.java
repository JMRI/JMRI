package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ReporterItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.Reporter> tableModel = PickListModel.reporterPickModelInstance();
        DisplayFrame df = new DisplayFrame("Reporter Item Panel Test");
        ReporterItemPanel t = new ReporterItemPanel(df,"IR01","",tableModel);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(df);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ReporterItemPanelTest.class);

}
