package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


/**
 * test class for AbstractSensor 
 *
 * @author	Bob Jacobsen 2016 from AbstractLightTestBase (which was called AbstractLightTest at the time)
 * @author      Paul Bender Copyright (C) 2018
*/
public class AbstractSensorTest extends AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    // load t with actual object; create scaffolds as needed
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        t = new AbstractSensor("Foo", "Bar"){
            @Override
                public void requestUpdateFromLayout(){}
        };
    }

    @Override
    @After
    public void tearDown() {
	    t.dispose();
	    t = null;
        JUnitUtil.tearDown();
    }

}
