package jmri.jmrit.operations.rollingstock.engines.gui;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EnginesTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EnginesTableModel t = new EnginesTableModel(true, null, null);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(EnginesTableModelTest.class);

}
