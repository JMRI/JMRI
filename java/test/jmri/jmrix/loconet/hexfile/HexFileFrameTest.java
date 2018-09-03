package jmri.jmrix.loconet.hexfile;

import java.awt.GraphicsEnvironment;
import jmri.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class HexFileFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnGUI( ()-> {
            HexFileFrame t = new HexFileFrame();
            LnHexFilePort p = new LnHexFilePort();
            t.setAdapter(p);
            t.initComponents();
            t.configure();
            t.dispose();
        });
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HexFileFrameTest.class);

}
