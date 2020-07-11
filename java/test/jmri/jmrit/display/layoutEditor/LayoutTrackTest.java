package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test common functioning of LayoutTrack.
 * Other tests inherit from this so that the
 * classes are still checked against the basic
 * LayoutTrack contract.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTrackTest {

    protected LayoutEditor le = null;

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        le = new LayoutEditor();
    }
    
    @AfterEach
    public void tearDown() {
        le = null;
        JUnitUtil.tearDown();
    }
}
