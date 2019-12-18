package jmri.jmrix.rps.aligntable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Test simple functioning of AlignTableFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AlignTableFrameTest extends jmri.util.JmriJFrameTestBase {

    private RpsSystemConnectionMemo memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        memo = new RpsSystemConnectionMemo();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new AlignTableFrame(memo);
	    }
    }

    @After
    @Override
    public void tearDown() {
	    memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }
}
