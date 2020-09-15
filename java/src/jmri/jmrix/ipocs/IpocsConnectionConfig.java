package jmri.jmrix.ipocs;

import jmri.jmrix.AbstractConnectionConfig;
import jmri.jmrix.PortAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsConnectionConfig extends AbstractConnectionConfig {
  private final static Logger log = LoggerFactory.getLogger(IpocsConnectionConfig.class);
  private IpocsPortController portController;
  private final JTextField portField = new JTextField(6);
  private final JLabel portFieldLabel = new JLabel();

  public IpocsConnectionConfig() {
    super();
    portField.setText("0");
  }

  public IpocsConnectionConfig(IpocsPortController portController) {
    this();
    this.portController = portController;
  }

  @Override
  public String name() {
    return "IPOCS Connection";
  }

  @Override
  protected void setInstance() {
    if (portController == null) {
      portController = new IpocsPortController(new IpocsSystemConnectionMemo());
      portController.configure();
    }
  }

  @Override
  public String getInfo() {
    return getAdapter().getCurrentPortName();
  }

  @Override
  public IpocsPortController getAdapter() {
    return portController;
  }

  @Override
  protected void checkInitDone() {

  }

  @Override
  public void updateAdapter() {
    getAdapter().setPort(Short.parseShort(portField.getText()));

    if (getAdapter().getSystemConnectionMemo() != null &&
        !getAdapter().getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
      systemPrefixField.setValue(getAdapter().getSystemConnectionMemo().getSystemPrefix());
      connectionNameField.setText(getAdapter().getSystemConnectionMemo().getUserName());
    }
  }

  public class SystemPrefixFieldActionListener implements ActionListener {
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (!getAdapter().getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
        JOptionPane.showMessageDialog(null, "Unable to set prefix");
        systemPrefixField.setValue(getAdapter().getSystemConnectionMemo().getSystemPrefix());
      }
    }
  }
  public class SystemPrefixFieldFocusListener implements FocusListener {
    @Override
    public void focusLost(java.awt.event.FocusEvent e) {
      if (!getAdapter().getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
        JOptionPane.showMessageDialog(null, "Unable to set prefix");
        systemPrefixField.setValue(getAdapter().getSystemConnectionMemo().getSystemPrefix());
      }
    }

    @Override
    public void focusGained(java.awt.event.FocusEvent e) {
    }
  }

  public class ConnectionNameFieldActionListener implements ActionListener {
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (!getAdapter().getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
        JOptionPane.showMessageDialog(null, "Unable to set connection name");
        connectionNameField.setText(getAdapter().getSystemConnectionMemo().getUserName());
      }
    }
  }
  public class ConnectionNameFieldFocusListener implements FocusListener {
    @Override
    public void focusLost(java.awt.event.FocusEvent e) {
      if (!getAdapter().getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
        JOptionPane.showMessageDialog(null, "Unable to set connection change");
        connectionNameField.setText(getAdapter().getSystemConnectionMemo().getUserName());
      }
    }

    @Override
    public void focusGained(java.awt.event.FocusEvent e) {
    }
  }

  public class PortFieldActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        getAdapter().setPort(Short.parseShort(portField.getText()));
      } catch (java.lang.NumberFormatException ex) {
          log.warn("Could not parse port attribute");
      }
    }
  }
  public class PortFieldFocusListener implements FocusListener {
    @Override
    public void focusLost(FocusEvent e) {
      try {
        getAdapter().setPort(Short.parseShort(portField.getText()));
      } catch (java.lang.NumberFormatException ex) {
          log.warn("Could not parse port attribute");
      }
    }

    @Override
    public void focusGained(FocusEvent e) {
    }
  }

  @Override
  public void loadDetails(JPanel details) {
    _details = details;
    _details.setLayout(gbLayout);
    setInstance();

    portField.setToolTipText("Port to accept IPOCS connections on");
    portField.setEnabled(true);
    portField.setText(String.valueOf(getAdapter().getPort()));
    portFieldLabel.setText("Listening port: ");

    if (getAdapter().getSystemConnectionMemo() != null) {
      systemPrefixField.setValue(getAdapter().getSystemConnectionMemo().getSystemPrefix());
      connectionNameField.setText(getAdapter().getSystemConnectionMemo().getUserName());
      NUMOPTIONS = NUMOPTIONS + 2;
    }
    systemPrefixField.addActionListener(new SystemPrefixFieldActionListener());
    systemPrefixField.addFocusListener(new SystemPrefixFieldFocusListener());
    connectionNameField.addActionListener(new ConnectionNameFieldActionListener());
    connectionNameField.addFocusListener(new ConnectionNameFieldFocusListener());
    portField.addActionListener(new PortFieldActionListener());
    portField.addFocusListener(new PortFieldFocusListener());

    int i = super.addStandardDetails(portController, false, 1);
    cR.gridy = i;
    cL.gridy = i;
    gbLayout.setConstraints(portFieldLabel, cL);
    gbLayout.setConstraints(portField, cR);
    _details.add(portFieldLabel);
    _details.add(portField);
  }

  @Override
  protected void showAdvancedItems() {
  }

  @Override
  public String getManufacturer() {
    return IpocsConnectionTypeList.IPOCSMR;
  }

  @Override
  public void setManufacturer(String manufacturer) {
    portController.setManufacturer(manufacturer);
  }

  @Override
  public String getConnectionName() {
    return "IPOCSMR";
  }

  private boolean isDisabled = false;

  @Override
  public boolean getDisabled() {
    return isDisabled;
  }

  @Override
  public void setDisabled(boolean disable) {
    this.isDisabled = disable;
  }
}
