package jmri.jmrix.rps.rpsmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RpsMonFrameTest extends jmri.util.JmriJFrameTestBase {

    private RpsSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new RpsMonFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsMonFrameTest.class);
}
