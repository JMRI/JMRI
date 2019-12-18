package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 *
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class EngineLengthsTest extends OperationsTestCase {

    // test EngineLengths Class
    // test EngineLengths creation
    @Test
    public void testEngineLengthsCreate() {
        EngineLengths el1 = new EngineLengths();
        Assert.assertNotNull("exists", el1);
    }

    // test EngineLengths public constants
    @Test
    public void testEngineLengthsConstants() {
        EngineLengths el1 = new EngineLengths();

        Assert.assertNotNull("exists", el1);
        Assert.assertEquals("EngineTypes ENGINELENGTHS_CHANGED_PROPERTY", "EngineLengths", EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY);
    }

    // test EngineLengths Names
    @Test
    public void testEngineLengthsNames() {
        EngineLengths el1 = new EngineLengths();

        Assert.assertEquals("EngineLengths Null Names", false, el1.containsName("TESTENGINELENGTHNAME1"));

        el1.addName("TESTENGINELENGTHNAME1");
        Assert.assertEquals("EngineLengths add Name1", true, el1.containsName("TESTENGINELENGTHNAME1"));

        el1.addName("TESTENGINELENGTHNAME2");
        Assert.assertEquals("EngineLengths add Name2", true, el1.containsName("TESTENGINELENGTHNAME2"));

        el1.deleteName("TESTENGINELENGTHNAME2");
        Assert.assertEquals("EngineLengths delete Name2", false, el1.containsName("TESTENGINELENGTHNAME2"));

        el1.deleteName("TESTENGINELENGTHNAME1");
        Assert.assertEquals("EngineLengths delete Name1", false, el1.containsName("TESTENGINELENGTHNAME1"));
    }

    // TODO: Add test for import
}
