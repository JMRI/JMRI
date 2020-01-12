package jmri.jmrit.operations;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CommonConductorYardmasterPanel class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CommonConductorYardmasterPanelTest extends OperationsTestCase {

    protected CommonConductorYardmasterPanel p = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", p);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        p = new CommonConductorYardmasterPanel() {
            @Override
            public void update() {
            }
        };
    }

}
