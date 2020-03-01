package jmri.plaf.macosx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest {

    @Test
    public void testCtor() {
        assertThat(new EawtApplication()).isNotNull();
    }

}
