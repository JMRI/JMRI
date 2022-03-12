package jmri.jmrit.beantable.light;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class AddEditSingleLightControlFrameTest {
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCTor() {
        LightControlPane lcp = new LightControlPane();
        AddEditSingleLightControlFrame t = new AddEditSingleLightControlFrame(lcp,null);
        assertNotNull(t);
        lcp.dispose();
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
