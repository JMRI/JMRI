package jmri.jmrit.beantable.sensor;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
