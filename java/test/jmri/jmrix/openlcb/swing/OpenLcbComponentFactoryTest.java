package jmri.jmrix.openlcb.swing;

import jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OpenLcbComponentFactoryTest {

    @Test
    public void testCTor() {
        OpenLcbComponentFactory t = new OpenLcbComponentFactory(new OlcbSystemConnectionMemoScaffold());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OpenLcbComponentFactoryTest.class);

}
