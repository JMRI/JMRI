package jmri.jmrix.can.cbus.swing.power;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test CBUS programming track power panel frame
 * 
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerPanelFrameTest extends jmri.util.JmriJFrameTestBase {
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CbusProgPowerPanelFrame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    
}
