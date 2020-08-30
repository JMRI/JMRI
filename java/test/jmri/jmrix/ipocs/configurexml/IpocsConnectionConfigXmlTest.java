package jmri.jmrix.ipocs.configurexml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.ipocs.IpocsConnectionConfig;

public class IpocsConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

  @BeforeEach
  @Override
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
    jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    xmlAdapter = new IpocsConnectionConfigXml();
    cc = new IpocsConnectionConfig();
  }

  @AfterEach
  @Override
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
    xmlAdapter = null;
    cc = null;
  }
}
