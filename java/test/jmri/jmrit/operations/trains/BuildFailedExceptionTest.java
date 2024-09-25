package jmri.jmrit.operations.trains;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * Tests for BuildFailedException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class BuildFailedExceptionTest extends OperationsTestCase {

    @Test
    public void stringTypeConstructorTest() {
        BuildFailedException bfe = new BuildFailedException("test exception", "NoRmAl");
        Assert.assertNotNull("BuildFailedException constructor", bfe);
        Assert.assertEquals("Type of build exception", "NoRmAl", bfe.getExceptionType());
    }

    @Test
    public void stringConstructorTest() {
        BuildFailedException bfe = new BuildFailedException("test exception");
        Assert.assertNotNull("BuildFailedException string constructor", bfe);
        Assert.assertEquals("Type of build exception", "normal", bfe.getExceptionType());
    }

    @Test
    public void exceptionConstructorTest() {
        Assert.assertNotNull("BuildFailedException string constructor", new BuildFailedException(new Exception()));
    }
}
