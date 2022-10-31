package jmri.swing;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RowSorterUtilTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testUnsortedByDefault() {
        JTable table = new JTable();
        table.setName("Test Sorter Table");
        RowSorter<? extends TableModel> sorter = new TableRowSorter<>(table.getModel());
        SortOrder so = RowSorterUtil.getSortOrder(sorter, 0);
        Assertions.assertNotNull(so);
        Assertions.assertEquals(SortOrder.UNSORTED, so);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RowSorterUtilTest.class);

}
