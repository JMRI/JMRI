package jmri.jmrix.pricom.downloader;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LoaderPanelActionTest {

    @Test
    public void testCTor() {
        LoaderPanelAction t = new LoaderPanelAction();
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

    // private final static Logger log = LoggerFactory.getLogger(LoaderPanelActionTest.class);

}
