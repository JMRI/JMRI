package jmri.jmrix.ecos.swing.preferences;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of PreferencesFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PreferencesFrameTest extends jmri.util.JmriJFrameTestBase {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new PreferencesFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        super.tearDown();
    }
}
