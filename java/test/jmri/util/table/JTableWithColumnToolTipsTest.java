package jmri.util.table;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
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
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        
        JTable table = new JTable(DATA, COLUMNS);
        
        assertThat(
            new JTableWithColumnToolTips(table.getModel(),TIPS))
            .isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testNoTipIfNotFound() {
        JTable table = new JTable(DATA, COLUMNS);
        JTableWithColumnToolTips tab = new JTableWithColumnToolTips(table.getModel(),TIPS);
        
        JTableHeader th = tab.getTableHeader();
        String tt = th.getToolTipText(null);
        assertThat(tt).isNull();
        
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
