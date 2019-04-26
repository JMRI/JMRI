package jmri.jmrix.rps.rpsmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsMonFrameTest extends jmri.util.JmriJFrameTestBase {

    private RpsSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new RpsMonFrame(memo);
	}
    }

    @After
    public void tearDown() {
	memo = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsMonFrameTest.class);

}
