package jmri.plaf.macosx;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;

import jmri.util.SystemType;

import org.junit.Assume;
import org.junit.jupiter.api.Test;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest {

    @Test
    public void testCtorMacOSXandJDK8() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeTrue(SystemType.isMacOSX());
        Assume.assumeFalse(Desktop.getDesktop().isSupported(Desktop.Action.valueOf("APP_ABOUT")));
        assertThat(new EawtApplication()).isNotNull();
    }

    @Test
    public void testCtorMacOSXandJDK9plus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeTrue(SystemType.isMacOSX());
        Assume.assumeTrue(Desktop.getDesktop().isSupported(Desktop.Action.valueOf("APP_ABOUT")));
        assertThat(new EawtApplication()).isNotNull();
    }

    @Test
    public void testCtorNotMacOSX() {
        Assume.assumeFalse(SystemType.isMacOSX());
        assertThat(new EawtApplication()).isNotNull();
    }

}
