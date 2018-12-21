package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ShowTrainsServingLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track tr = new Track("Test id", "Test Name", "Test Type", l);
        ShowTrainsServingLocationAction t = new ShowTrainsServingLocationAction("Test Action",l,tr);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationActionTest.class);

}
