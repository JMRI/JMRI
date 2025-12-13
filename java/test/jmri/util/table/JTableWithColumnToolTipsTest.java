package jmri.util.table;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of JTableWithColumnToolTips
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class JTableWithColumnToolTipsTest  {

    private final String[] COLUMNS = {"Column 1 Name"};

    private final String[] TIPS = {"Tip1"};

    private final Object[][] DATA = { {"1A"} };

    @Test
    @DisabledIfHeadless
    public void testInitComponents() {
        // for now, just makes sure there isn't an exception.
        
        JTable table = new JTable(DATA, COLUMNS);
        
        assertNotNull(
            new JTableWithColumnToolTips(table.getModel(),TIPS));
    }

    @Test
    @DisabledIfHeadless
    public void testNoTipIfNotFound() {
        JTable table = new JTable(DATA, COLUMNS);
        JTableWithColumnToolTips tab = new JTableWithColumnToolTips(table.getModel(),TIPS);
        
        JTableHeader th = tab.getTableHeader();
        String tt = th.getToolTipText( new MouseEvent(table, 12345, 0, 0, 1000, 10000, 1, false));
        assertNull(tt);

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
