package jmri.jmrix.pricom.downloader;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LoaderFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
          frame = new LoaderFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LoaderFrameTest.class);

}
