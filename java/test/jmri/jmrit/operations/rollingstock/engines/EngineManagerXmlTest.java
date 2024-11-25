package jmri.jmrit.operations.rollingstock.engines;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EngineManagerXmlTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EngineManagerXml t = new EngineManagerXml();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testXmlLoadDuplicates() {
        JUnitOperationsUtil.initOperationsData();
        // load a second time to create duplicates
        JUnitOperationsUtil.initOperationsData();
        // four duplicate messages
        jmri.util.JUnitAppender
                .assertErrorMessage("Duplicate rolling stock id: (PC5016)");
        jmri.util.JUnitAppender
                .assertErrorMessage("Duplicate rolling stock id: (PC5019)");
        jmri.util.JUnitAppender
                .assertErrorMessage("Duplicate rolling stock id: (PC5524)");
        jmri.util.JUnitAppender
                .assertErrorMessage("Duplicate rolling stock id: (PC5559)");
    }

}
