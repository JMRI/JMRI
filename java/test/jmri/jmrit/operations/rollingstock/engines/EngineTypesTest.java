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
public class EngineTypesTest extends OperationsTestCase {

    // test EngineTypes Class
    // test EngineTypes creation
    @Test
    public void testEngineTypesCreate() {
        EngineTypes et1 = new EngineTypes();
        Assert.assertNotNull("exists", et1);
    }

    // test EngineTypes public constants
    @Test
    public void testEngineTypesConstants() {
        EngineTypes et1 = new EngineTypes();

        Assert.assertNotNull("exists", et1);
        Assert.assertEquals("EngineTypes ENGINETYPES_LENGTH_CHANGED_PROPERTY", "EngineTypesLength", EngineTypes.ENGINETYPES_CHANGED_PROPERTY);
        Assert.assertEquals("EngineTypes ENGINETYPES_NAME_CHANGED_PROPERTY", "EngineTypesName", EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY);
    }

    // test EngineTypes Names
    @Test
    public void testEngineTypesNames() {
        EngineTypes et1 = new EngineTypes();

        Assert.assertEquals("EngineTypes Null Names", false, et1.containsName("TESTENGINETYPENAME1"));

        et1.addName("TESTENGINETYPENAME1");
        Assert.assertEquals("EngineTypes add Name1", true, et1.containsName("TESTENGINETYPENAME1"));

        et1.addName("TESTENGINETYPENAME2");
        Assert.assertEquals("EngineTypes add Name2", true, et1.containsName("TESTENGINETYPENAME2"));

        et1.replaceName("TESTENGINETYPENAME1", "TESTENGINETYPENAME3");
        Assert.assertEquals("EngineTypes replace Name1", false, et1.containsName("TESTENGINETYPENAME1"));
        Assert.assertEquals("EngineTypes replace Name3", true, et1.containsName("TESTENGINETYPENAME3"));

        et1.deleteName("TESTENGINETYPENAME2");
        Assert.assertEquals("EngineTypes delete Name2", false, et1.containsName("TESTENGINETYPENAME2"));

        et1.deleteName("TESTENGINETYPENAME3");
        Assert.assertEquals("EngineTypes delete Name3", false, et1.containsName("TESTENGINETYPENAME3"));
    }
}
