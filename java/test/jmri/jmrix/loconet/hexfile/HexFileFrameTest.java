package jmri.jmrix.loconet.hexfile;

import java.awt.GraphicsEnvironment;
import jmri.util.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HexFileFrameTest {

    @Test
    public void testCTor() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnHexFilePort p = new LnHexFilePort();
        
        HexFileFrame f = new HexFileFrame();

        ThreadingUtil.runOnGUI( ()-> {
            f.setAdapter(p);
            f.initComponents();
            f.configure();
       });

        ThreadingUtil.runOnGUI( ()-> {
            f.dispose();
       });
            
        p.dispose();
        f.sourceThread.stop();
        f.sourceThread.join();
        f.dispose();   
 }   

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
