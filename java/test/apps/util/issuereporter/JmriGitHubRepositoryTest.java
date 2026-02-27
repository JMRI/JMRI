package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for JmriGitHubRepository class
 */
public class JmriGitHubRepositoryTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new JmriGitHubRepository(),
            "JmriGitHubRepository constructor");
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

