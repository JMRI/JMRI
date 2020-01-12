package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AutomationTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        AutomationTableModel t = new AutomationTableModel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(AutomationTableModelTest.class);

}
