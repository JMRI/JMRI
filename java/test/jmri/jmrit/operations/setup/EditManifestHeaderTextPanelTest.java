package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditManifestHeaderTextPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EditManifestHeaderTextPanel t = new EditManifestHeaderTextPanel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextPanelTest.class);

}
