package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;


/**
 * Test simple functioning of CbusNodeUserCommentsPane
 *
 * @author Steve Young Copyright (C) 2021
 */
public class CbusNodeUserCommentsPaneTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        CbusNodeUserCommentsPane t = new CbusNodeUserCommentsPane(null);
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeUserCommentsPaneTest.class);

}
