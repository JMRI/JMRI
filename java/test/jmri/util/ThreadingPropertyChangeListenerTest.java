package jmri.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class ThreadingPropertyChangeListenerTest {

    @Test
    public void testConstructor() {
        assertThat(new ThreadingPropertyChangeListener(e -> {}, ThreadingPropertyChangeListener.Thread.GUI)).isNotNull();
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
