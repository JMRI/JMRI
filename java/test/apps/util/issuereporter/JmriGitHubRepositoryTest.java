package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Minimal test skeleton for JmriGitHubRepository class
 */
public class JmriGitHubRepositoryTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JmriGitHubRepository constructor", new JmriGitHubRepository());
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

