package jmri.jmrix.mrc;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MrcMonPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MrcSystemConnectionMemoTest extends SystemConnectionMemoTestBase<MrcSystemConnectionMemo> {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        scm = new jmri.jmrix.mrc.MrcSystemConnectionMemo();
        jmri.jmrix.mrc.MrcInterfaceScaffold tc = new jmri.jmrix.mrc.MrcInterfaceScaffold();
        scm.setMrcTrafficController(tc);
        jmri.InstanceManager.store(scm, MrcSystemConnectionMemo.class);
        scm.configureManagers();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
