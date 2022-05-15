package jmri.implementation.decorators;

import jmri.Reporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Test for the jmri.implmentaiton.decorators.TimeoutReporter class
 *
 * @author Paul Bender Copyright (C) 2022
 */
import static org.assertj.core.api.Assertions.assertThat;

class TimeoutReporterTest {

    @Test
    void testEqualsAndHashCode() {
        Reporter baseReporter1 = Mockito.mock(Reporter.class);
        Mockito.when(baseReporter1.getSystemName()).thenReturn("foo");
        Reporter baseReporter2 = Mockito.mock(Reporter.class);
        Mockito.when(baseReporter2.getSystemName()).thenReturn("bar");
        TimeoutReporter tr1 = new TimeoutReporter(baseReporter1);
        TimeoutReporter tr2 = new TimeoutReporter(baseReporter1);
        TimeoutReporter tr3 = new TimeoutReporter(baseReporter2);

        assertThat(tr1).isEqualTo(tr2).isNotEqualTo(tr3);
        assertThat(tr1).hasSameHashCodeAs(tr2);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}

