package jmri.util.table;

import java.awt.Color;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of ProgressCellRender
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class ProgressCellRenderTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        assertNotNull(t);
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testValues() {

        JTable table = new JTable(new Object[][]{}, new String[]{});
        assertNotNull(t);
        assertEquals("0%",(( JProgressBar )
            t.getTableCellRendererComponent(table, 0f, true, true, 0, 0)).getString());

        assertEquals("12%",(( JProgressBar )
            t.getTableCellRendererComponent(table, 0.117f, false, false, 0, 0)).getString());

        assertEquals("99%",(( JProgressBar )
            t.getTableCellRendererComponent(table, 0.999f, false, false, 1, 0)).getString());

        assertEquals("100%",(( JProgressBar )
            t.getTableCellRendererComponent(table, 1f, false, false, 0, 0)).getString());

        assertEquals("0%",(( JProgressBar )
            t.getTableCellRendererComponent(table, "", false, false, 0, 0)).getString());
        
        
    }
    
    private ProgressCellRender t = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new ProgressCellRender(new Color(0xf5,0xf5,0xf5));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
