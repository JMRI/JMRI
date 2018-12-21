package jmri.jmrit.operations.trains.excel;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainCustomManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainCustomManifest t = new TrainCustomManifest();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCustomManifestTest.class);

}
