package jmri.jmrit.operations;

import java.util.Locale;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2024
 */
public class OperationsStartupActionFactoryTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsStartupActionFactory t = new OperationsStartupActionFactory();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testNumberOfStartUpClasses() {
        OperationsStartupActionFactory t = new OperationsStartupActionFactory();
        Class<?>[] classes = t.getActionClasses();
        Assert.assertEquals("number of classes available for startup", 8, classes.length);
    }

    @Test
    public void testStartUpTitles() {
        OperationsStartupActionFactory t = new OperationsStartupActionFactory();
        Class<?>[] classes = t.getActionClasses();
        for (Class<?> c : classes) {
            String title = t.getTitle(c, Locale.getDefault());
            Assert.assertFalse(title.isBlank());
        }
    }
}
