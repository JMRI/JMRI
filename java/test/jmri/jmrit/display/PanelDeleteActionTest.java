package jmri.jmrit.display;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2021
 */
public class PanelDeleteActionTest {

    @Test
    public void testCTor() {
        PanelDeleteAction t = new PanelDeleteAction();
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

    // private final static Logger log = LoggerFactory.getLogger(NewPanelActionTest.class);
}
