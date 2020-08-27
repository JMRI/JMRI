package jmri.jmrix.rps.swing.debugger;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class DebuggerFrameTest extends jmri.util.JmriJFrameTestBase {

    private RpsSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new RpsSystemConnectionMemo();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new DebuggerFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DebuggerFrameTest.class);

}
