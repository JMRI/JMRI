package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the TagCarItem class.
 * @author Steve Young Copyright (C) 2022
 */
public class TagCarItemTest {

    @Test
    public void testCtor() {
        TagCarItem t = new TagCarItem();
        Assertions.assertNotNull(t,"exists");
    }
    
    @Test
    public void testCtorNewTag() {
        TagCarItem t = new TagCarItem("TestNewTag");
        Assertions.assertNotNull(t,"exists");
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
