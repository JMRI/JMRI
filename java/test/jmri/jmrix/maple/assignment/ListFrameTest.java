package jmri.jmrix.maple.assignment;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ListFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ListFrameTest extends jmri.util.JmriJFrameTestBase {

    private MapleSystemConnectionMemo _memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        _memo = new MapleSystemConnectionMemo("K", "Maple");
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new ListFrame(_memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        _memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
