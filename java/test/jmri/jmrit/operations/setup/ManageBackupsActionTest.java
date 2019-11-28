package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ManageBackupsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ManageBackupsAction t = new ManageBackupsAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ManageBackupsActionTest.class);

}
