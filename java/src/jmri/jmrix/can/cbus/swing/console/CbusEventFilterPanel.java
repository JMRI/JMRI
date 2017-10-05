package jmri.jmrix.can.cbus.swing.console;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.jmrix.can.cbus.CbusConstants;

/**
 *
 * @author Andrew Crosland
 */
public class CbusEventFilterPanel extends JPanel {

    protected JCheckBox nnEnButton = new JCheckBox();
    protected JTextField nnLowField = new JTextField("", 5);
    protected JTextField nnHighField = new JTextField("", 5);
    // Fields to enter Event range
    protected JCheckBox evEnButton = new JCheckBox();
    protected JTextField evLowField = new JTextField("", 5);
    protected JTextField evHighField = new JTextField("", 5);
    // Buttons to select event type
    protected JRadioButton onButton = new JRadioButton();
    protected JRadioButton offButton = new JRadioButton();
    protected JRadioButton eitherButton = new JRadioButton();
    protected ButtonGroup eventGroup = new ButtonGroup();
    // Buttons to enable/disable filters
    protected JButton enableButton = new JButton();
    protected JButton disableButton = new JButton();

    /**
     * Create a new instance of CbusEventFilterPanel.
     */
    public CbusEventFilterPanel(CbusEventFilterFrame filterFrame, int index) {
        super();
        _index = index;
        _filterFrame = filterFrame;
    }

    protected CbusEventFilterPanel() {
        super();
    }

    public void initComponents(int index) {
        // Panels will be added across
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Pane to hold node number
        JPanel nnPane = new JPanel();
        nnPane.setLayout(new BoxLayout(nnPane, BoxLayout.X_AXIS));
        nnPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("NodeNumberTitle")));

        // define contents
        nnEnButton.setText(Bundle.getMessage("ButtonEnable"));
        nnEnButton.setVisible(true);
        nnEnButton.setSelected(true);
        nnEnButton.setToolTipText(Bundle.getMessage("EnableNodeTooltip"));
        nnPane.add(nnEnButton);

        nnLowField.setToolTipText(Bundle.getMessage("NnLowfieldTooltip"));
        nnPane.add(nnLowField);

        this.add(nnPane);

        // Pane to hold Event
        JPanel evPane = new JPanel();
        evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
        evPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("EventCol")));

        // define contents
        evEnButton.setText(Bundle.getMessage("ButtonEnable"));
        evEnButton.setVisible(true);
        evEnButton.setToolTipText(Bundle.getMessage("EnableEventTooltip"));
        evPane.add(evEnButton);

        evLowField.setToolTipText(Bundle.getMessage("EvLowfieldTooltip"));
        evPane.add(evLowField);

        this.add(evPane);

        // Pane to hold event type
        JPanel eventPane = new JPanel();
        eventPane.setLayout(new BoxLayout(eventPane, BoxLayout.X_AXIS));
        eventPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("EventTypeTitle")));

        // define contents
        onButton.setText(Bundle.getMessage("InitialStateOn"));
        onButton.setVisible(true);
        onButton.setToolTipText(Bundle.getMessage("OnEventsTooltip"));
        eventPane.add(onButton);

        offButton.setText(Bundle.getMessage("InitialStateOff"));
        offButton.setVisible(true);
        offButton.setToolTipText(Bundle.getMessage("OffEventsTooltip"));
        eventPane.add(offButton);

        eitherButton.setText(Bundle.getMessage("ButtonEither"));
        eitherButton.setVisible(true);
        eitherButton.setSelected(true);
        eitherButton.setToolTipText(Bundle.getMessage("AllEventsTooltip"));
        eventPane.add(eitherButton);

        // Add to group to make one-hot
        eventGroup.add(onButton);
        eventGroup.add(offButton);
        eventGroup.add(eitherButton);

        this.add(eventPane);

        enableButton.setText(Bundle.getMessage("ButtonApply"));
        enableButton.setVisible(true);
        enableButton.setToolTipText(Bundle.getMessage("TooltipApply"));
        this.add(enableButton);

        disableButton.setText(Bundle.getMessage("ButtonDisable"));
        disableButton.setVisible(true);
        disableButton.setToolTipText(Bundle.getMessage("TooltipDisable"));
        this.add(disableButton);

        // connect actions to buttons
        enableButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enableButtonActionPerformed(e);
            }
        });

        // connect actions to buttons
        disableButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                disableButtonActionPerformed(e);
            }
        });
    }

    public void enableButtonActionPerformed(java.awt.event.ActionEvent e) {
        int nn = 0;
        int ev = 0;
        int ty = CbusConstants.EVENT_EITHER;

        if (nnEnButton.isSelected()) {
            try {
                nn = Integer.parseInt(nnLowField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SendEventNodeErrorDialog"),
                        Bundle.getMessage("EventFilterTitle"), JOptionPane.ERROR_MESSAGE);
                nn = 0;
                return;
            }
            if ((nn > 65535) || (nn < 0)) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SendEventNodeErrorDialog"),
                        Bundle.getMessage("EventFilterTitle"), JOptionPane.ERROR_MESSAGE);
                nn = 0;
                return;
            }
        }

        if (evEnButton.isSelected()) {
            try {
                ev = Integer.parseInt(evLowField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SendEventInvalidDialog"),
                        Bundle.getMessage("EventFilterTitle"), JOptionPane.ERROR_MESSAGE);
                ev = 0;
                return;
            }
            if ((ev > 65535) || (ev < 0)) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SendEventInvalidDialog"),
                        Bundle.getMessage("EventFilterTitle"), JOptionPane.ERROR_MESSAGE);
                ev = 0;
                return;
            }
        }

        if (onButton.isSelected()) {
            ty = CbusConstants.EVENT_ON;
        }
        if (offButton.isSelected()) {
            ty = CbusConstants.EVENT_OFF;
        }

        _filterFrame.enable(_index, nn, nnEnButton.isSelected(), ev, evEnButton.isSelected(), ty);
    }

    public void disableButtonActionPerformed(java.awt.event.ActionEvent e) {
        _filterFrame.disable(_index);
    }

    private int _index;
    private CbusEventFilterFrame _filterFrame;

}
