package jmri.jmrit.operations.locations.divisions;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class DivisionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        Assert.assertEquals("name", "testName", division.getName());
        Assert.assertEquals("id", "testId", division.getId());
    }
}
