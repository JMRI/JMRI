package jmri.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.swing.EditableList class.
 *
 * @author Bob Jacobsen Copyright 2014
 */
public class EditableListTest {

    @Test
    public void testCtor() {
        Assertions.assertNotNull( new EditableList<String>() );
        Assertions.assertNotNull( new EditableList<Integer>() );
        Assertions.assertNotNull( new EditableList<Object>() );
    }

    @Test
    public void testCtorWithMode() {
        Assertions.assertNotNull( new DefaultEditableListModel<String>() );
        Assertions.assertNotNull( new DefaultEditableListModel<Integer>() );
        Assertions.assertNotNull( new DefaultEditableListModel<Object>() );
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
