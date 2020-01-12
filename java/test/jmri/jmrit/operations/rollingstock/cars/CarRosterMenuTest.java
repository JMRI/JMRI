package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CarRosterMenuTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        CarRosterMenu t = new CarRosterMenu("test menu",1,ctf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ctf);
    }

    // private final static Logger log = LoggerFactory.getLogger(CarRosterMenuTest.class);

}
