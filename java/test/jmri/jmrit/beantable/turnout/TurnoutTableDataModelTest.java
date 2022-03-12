package jmri.jmrit.beantable.turnout;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young (C) 2021
 */
public class TurnoutTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<Turnout> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 20;
    }
    
    @Test
    public void testGetColumnNames() {
        assertEquals("Column5 - INVERTCOL",Bundle.getMessage("Inverted"), 
            t.getColumnName(TurnoutTableDataModel.INVERTCOL));
        assertEquals("Column6 - LOCKCOL",Bundle.getMessage("Locked"),
            t.getColumnName(TurnoutTableDataModel.LOCKCOL));
        assertEquals("Column7 - EDITCOL","",
            t.getColumnName(TurnoutTableDataModel.EDITCOL));
        assertEquals("Column8 - KNOWNCOL",Bundle.getMessage("Feedback"),
            t.getColumnName(TurnoutTableDataModel.KNOWNCOL));
        assertEquals("Column9 - MODECOL",Bundle.getMessage("ModeLabel"),
            t.getColumnName(TurnoutTableDataModel.MODECOL));
        assertEquals("Column10 - SENSOR1COL",Bundle.getMessage("BlockSensor") + "1",
            t.getColumnName(TurnoutTableDataModel.SENSOR1COL));
        assertEquals("Column11 - SENSOR2COL",Bundle.getMessage("BlockSensor") + "2",
            t.getColumnName(TurnoutTableDataModel.SENSOR2COL));
        assertEquals("Column12 - OPSONOFFCOL",Bundle.getMessage("TurnoutAutomationMenu"),
            t.getColumnName(TurnoutTableDataModel.OPSONOFFCOL));
        assertEquals("Column13 - OPSEDITCOL","",
            t.getColumnName(TurnoutTableDataModel.OPSEDITCOL));
        assertEquals("Column14 - LOCKOPRCOL",Bundle.getMessage("LockMode"),
            t.getColumnName(TurnoutTableDataModel.LOCKOPRCOL));
        assertEquals("Column15 - LOCKDECCOL",Bundle.getMessage("Decoder"),
            t.getColumnName(TurnoutTableDataModel.LOCKDECCOL));
        assertEquals("Column16 - STRAIGHTCOL",Bundle.getMessage("ClosedSpeed"),
            t.getColumnName(TurnoutTableDataModel.STRAIGHTCOL));
        assertEquals("Column17 - DIVERGCOL",Bundle.getMessage("ThrownSpeed"),
            t.getColumnName(TurnoutTableDataModel.DIVERGCOL));
        assertEquals("Column18 - FORGETCOL",Bundle.getMessage("StateForgetHeader"),
            t.getColumnName(TurnoutTableDataModel.FORGETCOL));
        assertEquals("Column19 - QUERYCOL",Bundle.getMessage("StateQueryHeader"),
            t.getColumnName(TurnoutTableDataModel.QUERYCOL));

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        t = new TurnoutTableDataModel();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutTableDataModelTest.class);

}
