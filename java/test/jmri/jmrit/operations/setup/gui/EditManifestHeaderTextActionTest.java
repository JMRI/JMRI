package jmri.jmrit.operations.setup.gui;

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

    // private static final Logger log = LoggerFactory.getLogger(EditManifestHeaderTextActionTest.class);

}
