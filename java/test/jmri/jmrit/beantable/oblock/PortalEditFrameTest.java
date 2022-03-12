package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import java.awt.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
public class PortalEditFrameTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        PortalEditFrame pef = new PortalEditFrame(
                "New Portal",
                null,
                null);
        Assertions.assertNotNull(pef, "New PEF exists");
    }

    @Test
    public void testPortalCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Portal p = new Portal("portal-1");
        //TableFrames tf = new TableFrames();
        //PortalTableModel model = new PortalTableModel(tf);
        PortalEditFrame pef = new PortalEditFrame(
                "Edit Portal-1",
                p,
                null);
        Assertions.assertNotNull(pef, "Portal PEF exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalEditFrameTest.class);

}
