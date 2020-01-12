package jmri.jmrit.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsPanel t = new OperationsPanel();
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsPanelTest.class);

}
