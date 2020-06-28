package jmri.plaf.macosx;

import java.awt.GraphicsEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;
import static org.junit.jupiter.api.condition.JRE.*;
import static org.junit.jupiter.api.condition.OS.MAC;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest {

    @Test
    @EnabledOnOs(MAC)
    @EnabledOnJre(JAVA_8)
    public void testCtorMacOSXandJDK8() {
        assumeThat(GraphicsEnvironment.isHeadless()).isFalse();
        assertThat(new EawtApplication()).isNotNull();
    }

    @Test
    @EnabledOnOs(MAC)
    @EnabledForJreRange(min = JAVA_9)
    public void testCtorMacOSXandJDK9plus() {
        assumeThat(GraphicsEnvironment.isHeadless()).isFalse();
        assertThatCode(() -> new EawtApplication()).isInstanceOf(NoClassDefFoundError.class);
    }

    @Test
    @DisabledOnOs(MAC)
    public void testCtorNotMacOSX() {
        assertThatCode(() -> new EawtApplication()).isInstanceOf(RuntimeException.class);
    }

}
