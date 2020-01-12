package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsTableModel t = new TrainsTableModel();
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableModelTest.class);

}
