package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for BuildFailedException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class BuildFailedExceptionTest extends OperationsTestCase {

    @Test
    public void stringTypeConstructorTest() {
        Assert.assertNotNull("BuildFailedException constructor", new BuildFailedException("test exception", "normal"));
    }

    @Test
    public void stringConstructorTest() {
        Assert.assertNotNull("BuildFailedException string constructor", new BuildFailedException("test exception"));
    }
}
