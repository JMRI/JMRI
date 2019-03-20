package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RestoreFilesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RestoreFilesAction t = new RestoreFilesAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RestoreFilesActionTest.class);

}
