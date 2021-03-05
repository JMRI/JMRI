package jmri.jmrit.beantable.sensor;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<Sensor> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 13;
    }
    
    @Test
    public void testGetColumnNames() {
        assertEquals("Column5 - INVERTCOL",Bundle.getMessage("Inverted"),
            t.getColumnName(SensorTableDataModel.INVERTCOL));
        assertEquals("Column6 - EDITCOL","",
            t.getColumnName(SensorTableDataModel.EDITCOL));
        assertEquals("Column7 - USEGLOBALDELAY",Bundle.getMessage("SensorUseGlobalDebounce"),
            t.getColumnName(SensorTableDataModel.USEGLOBALDELAY));
        assertEquals("Column8 - ACTIVEDELAY",Bundle.getMessage("SensorActiveDebounce"),
            t.getColumnName(SensorTableDataModel.ACTIVEDELAY));
        assertEquals("Column9 - INACTIVEDELAY",Bundle.getMessage("SensorInActiveDebounce"),
            t.getColumnName(SensorTableDataModel.INACTIVEDELAY));
        assertEquals("Column10 - PULLUPCOL",Bundle.getMessage("SensorPullUp"),
            t.getColumnName(SensorTableDataModel.PULLUPCOL));
        assertEquals("Column11 - FORGETCOL",Bundle.getMessage("StateForgetHeader"),
            t.getColumnName(SensorTableDataModel.FORGETCOL));
        assertEquals("Column12 - QUERYCOL",Bundle.getMessage("StateQueryHeader"),
            t.getColumnName(SensorTableDataModel.QUERYCOL));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        t = new SensorTableDataModel();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorTableDataModelTest.class);

}
