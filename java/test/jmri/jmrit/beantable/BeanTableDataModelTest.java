package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.jmrit.beantable.sensor.SensorTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Tests for BeanTableDataModel.
 * Uses SensorTableDataModel as base implementation.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class BeanTableDataModelTest extends AbstractBeanTableDataModelBase<Sensor> {
    
    // protected BeanTableDataModel<B> t; inherited from Abstract
    
    @BeforeEach
    @Override
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
                
        Manager<Sensor>  mgr  =   InstanceManager.sensorManagerInstance();
        t = new BeanTableDataModelImpl(mgr);
        
    }
    
    @AfterEach
    @Override
    public void tearDown(){
    
        if (t!=null){
            t.dispose();
        }
        t=null;
        JUnitUtil.tearDown();
    }
    
    @Override
    public int getModelColumnCount(){
        return 13;
    }

    // An implementation of BeanTableModel which can be used in testing.
    private class BeanTableDataModelImpl extends SensorTableDataModel {

        public BeanTableDataModelImpl(Manager<Sensor> mgr){
            super(mgr);
        }
        
    }
    
    // private final static Logger log = LoggerFactory.getLogger(BeanTableDataModelTest.class);
    
}
