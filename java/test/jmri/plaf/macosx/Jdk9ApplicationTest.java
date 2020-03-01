package jmri.plaf.macosx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests for the Jdk9Application class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Jdk9ApplicationTest {

    @Test
    public void testCtor() {
        assertThat(new Jdk9Application()).isNotNull();
    }

}
