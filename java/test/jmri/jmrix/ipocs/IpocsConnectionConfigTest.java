package jmri.jmrix.ipocs;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import javax.swing.JOptionPane;

import org.junit.Test;
import org.mockito.MockedStatic;

public class IpocsConnectionConfigTest {
  @Test
  public void constructorTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    assertNotNull(new IpocsConnectionConfig());
    assertNotNull(new IpocsConnectionConfig(portController));
  }

  @Test
  public void nameTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals("IPOCS Connection", cc.name());
  }

  @Test
  public void setInstanceTest() {
    IpocsConnectionConfig cc = new IpocsConnectionConfig();
    cc.setInstance();
  }

  @Test
  public void getInfoTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals(null, cc.getInfo());
  }

  @Test
  public void getAdapter() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals(portController, cc.getAdapter());
  }

  @Test
  public void checkInitDoneTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.checkInitDone();
  }

  @Test
  public void updateAdapterTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.updateAdapter();
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    when(portController.getSystemConnectionMemo()).thenReturn(memo);
    when(memo.setSystemPrefix(any())).thenReturn(true);
    cc.updateAdapter();
    when(memo.setSystemPrefix(any())).thenReturn(false);
    cc.updateAdapter();
  }

  @Test
  public void loadDetailsTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.loadDetails(new javax.swing.JPanel());
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    when(portController.getSystemConnectionMemo()).thenReturn(memo);
    when(memo.setSystemPrefix(any())).thenReturn(true);
    cc.loadDetails(new javax.swing.JPanel());
    when(memo.setSystemPrefix(any())).thenReturn(false);
    cc.loadDetails(new javax.swing.JPanel());
    // Test action listeners
    try (MockedStatic<JOptionPane> theMock = mockStatic(JOptionPane.class)) {
      cc.new SystemPrefixFieldActionListener().actionPerformed(null);
      cc.new SystemPrefixFieldFocusListener().focusLost(null);
      cc.new SystemPrefixFieldFocusListener().focusGained(null);
      cc.new ConnectionNameFieldActionListener().actionPerformed(null);
      cc.new ConnectionNameFieldFocusListener().focusLost(null);
      cc.new ConnectionNameFieldFocusListener().focusGained(null);
      cc.new PortFieldActionListener().actionPerformed(null);
      cc.new PortFieldFocusListener().focusLost(null);
      cc.new PortFieldFocusListener().focusGained(null);
    }
  }

  @Test
  public void showAdvancedItemsTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.showAdvancedItems();
  }

  @Test
  public void getManufacturerTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals(IpocsConnectionTypeList.IPOCSMR, cc.getManufacturer());
  }

  @Test
  public void setManufacturerTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.setManufacturer("");
    jmri.util.JUnitAppender.suppressErrorMessage("Tried to change manufacturer to ");
  }

  @Test
  public void getConnectionNameTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals("IPOCSMR", cc.getConnectionName());
  }

  @Test
  public void disabledTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.setDisabled(true);
    assertEquals(true, cc.getDisabled());
  }
}
