package jmri.jmrix.roco.z21.swing.configtool;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Z21ConfigFrame class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class Z21ConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();

        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        if(!GraphicsEnvironment.isHeadless()){
          frame = new Z21ConfigFrame(memo);
	}
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
