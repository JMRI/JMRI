package jmri.jmrix.rps.swing.polling;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PollTableFrameTest extends jmri.util.JmriJFrameTestBase {

    private RpsSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new PollTableFrame(memo);
        }
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PollTableFrameTest.class);

}
