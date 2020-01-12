package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoSaveTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        AutoSave t = new AutoSave();
        Assert.assertNotNull("exists", t);
        t.stop();
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoSaveTest.class);

}
