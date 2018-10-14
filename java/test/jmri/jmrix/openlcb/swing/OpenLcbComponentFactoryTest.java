package jmri.jmrix.openlcb.swing;

import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OpenLcbComponentFactoryTest {

    @Test
    public void testCTor() {
        OpenLcbComponentFactory t = new OpenLcbComponentFactory(new OlcbSystemConnectionMemo());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OpenLcbComponentFactoryTest.class);

}
