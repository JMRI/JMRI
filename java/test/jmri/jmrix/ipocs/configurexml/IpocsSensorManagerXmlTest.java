package jmri.jmrix.ipocs.configurexml;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class IpocsSensorManagerXmlTest {

  @Test
  public void testCtor() {
    Assert.assertNotNull("IpocsSensorManagerXml constructor", new IpocsSensorManagerXml());
  }

  @BeforeEach
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
  }

  @AfterEach
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
  }
}
