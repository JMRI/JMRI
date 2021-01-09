package jmri.jmrit.operations.rollingstock.cars;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CarTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Car t = new Car();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testLoadNames() {
        Car car = new Car();
        car.setLoadName("the load");
        car.setReturnWhenEmptyLoadName("Return empty load");
        car.setReturnWhenLoadedLoadName("Return loaded load");

        Assert.assertEquals("confirm load name", "the load", car.getLoadName());
        Assert.assertEquals("confirm RWE load name", "Return empty load", car.getReturnWhenEmptyLoadName());
        Assert.assertEquals("confirm RWL load name", "Return loaded load", car.getReturnWhenLoadedLoadName());
    }

}
