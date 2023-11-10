package jmri.jmrix.bidib;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.implementation.AbstractLightTestBase;
import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBLight class
 * 
 * TODO: Test different types of Lights
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBLightTest  extends AbstractLightTestBase {
    
    BiDiBSystemConnectionMemo memo;
    // actually there is no AbstractVariableLightTestBase and variable t is just a 'Light'.
    // Since we would like to test VariableLight features, we use our own instance variable named 'vl'
    BiDiBLight vl;    
    
    @Override
    public int numListeners() {
        return 0; //TODO: handle Bidib Message Receiver Listeners
    }

    @Override
    public void checkOffMsgSent() {
        // not used, but must be implemented
    }

    @Override
    public void checkOnMsgSent() {
        // not used, but must be implemented
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        BiDiBLightManager lm = new BiDiBLightManager(memo);
        String p = memo.getSystemPrefix();
        vl = new BiDiBLight(p + lm.typeLetter() + "Test1:13L", lm);
        t = vl;
        assertTrue("invalid address", vl.getAddress().isValid());
        // we created a LIGHTPORT - use dimming function 2 and 3 for ON/OFF
//        vl.setMinIntensity(0.02);
//        vl.setMaxIntensity(0.03);
    }
    
    @AfterEach
    public void tearDown() {
        vl.dispose();
        vl = null;
        t = null;
        JUnitUtil.tearDown();
    }

}
