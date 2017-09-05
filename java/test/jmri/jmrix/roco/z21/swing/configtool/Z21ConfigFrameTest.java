package jmri.jmrix.roco.z21.swing.configtool;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Z21ConfigFrame class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class Z21ConfigFrameTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Test
    public void MemoConstructorTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Z21ConfigFrame f = new Z21ConfigFrame(memo);
        Assert.assertNotNull("Z21ConfigFrame constructor", f);
        f.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
    }

    @After
    public void tearDown() {
        memo = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
