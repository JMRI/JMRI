package jmri.util.problemreport;

import java.io.File;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class LogProblemReportProviderTest {

    /**
     * Test that all providers adhere to the stated contract.
     *
     * @param provider the provider to test
     */
    @ParameterizedTest
    @MethodSource(value = "getProviders")
    public void testGetFiles(LogProblemReportProvider provider) {
        assertThat(provider.getFiles()).isNotNull();
    }

    public static Stream<LogProblemReportProvider> getProviders() {
        return StreamSupport.stream(ServiceLoader.load(LogProblemReportProvider.class).spliterator(), false);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Simple test implementation should there be no implements in the JMRI
     * sources.
     */
    @ServiceProvider(service = LogProblemReportProvider.class)
    public static class LogProblemReportProviderTestImpl implements LogProblemReportProvider {

        @Override
        public File[] getFiles() {
            return new File[]{};
        }

    }
}
