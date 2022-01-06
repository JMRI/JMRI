package jmri.jmrit.operations.locations.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author J. Scott Walton Copyright (C) 2022
 */
public class ImportLocationsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportLocations t = new ImportLocations();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testImportToEmpty() {

    }

    @Test
    public void testImportToPartial() {

    }
}
