package jmri.jmrit.logixng.tools.swing.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ModuleEditorMaleSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class ModuleEditorMaleSocketSwingTest {

    @Test
    public void testCtor() {
        ModuleEditorMaleSocketSwing t = new ModuleEditorMaleSocketSwing();
        Assertions.assertNotNull( t, "not null");
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
