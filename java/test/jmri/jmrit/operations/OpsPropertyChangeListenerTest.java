package jmri.jmrit.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OpsPropertyChangeListenerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OpsPropertyChangeListener t = new OpsPropertyChangeListener();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OpsPropertyChangeListenerTest.class);

}
