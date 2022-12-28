package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of AssociateTag Panel.
 *
 * @author Steve Young Copyright (C) 2022
 */
public class AssociateTagTest {
    
    @Test
    public void testCtor() {
        AssociateTag panel = new AssociateTag("TestAssociateTag");
        Assertions.assertNotNull(panel,"exists");
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
