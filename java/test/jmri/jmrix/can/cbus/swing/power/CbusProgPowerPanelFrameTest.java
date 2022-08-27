package jmri.jmrix.can.cbus.swing.power;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test CBUS programming track power panel frame
 * 
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerPanelFrameTest extends jmri.util.JmriJFrameTestBase {
    
    CbusPreferences preferences;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CbusProgPowerPanelFrame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        preferences = null;
        super.tearDown();
    }

    
}
