package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LoadDemoActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        LoadDemoAction t = new LoadDemoAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(LoadDemoActionTest.class);

}
