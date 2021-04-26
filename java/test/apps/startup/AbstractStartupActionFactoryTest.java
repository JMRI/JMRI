package apps.startup;

import java.util.Locale;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2019
 */
public class AbstractStartupActionFactoryTest {

    @Test
    public void testCTor() {
        @SuppressWarnings("deprecation")
        AbstractStartupActionFactory t = new AbstractStartupActionFactory() {
            @Override
            public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
                return "";
            }

            @Override
            public Class<?>[] getActionClasses() {
                return new Class[]{};
            }
        };
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
