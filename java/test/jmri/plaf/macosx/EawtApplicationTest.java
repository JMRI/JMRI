package jmri.plaf.macosx;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.util.SystemType;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest {

    @Test
    public void testCtorMacOSX() {
        Assume.assumeTrue(SystemType.isMacOSX());
        assertThat(new EawtApplication()).isNotNull();
    }

    @Test(expected = RuntimeException.class)
    public void testCtorNotMacOSX() {
        Assume.assumeFalse(SystemType.isMacOSX());
        assertThat(new EawtApplication()).isNotNull();
    }

}
