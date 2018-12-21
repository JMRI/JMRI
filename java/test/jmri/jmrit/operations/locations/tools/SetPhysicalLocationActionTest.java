package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SetPhysicalLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        SetPhysicalLocationAction t = new SetPhysicalLocationAction("Test",l);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationActionTest.class);

}
