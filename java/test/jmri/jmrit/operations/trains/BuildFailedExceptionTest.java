package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for BuildFailedException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class BuildFailedExceptionTest extends OperationsTestCase {

    @Test
    public void StringTypeConstructorTest() {
        Assert.assertNotNull("BuildFailedException constructor", new BuildFailedException("test exception", "normal"));
    }

    @Test
    public void StringConstructorTest() {
        Assert.assertNotNull("BuildFailedException string constructor", new BuildFailedException("test exception"));
    }
}
