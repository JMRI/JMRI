package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ModifyLocationsCarLoadsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ModifyLocationsCarLoadsAction t = new ModifyLocationsCarLoadsAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ModifyLocationsCarLoadsActionTest.class);

}
