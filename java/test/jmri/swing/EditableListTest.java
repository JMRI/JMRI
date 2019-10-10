package jmri.swing;

import org.junit.Test;

/**
 * Tests for the jmri.swing.EditableList class.
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class EditableListTest {

    @Test
    public void testCtor() {
        new EditableList<String>();
        new EditableList<Integer>();
        new EditableList<Object>();
    }

    @Test
    public void testCtorWithMode() {
        new EditableList<String>(new DefaultEditableListModel<String>());
        new EditableList<Integer>(new DefaultEditableListModel<Integer>());
        new EditableList<Object>(new DefaultEditableListModel<Object>());
    }

}
