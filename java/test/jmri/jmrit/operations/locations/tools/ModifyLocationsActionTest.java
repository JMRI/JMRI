package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ModifyLocationsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ModifyLocationsAction t = new ModifyLocationsAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ModifyLocationsActionTest.class);

}
