package jmri.jmrix.secsi.serialmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest extends jmri.util.JmriJFrameTestBase {

    private SecsiSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SerialMonFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getTrafficController().terminateThreads();
        memo = null;
        super.tearDown();
    }
}
