package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditManifestHeaderTextActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EditManifestHeaderTextAction t = new EditManifestHeaderTextAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextActionTest.class);

}
