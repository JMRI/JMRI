package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ShowCarsByLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ShowCarsByLocationAction t = new ShowCarsByLocationAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowCarsByLocationActionTest.class);

}
