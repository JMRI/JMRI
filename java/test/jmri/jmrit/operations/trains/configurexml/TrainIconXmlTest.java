package jmri.jmrit.operations.trains.configurexml;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * TrainIconXmlTest.java
 *
 * Description: tests for the TrainIconXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrainIconXmlTest extends OperationsTestCase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("TrainIconXml constructor", new TrainIconXml());
    }
}
