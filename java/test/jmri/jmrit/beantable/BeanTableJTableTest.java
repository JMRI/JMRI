package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the BeanTableJTableTest class.
 * @author Steve Young Copyright (C) 2024
 */
public class BeanTableJTableTest {

    @Test
    public void testBeanTableJTableCtor() {
        MemoryTableDataModel dm = new MemoryTableDataModel();
        BeanTableJTable<jmri.Memory> t = new BeanTableJTable<>(dm);
        Assertions.assertNotNull(t);
        dm.dispose();
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
