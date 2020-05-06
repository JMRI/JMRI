package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.*;


/**
 * test class for AbstractSensor 
 *
 * @author Bob Jacobsen 2016 from AbstractLightTestBase (which was called AbstractLightTest at the time)
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

    @Test
    public void checkToString() {
        Sensor nb = new AbstractSensor("Foo", "Bar"){
            @Override
            public void requestUpdateFromLayout(){} // for abstract class
            
            @Override
            public String toStringSuffix(){ return " After";} // feature under test
        };

        Assert.assertEquals(nb.toString(), "Foo After");
    }
    
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
