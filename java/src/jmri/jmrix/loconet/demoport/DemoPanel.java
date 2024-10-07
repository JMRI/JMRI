package jmri.jmrix.loconet.demoport;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.*;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.JmriPanel;

/**
 * Demo panel.
 *
 * @author Daniel Bergqvist (C) 2024
 */
class DemoPanel extends JmriPanel {

    private final Map<SystemConnectionMemo, AbstractSerialPortController> memoMap = new HashMap<>();
    private DemoSerialPort _demoSerialPort;
    private LocoNetConnection _connection;
    private JComboBox<LocoNetConnection> _locoNetConnection;
    private JTextArea _textArea;
    private boolean turnoutThrown1;
    private boolean turnoutThrown2;

    /** {@inheritDoc} */
    @Override
    public void initComponents() {

        super.initComponents();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        add(new JLabel(Bundle.getMessage("DemoPanel_Connection")), c);

        for (ConnectionConfig cc : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (cc.getAdapter() instanceof AbstractSerialPortController) {
                AbstractSerialPortController pc = (AbstractSerialPortController) cc.getAdapter();
                if (pc.isPortOpen()) {
                    memoMap.put(cc.getAdapter().getSystemConnectionMemo(), pc);
                }
            }
        }

        _locoNetConnection = new JComboBox<>();
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        add(_locoNetConnection, c);

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        JButton buttonStartTest = new JButton(Bundle.getMessage("DemoPanel_ButtonStartTest"));
        buttonStartTest.setEnabled(false);
        add(buttonStartTest, c);

        c.gridy = 2;
        JButton buttonThrowTurnout1 = new JButton(Bundle.getMessage("DemoPanel_ButtonThrowTurnout1"));
        buttonThrowTurnout1.addActionListener((e)->{
            _demoSerialPort.throwTurnout(1, turnoutThrown1);
            turnoutThrown1 = !turnoutThrown1;
        });
        buttonThrowTurnout1.setEnabled(false);
        add(buttonThrowTurnout1, c);

        c.gridy = 3;
        JButton buttonThrowTurnout2 = new JButton(Bundle.getMessage("DemoPanel_ButtonThrowTurnout2"));
        buttonThrowTurnout2.addActionListener((e)->{
            _demoSerialPort.throwTurnout(2, turnoutThrown2);
            turnoutThrown2 = !turnoutThrown2;
        });
        buttonThrowTurnout2.setEnabled(false);
        add(buttonThrowTurnout2, c);

        c.gridy = 4;
        add(new JLabel(Bundle.getMessage("DemoPanel_LocoNetMonitor")), c);

        c.gridy = 5;
        _textArea = new JTextArea();
        _textArea.setColumns(50);
        _textArea.setRows(30);
        add(_textArea, c);

        _locoNetConnection.addActionListener((e)->{
            _connection = _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex());
            buttonStartTest.setEnabled(true);
        });
        List<LocoNetSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        for (LocoNetSystemConnectionMemo memo : systemConnections) {
            if (memoMap.get(memo) != null) {
                LocoNetConnection conn = new LocoNetConnection(memo);
                _locoNetConnection.addItem(conn);
            }
        }

        buttonStartTest.addActionListener((e)->{
            _demoSerialPort = new DemoSerialPort(this, _connection._memo);
            _demoSerialPort.startDemo();
            _locoNetConnection.setEnabled(false);
            buttonStartTest.setEnabled(false);
            buttonThrowTurnout1.setEnabled(true);
            buttonThrowTurnout2.setEnabled(true);
        });
    }

    public AbstractSerialPortController getPortController() {
        if (_connection == null) {
            return null;
        }
        return memoMap.get(_connection._memo);
    }

    public void addMessage(String msg) {
        _textArea.append(msg);
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return "Demo panel"; // this should come from a Bundle in your package
    }

    /** {@inheritDoc} */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        return menuList;
    }


    private static class LocoNetConnection {

        private final LocoNetSystemConnectionMemo _memo;

        public LocoNetConnection(LocoNetSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }

}
