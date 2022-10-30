package jmri.jmrix.ipocs.configurexml;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

import jmri.jmrix.ipocs.IpocsConnectionConfig;
import jmri.jmrix.ipocs.IpocsPortController;
import jmri.util.JUnitUtil;

public class IpocsConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new IpocsConnectionConfigXml();
        
        IpocsPortController portController = mock(IpocsPortController.class);
        when(portController.getOptions()).thenReturn(new String[]{});
        when(portController.getDisabled()).thenReturn(true);
        cc = mock(IpocsConnectionConfig.class);
        when(cc.getAdapter()).thenReturn(portController);

    }

    @AfterEach
    @Override
    public void tearDown() {
        cc.dispose();
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void specificLoadTest() {
        assertThrows(UnsupportedOperationException.class, () -> xmlAdapter.load(null));
        assertThrows(UnsupportedOperationException.class, () -> xmlAdapter.load(new Element("connection")));
    }
}
