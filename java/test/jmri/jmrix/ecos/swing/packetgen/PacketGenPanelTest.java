package jmri.jmrix.ecos.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
        panel = new PacketGenPanel();
        title = "Send ECoS Command";
        helpTarget = "package.jmri.jmrix.ecos.swing.packetgen.PacketGenFrame";
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
