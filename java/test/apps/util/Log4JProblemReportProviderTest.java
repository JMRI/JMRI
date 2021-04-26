package apps.util;

import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class Log4JProblemReportProviderTest {
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testGetFiles() {
        assertThat(new Log4JProblemReportProvider().getFiles()).isNotNull();
    }
    
}
