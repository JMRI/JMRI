package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PortalListTest {

    @Test
    public void testCTor() {
        PortalList t = new PortalList( new OBlock("OB1", "Test"), null);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalListTest.class);

}
