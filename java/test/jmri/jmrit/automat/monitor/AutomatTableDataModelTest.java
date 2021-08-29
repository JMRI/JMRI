package jmri.jmrit.automat.monitor;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutomatTableDataModelTest {

    @Test
    public void testCTor() {
        AutomatTableDataModel t = new AutomatTableDataModel();
        Assert.assertNotNull("exists",t);
        
        // table is empty, but we can ask about row 0 in some cases anyway
        
        Assert.assertEquals(t.getColumnClass(AutomatTableDataModel.TURNSCOL), Integer.class);
        Assert.assertEquals(t.isCellEditable(0, AutomatTableDataModel.TURNSCOL), false);
        t.setValueAt(0, 0, AutomatTableDataModel.TURNSCOL);
        
        Assert.assertEquals(t.getColumnClass(AutomatTableDataModel.KILLCOL), String.class);
        Assert.assertEquals(t.isCellEditable(0, AutomatTableDataModel.KILLCOL), true);
//        Must have an automation to read kill text DAB 8/4/2021
//        Assert.assertEquals(t.getValueAt(0, AutomatTableDataModel.KILLCOL), 
//                ResourceBundle.getBundle("jmri.jmrit.automat.monitor.AutomatTableBundle").getString("ButtonKill"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AutomatTableDataModelTest.class);

}
