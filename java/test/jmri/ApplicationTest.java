package jmri;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the Application class
 *
 * @author Matthew Harris Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2020
 */
public class ApplicationTest {

    @Test
    public void testSetName() {
        // test default
        assertThat(Application  .getApplicationName())
                .withFailMessage("Default Applicaiton name is 'JMRI'")
                .isEqualTo("JMRI");

        // test ability to change
        Throwable thrown = catchThrowable( () -> Application.setApplicationName(null));
        assertThat(thrown)
                .withFailMessage("Cannot set application name to null")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application name cannot be null.");

        thrown = catchThrowable( () -> Application.setApplicationName("JMRI Testing"));
        assertThat(thrown)
                .withFailMessage("Can set application name to string")
                .isNull();
        assertThat(Application.getApplicationName())
                .withFailMessage("Changed Application name is 'JMRI Testing'")
                .isEqualTo("JMRI Testing");

        // test failure on 2nd change
        thrown = catchThrowable( () -> Application.setApplicationName("JMRI Testing 2"));
        assertThat(thrown)
                .withFailMessage("Changed Application name to 'JMRI Testing 2' prevented")
                .isNotNull()
                .isInstanceOf(IllegalAccessException.class)
                .hasMessage("Application name cannot be modified once set.");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetApplication();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetApplication();
        jmri.util.JUnitUtil.tearDown();
    }

}
