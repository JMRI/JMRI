package jmri.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import jmri.util.swing.DedupingPropertyChangeListenerTest;

/**
 * Invokes complete set of tests in the jmri.swing tree
 *
 * @author	Bob Jacobsen Copyright 2014
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    EditableListTest.class,
    JmriJTablePersistenceManagerTest.class,
    ConnectionLabelTest.class,
    AboutDialogTest.class,
    AboutActionTest.class,
    DefaultEditableListModelTest.class,
    RowSorterUtilTest.class,
    JTitledSeparatorTest.class,
    DefaultListCellEditorTest.class,
    DedupingPropertyChangeListenerTest.class,
    ManagerComboBoxTest.class,
    NamedBeanComboBoxTest.class,
    SystemNameValidatorTest.class
})
public class PackageTest {
}
