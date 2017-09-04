package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ShowTrainsServingLocationActionTest {

    @Test
    public void testCTor() {
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track tr = new Track("Test id", "Test Name", "Test Type", l);
        ShowTrainsServingLocationAction t = new ShowTrainsServingLocationAction("Test Action",l,tr);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationActionTest.class);

}
