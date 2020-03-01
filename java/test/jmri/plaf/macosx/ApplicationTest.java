package jmri.plaf.macosx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import jmri.util.SystemType;
import org.junit.Test;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ApplicationTest {

    @Test
    public void testGetApplicationOnMacOSX() {
        assumeThat(SystemType.isMacOSX()).isTrue();
        assertThat(Application.getApplication()).isNotNull();
    }

    @Test
    public void testGetApplicationNotOnMacOSX() {
        assumeThat(SystemType.isMacOSX()).isFalse();
        assertThat(Application.getApplication()).isNull();
    }
}
