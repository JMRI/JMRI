package jmri.jmrix.loconet.duplexgroup.swing;

import java.awt.Cursor;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.duplexgroup.LnDplxGrpInfoImplConstants;
import jmri.util.swing.ValidatedTextField;

/**
 * Provides a JPanel for querying and configuring Digitrax Duplex and WiFi
 * network identification. Provides useful function if one or more devices
 * are connected to LocoNet.
 * <p>
 * This tool makes use of LocoNet messages which have not been publicly
 * documented by Digitrax. This tool is made possible by the reverse-
 * engineering efforts of B. Milhaupt. Because these messages have been
 * reverse-engineered, it is possible that the tool may not function as desired
 * for some Digitrax hardware, and that future Digitrax hardware may not be
 * compatible with this tool.
 *
 * @author B. Milhaupt Copyright 2010, 2011
 */
public class DuplexGroupInfoPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements java.beans.PropertyChangeListener {

    // member declarations
    JButton swingReadButton;
    JButton swingSetButton;
    JButton swingUpdateGrpName;
    JButton swingUpdateChannel;
    JButton swingUpdatePassword;
    JButton swingUpdateGroupId;
    ValidatedTextField swingNameValueField = new ValidatedTextField(1, false, "a", "b");
    ValidatedTextField swingChannelValueField = new ValidatedTextField(1, false, "a", "b");
    ValidatedTextField swingPasswordValueField = new ValidatedTextField(1, false, "a", "b");
    ValidatedTextField swingIdValueField = new ValidatedTextField(1, false, "a", "b");
    JTable swingDeviceTable=new JTable();
    JTable swingDeviceResponseTable=new JTable();
    ResponsesTableModel dtModel;
    ResponsesTableModel dtrtModel;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.S");
    JLabel swingNumUr92Label;
    JLabel swingStatusValueLabel;
    private int numDuplexTypeDevices;

    private LnDplxGrpInfoImpl duplexGroupImplementation;

    private int minWindowWidth = 0;

    public DuplexGroupInfoPanel() {
        swingNameValueField = new ValidatedTextField(9, false, "^.{1,8}$",  // NOI18N
                "ErrorBadGroupName");

        swingChannelValueField = new ValidatedTextField(3, false, 11, 26, "ErrorBadGroupChannel");
        swingPasswordValueField = new ValidatedTextField(5, true, "^[0-9A-C]{4}$",  // NOI18N
                "ErrorBadGroupPassword");
        swingIdValueField = new ValidatedTextField(3, false, 0, 127, "ErrorBadGroupId");
        swingNameValueField.addPropertyChangeListener(ValidatedTextField.VTF_PC_STAT_LN_UPDATE, this);
        swingChannelValueField.addPropertyChangeListener(ValidatedTextField.VTF_PC_STAT_LN_UPDATE, this);
        swingPasswordValueField.addPropertyChangeListener(ValidatedTextField.VTF_PC_STAT_LN_UPDATE, this);
        swingIdValueField.addPropertyChangeListener(ValidatedTextField.VTF_PC_STAT_LN_UPDATE, this);

        duplexGroupImplementation = null;

    }

    @Override
    public void initComponents() {
        // uses swing operations
        JLabel swingTempLabel;

        try {
            minWindowWidth = Integer.parseInt(Bundle.getMessage("MinimumWidthForWindow"), 10);
        } catch (RuntimeException e) {
            minWindowWidth = DEFAULT_WINDOW_WIDTH;
        }

        numDuplexTypeDevices = 0;        // assume 0 UR92 devices available
        swingStatusValueLabel = new JLabel();
        swingStatusValueLabel.setName("ProcessingInitialStatusMessage");  //this string is used as a reference to a .properties file entry; internationalization is handled there.
        swingStatusValueLabel.setText(convertToHtml(swingStatusValueLabel.getName(), minWindowWidth));

        swingNameValueField.setText(Bundle.getMessage("ValueUnknownGroupName"));
        swingNameValueField.setToolTipText(Bundle.getMessage("ToolTipGroupName"));
        swingNameValueField.setLastQueriedValue(Bundle.getMessage("ValueUnknownGroupName"));
        swingUpdateGrpName = new JButton(Bundle.getMessage("ButtonUpdate"));
        swingUpdateGrpName.setToolTipText(Bundle.getMessage("ButtonUpdateDuplexNameHint"));

        swingChannelValueField.setText(Bundle.getMessage("ValueUnknownGroupChannel"));
        swingChannelValueField.setToolTipText(Bundle.getMessage("ToolTipGroupChannel"));
        swingChannelValueField.setLastQueriedValue(Bundle.getMessage("ValueUnknownGroupChannel"));
        swingUpdateChannel = new JButton(Bundle.getMessage("ButtonUpdate"));
        swingUpdateChannel.setToolTipText(Bundle.getMessage("ButtonUpdateChannelHint"));

        swingPasswordValueField.setText(Bundle.getMessage("ValueUnknownGroupPassword"));
        swingPasswordValueField.setToolTipText(Bundle.getMessage("ToolTipGroupPassword"));
        swingPasswordValueField.setLastQueriedValue(Bundle.getMessage("ValueUnknownGroupPassword"));
        swingUpdatePassword = new JButton(Bundle.getMessage("ButtonUpdate"));
        swingUpdatePassword.setToolTipText(Bundle.getMessage("ButtonDuplexPasswordHint"));

        swingIdValueField.setText(Bundle.getMessage("ValueUnknownGroupID"));
        swingIdValueField.setToolTipText(Bundle.getMessage("ToolTipGroupID"));
        swingIdValueField.setLastQueriedValue(Bundle.getMessage("ValueUnknownGroupID"));
        swingUpdateGroupId = new JButton(Bundle.getMessage("ButtonUpdate"));
        swingUpdateGroupId.setToolTipText(Bundle.getMessage("ButtonDuplexGroupIDHint"));

        // Want to force space in the GUI for N lines of status, where N comes
        // from a value in the DuplexGroup.properties file;
        // assume 2 if not able to parse the variable from the .properties file.
        int numLinesForStatus = 2;
        try {
            numLinesForStatus = Integer.parseInt(Bundle.getMessage("FixedLinesForStatus"));
        } catch (RuntimeException e) {
            numLinesForStatus = 2;
        }

        swingStatusValueLabel.setPreferredSize(new java.awt.Dimension(minWindowWidth,
                numLinesForStatus * (int) swingStatusValueLabel.getMaximumSize().getHeight()));

        swingReadButton = new JButton(Bundle.getMessage("ButtonRead"));
        swingSetButton = new JButton(Bundle.getMessage("ButtonSet"));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // need to override the default layout of FlowLayout (horizontal layout)
        swingReadButton.hasFocus();

        JPanel swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 3));
        swingTempPanel.add(swingTempLabel = new JLabel(Bundle.getMessage("LabelDuplexName")));
        swingTempPanel.add(swingNameValueField);
        swingTempLabel.setLabelFor(swingNameValueField); // for "assistive technology" per JLabel on-line documentation
        swingTempPanel.add(swingUpdateGrpName);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 3));
        swingTempPanel.add(swingTempLabel = new JLabel(Bundle.getMessage("LabelDuplexChannel")));
        swingTempPanel.add(swingChannelValueField);
        swingTempLabel.setLabelFor(swingChannelValueField); // for "assistive technology" per JLabel on-line documentation
        swingTempPanel.add(swingUpdateChannel);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 3));
        swingTempPanel.add(swingTempLabel = new JLabel(Bundle.getMessage("LabelDuplexPassword")));
        swingTempPanel.add(swingPasswordValueField);
        swingTempLabel.setLabelFor(swingPasswordValueField); // for "assistive technology" per JLabel on-line documentation
        swingTempPanel.add(swingUpdatePassword);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 3));
        swingTempPanel.add(swingTempLabel = new JLabel(Bundle.getMessage("LabelDuplexGroupID")));
        swingTempPanel.add(swingIdValueField);
        swingTempLabel.setLabelFor(swingIdValueField); // for "assistive technology" per JLabel on-line documentation
        swingTempPanel.add(swingUpdateGroupId);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 3));
        swingTempPanel.add(swingNumUr92Label = new JLabel(" "));
        updateDisplayOfUr92Count();
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.BorderLayout());
        dtModel = new ResponsesTableModel(
                new String[]{ Bundle.getMessage("IPLDeviceInfoHeaderType"),
                        Bundle.getMessage("IPLDeviceInfoHeaderSerial"),
                        Bundle.getMessage("IPLDeviceInfoHeaderFwv") },0);
        swingDeviceTable.setModel(dtModel);
        JScrollPane sp=new JScrollPane(swingDeviceTable);
        sp.setPreferredSize(new Dimension(0,swingDeviceTable.getRowHeight()*5)); //five rows high
        swingTempPanel.add(sp);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.BorderLayout());
        dtrtModel = new ResponsesTableModel(new String[]{ Bundle.getMessage("IPLDeviceRespHeaderTime"),
                Bundle.getMessage("IPLDeviceRespHeaderName"),
                Bundle.getMessage("IPLDeviceRespHeaderChannel"),
                Bundle.getMessage("IPLDeviceRespHeaderPassword"),
                Bundle.getMessage("IPLDeviceRespHeaderGroupID") },0);
        swingDeviceResponseTable.setModel(dtrtModel);
        JScrollPane spdtrt=new JScrollPane(swingDeviceResponseTable);
        spdtrt.setPreferredSize(new Dimension(0,swingDeviceResponseTable.getRowHeight()*5)); //five rows high
        swingTempPanel.add(spdtrt);
        add(swingTempPanel);

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout());
        swingTempPanel.add(swingReadButton);
        swingTempPanel.add(swingSetButton);
        add(swingTempPanel);

        add(new JSeparator());

        swingTempPanel = new JPanel();
        swingTempPanel.setLayout(new java.awt.FlowLayout());
        swingTempPanel.add(swingStatusValueLabel);
        add(swingTempPanel);

        swingUpdateGrpName.addActionListener(e -> {
            swingNameValueField.setForeground(COLOR_OK);
            updateStatusLineMessage(" ", COLOR_STATUS_OK);
            if (validateGroupNameField() == false) {
                swingNameValueField.setForeground(COLOR_ERROR_VAL);
                updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
                swingNameValueField.requestFocusInWindow();
                return;
            }
            writeGroupName();
            readButtonActionPerformed();
        });
        swingUpdateChannel.addActionListener(e -> {
            swingChannelValueField.setForeground(COLOR_OK);
            updateStatusLineMessage(" ", COLOR_STATUS_OK);
            if (validateGroupNameField() == false) {
                swingChannelValueField.setForeground(COLOR_ERROR_VAL);
                updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
                swingChannelValueField.requestFocusInWindow();
                return;
            }
            writeChannel();
            readButtonActionPerformed();
        });
        swingUpdatePassword.addActionListener(e -> {
            swingPasswordValueField.setForeground(COLOR_OK);
            updateStatusLineMessage(" ", COLOR_STATUS_OK);
            if (validateGroupNameField() == false) {
                swingPasswordValueField.setForeground(COLOR_ERROR_VAL);
                updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
                swingPasswordValueField.requestFocusInWindow();
                return;
            }
            writePassword();
            readButtonActionPerformed();
        });
        swingUpdateGroupId.addActionListener(e -> {
            swingIdValueField.setForeground(COLOR_OK);
            updateStatusLineMessage(" ", COLOR_STATUS_OK);
            if (validateGroupNameField() == false) {
                swingIdValueField.setForeground(COLOR_ERROR_VAL);
                updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
                swingIdValueField.requestFocusInWindow();
                return;
            }
            writeDuplexId();
            readButtonActionPerformed();
        });

        swingSetButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setButtonActionPerformed();
            }
        });
        swingReadButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                scanButtonActionPerformed();
            }

        });
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel"; // NOI18N
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("Title");
    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        duplexGroupImplementation = new LnDplxGrpInfoImpl(memo);
        duplexGroupImplementation.addPropertyChangeListener(this);

        scanButtonActionPerformed();    // begin a query for UR92s
    }

    /**
     * Update GUI status then generates LocoNet message used to count the
     * available UR92 devices.
     */
    private void scanButtonActionPerformed() {
        numDuplexTypeDevices = 0;
        updateStatusLineMessage("ProcessingFindingUR92s", COLOR_STATUS_OK);
        duplexGroupImplementation.countUr92sAndQueryDuplexIdentityInfo();
    }

    /**
     * Modify GUI to show that displayed Duplex network information is not
     * currently valid. Creates and sends LocoNet traffic to query any available
     * UR92(s) for Duplex network identity information.
     */
    private void readButtonActionPerformed() {
        if (numDuplexTypeDevices == 0) {
            scanButtonActionPerformed();
            return;
        }

        swingNameValueField.setForeground(COLOR_OK);     // set foreground to default color
        swingChannelValueField.setForeground(COLOR_OK);  // set foreground to default color
        swingPasswordValueField.setForeground(COLOR_OK); // set foreground to default color
        swingIdValueField.setForeground(COLOR_OK);       // set foreground to default color
        swingNameValueField.setText(Bundle.getMessage("ValueUnknownGroupName"));
        swingChannelValueField.setText(Bundle.getMessage("ValueUnknownGroupChannel"));
        swingPasswordValueField.setText(Bundle.getMessage("ValueUnknownGroupPassword"));
        swingIdValueField.setText(Bundle.getMessage("ValueUnknownGroupID"));
        updateStatusLineMessage("ProcessingReadingInfo", COLOR_STATUS_OK);
        duplexGroupImplementation.queryDuplexGroupIdentity();
        updateStatusLineMessage("ProcessingWaitingForReport", COLOR_STATUS_OK);
    }

    /**
     * Validate the Duplex group name currently specified in the GUI. If the
     * group name is invalid, the GUI status line is updated with an appropriate
     * message.
     *
     * @return true if current swingNameValueField is a valid Duplex group name
     */
    private boolean validateGroupNameField() {
        return swingNameValueField.isValid();
    }

    /**
     * Validates the Duplex group channel number currently specified in the GUI.
     * If the group channel number is invalid, the GUI status line is updated
     * with an appropriate message.
     *
     * @return true if current swingChannelValueField is a valid Duplex group
     *         channel
     */
    private boolean validateGroupChannelField() {
        return swingChannelValueField.isValid();
    }

    /**
     * Validate the Duplex group ID number currently specified in the GUI. If
     * the group ID number is invalid, the GUI status line is updated with an
     * appropriate message.
     *
     * @return true if current swingIdValueField is a valid Duplex group ID
     *         number
     */
    private boolean validateGroupIDField() {
        return swingIdValueField.isValid();
    }

    /**
     * Validate the Duplex group password currently specified in the GUI.
     *
     * @return true if current swingNameValueField is a valid Duplex group
     *         password
     */
    private boolean validateGroupPasswordField() {
        return swingPasswordValueField.isValid();
    }

    /**
     * Perform actions required when the Set Group Information button is
     * clicked.
     * <p>
     * First validates the Duplex group name, channel, password, and group ID.
     * If any is invalid, the GUI status line is updated and the process is
     * aborted. If all are valid, the appropriate LocoNet messages are created,
     * and sent in sequence, to update the attached UR92(s) to the specified
     * Duplex group identity information, then initiates a read of the UR92(s)
     * to update the GUI.
     */
    private void setButtonActionPerformed() {
        boolean result = true;

        // assume all values are valid, so put fields to valid color and clear
        // status line
        swingNameValueField.setForeground(COLOR_OK);
        swingChannelValueField.setForeground(COLOR_OK);
        swingPasswordValueField.setForeground(COLOR_OK);
        swingIdValueField.setForeground(COLOR_OK);
        updateStatusLineMessage(" ", COLOR_STATUS_OK);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (validateGroupNameField() == false) {
            swingNameValueField.setForeground(COLOR_ERROR_VAL);
            result = false;
            updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
            swingNameValueField.requestFocusInWindow();
        } else if (validateGroupChannelField() == false) {
            swingChannelValueField.setForeground(COLOR_ERROR_VAL);
            result = false;
            updateStatusLineMessage("ErrorBadGroupChannel", COLOR_STATUS_ERROR);
            swingChannelValueField.requestFocusInWindow();
        } else if (validateGroupPasswordField() == false) {
            swingPasswordValueField.setForeground(COLOR_ERROR_VAL);
            result = false;
            updateStatusLineMessage("ErrorBadGroupPassword", COLOR_STATUS_ERROR);
            swingPasswordValueField.requestFocusInWindow();
        } else if (validateGroupIDField() == false) {
            swingIdValueField.setForeground(COLOR_ERROR_VAL);
            result = false;
            updateStatusLineMessage("ErrorBadGroupId", COLOR_STATUS_ERROR);
            swingIdValueField.requestFocusInWindow();
        }

        if (result == true) {
            if (!writeGroupName() ||
                    !writeChannel() ||
                    !writePassword() ||
                    !writeDuplexId()) {
                // do nothing
            } else {
                readButtonActionPerformed();
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    // delay need between messages slower Duplex/LNWI devices
    // eg LNWI, UR93
    private static final int WRITE_DELAY = 250;
    private static final int WRITE_DELAY_PASSWORD = 2500; // LNWI need a lot of time on password updates

    // four routines for performing the writes.
    private boolean writeGroupName() {
        updateStatusLineMessage("ProcessingGroupUpdate", COLOR_STATUS_OK);
        StringBuilder writeGroupName = new StringBuilder();
        writeGroupName.append(swingNameValueField.getText());
        writeGroupName.append("         "); // ensure length at least 8 characters
        writeGroupName.setLength(LnDplxGrpInfoImplConstants.DPLX_NAME_LEN); // trim to required length
        try {
            duplexGroupImplementation.setDuplexGroupName(writeGroupName.toString());
            Thread.sleep(WRITE_DELAY);
        } catch (LocoNetException | InterruptedException e) {
            // illegal Duplex Group Name
            updateStatusLineMessage("ErrorBadGroupName", COLOR_STATUS_ERROR);
            swingNameValueField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private boolean writeChannel() {
        updateStatusLineMessage("ProcessingGroupUpdate", COLOR_STATUS_OK);
        try {
            duplexGroupImplementation.setDuplexGroupChannel(Integer.parseInt(swingChannelValueField.getText(), 10));
            Thread.sleep(WRITE_DELAY);
        } catch (LocoNetException | InterruptedException e) {
            // illegal Duplex Group Channel
            updateStatusLineMessage("ErrorBadGroupChannel", COLOR_STATUS_ERROR);
            swingChannelValueField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private boolean writePassword() {
        try {
            duplexGroupImplementation.setDuplexGroupPassword(swingPasswordValueField.getText());
            Thread.sleep(WRITE_DELAY_PASSWORD);
        } catch (LocoNetException | InterruptedException e) {
            // illegal Duplex Group Password
            updateStatusLineMessage("ErrorBadGroupPassword", COLOR_STATUS_ERROR);
            swingPasswordValueField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private boolean writeDuplexId() {
        try {
            duplexGroupImplementation.setDuplexGroupId(swingIdValueField.getText());
            Thread.sleep(WRITE_DELAY);
        } catch (LocoNetException | InterruptedException e) {
            // illegal Duplex Group Id
            updateStatusLineMessage("ErrorBadGroupId", COLOR_STATUS_ERROR);
            swingIdValueField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    /**
     *
     * @param s     Name of tag in .properties file for string to be converted
     *              to HTML
     * @param width Width of resulting HTML, in Swing dimensional units
     * @return String containing HTML for input string s
     */
    private String convertToHtml(String s, int width) {
        String result = "<html><body><div align=center style='width: "; // NOI18N

        if (s.length() == 1) {
            result = " ";
        } else {
            result = result + width + "'>" +  // NOI18N
                    Bundle.getMessage(s);
        }
        return result;
    }

    /**
     * Update the GUI label showing the number of UR92 devices.
     */
    private void updateDisplayOfUr92Count() {
        Object[] messageArguments = {
            numDuplexTypeDevices,
            numDuplexTypeDevices
        };
        java.text.MessageFormat formatter = new java.text.MessageFormat("");

        try {
            formatter.applyPattern(Bundle.getMessage("LabelDeviceCountUR92"));
            double[] pluralLimits = {0, 1, 2};
            String[] devicePlurals = {
                    Bundle.getMessage("LabelDeviceCountUR92Plural0"),
                    Bundle.getMessage("LabelDeviceCountUR92Plural1"),
                    Bundle.getMessage("LabelDeviceCountUR92Plural2")
            };
            java.text.ChoiceFormat pluralForm = new java.text.ChoiceFormat(pluralLimits, devicePlurals);
            java.text.Format[] messageFormats = {
                java.text.NumberFormat.getInstance(),
                pluralForm
            };
            formatter.setFormats(messageFormats);
            String ur92CountString = formatter.format(messageArguments);
            swingNumUr92Label.setText(ur92CountString);
        } catch (RuntimeException e) {
            swingNumUr92Label.setText(Bundle.getMessage("LabelDeviceCountUR92Except", numDuplexTypeDevices));
            // eat the exception and show a simple, gramatically ambiguous message
        }
        swingNumUr92Label.repaint();
    }

    private void updateStatusLineMessage(String statusMessage, java.awt.Color fgColor) {
        if (statusMessage == null) {
            swingStatusValueLabel.setForeground(fgColor);
            swingStatusValueLabel.setName(" ");  //this string is used as a reference to a .properties file entry; internationalization is handled there.
            swingStatusValueLabel.setText(convertToHtml(swingStatusValueLabel.getName(), minWindowWidth));
        } else {
            swingStatusValueLabel.setForeground(fgColor);
            swingStatusValueLabel.setName(statusMessage);  //this string is used as a reference to a .properties file entry; internationalization is handled there.
            swingStatusValueLabel.setText(convertToHtml(swingStatusValueLabel.getName(), minWindowWidth));
        }
    }

    /**
     * Process the "property change" events from LnDplxGrpInfoImpl and
     * ValidatedTextField object. Includes processing of:
     * <ul>
     *     <li>ValidatedTextField - ValidatedTextField.VTF_PC_STAT_LN_UPDATE
     *     <li>LnDplxGrpInfoImpl - StatusDontBlastError
     *     <li>StatusLineUpdate
     *     <li>NumberOfUr92sUpdate
     * </ul>
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        // these messages can arrive without a complete
        // GUI, in which case we just ignore them
        String eventName = evt.getPropertyName();

        if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR)) {
            if (swingStatusValueLabel == null) {
                return;
            }

            if (swingStatusValueLabel.getForeground().equals(COLOR_STATUS_ERROR)) {
                return; // don't overwrite an existing error message for this case
            }
            String statusMessage = (String) evt.getNewValue();
            java.awt.Color fgColor = COLOR_STATUS_OK;
            if (statusMessage == null) {
                updateStatusLineMessage(" ", COLOR_STATUS_OK);
                return;
            } // if current status message begins with Error, then don't replace it.
            else if ((statusMessage.startsWith("Error"))
                    || (swingStatusValueLabel.getForeground().equals(COLOR_STATUS_ERROR))) {
                return;
            }
            // is not an error message, so replace it.
            updateStatusLineMessage(statusMessage, fgColor);
        } else if ( eventName.equals(LnDplxGrpInfoImpl.DPLX_IPL_DEVICE_DETAILS) ) {
            LnDplxGrpInfoImpl.BasicIPLDeviceInfo tmpItem = (LnDplxGrpInfoImpl.BasicIPLDeviceInfo)evt.getNewValue();
            if (tmpItem.getType().isEmpty()) {
                for (int ix = dtModel.getRowCount()-1  ; ix > -1 ; ix --) {
                    dtModel.removeRow(ix);
                }
            } else {
                dtModel.addRow(new String[] {tmpItem.getType(), tmpItem.getSerialNumber(), tmpItem.getSwVersion()});
            }
            return;
        } else if ( eventName.equals(LnDplxGrpInfoImpl.DPLX_IPL_DEVICE_RESPONSE_DETAILS) ) {
            LnDplxGrpInfoImpl.BasicIPLDeviceResponseInfo tmpItem = (LnDplxGrpInfoImpl.BasicIPLDeviceResponseInfo)evt.getNewValue();
            if (tmpItem.getGroupName().isEmpty()) {
                for (int ix = dtrtModel.getRowCount()-1  ; ix > -1 ; ix --) {
                    dtrtModel.removeRow(ix);
                }
            } else {
                dtrtModel.addRow(new String[] {dtf.format(LocalDateTime.now()),tmpItem.getGroupName(), tmpItem.getChannel(), tmpItem.getPassword(), tmpItem.getGroupId()});
            }
            return;
        } else if ((eventName.equals(ValidatedTextField.VTF_PC_STAT_LN_UPDATE))
                || (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_STAT_LN_UPDATE))) {
            if (swingStatusValueLabel == null) {
                return;
            }
            String statusMessage = (String) evt.getNewValue();
            if (statusMessage == null) {
                updateStatusLineMessage(" ", COLOR_STATUS_OK);
                return;
            } else {
                java.awt.Color fgColor = COLOR_STATUS_OK;
                if (statusMessage.startsWith("ERROR:")) { // NOI18N
                    fgColor = COLOR_STATUS_ERROR;
                    statusMessage = statusMessage.substring(6);
                } else if (statusMessage.startsWith("Error")) { // NOI18N
                    fgColor = COLOR_STATUS_ERROR;
                }
                updateStatusLineMessage(statusMessage, fgColor);
            }
        } else if (eventName.equals("NumberOfUr92sUpdate")) { // NOI18N
            numDuplexTypeDevices = (Integer) evt.getNewValue();
            updateDisplayOfUr92Count();
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_NAME_VALIDITY)) {
            swingNameValueField.setForeground(COLOR_OK);
            swingNameValueField.setEnabled(evt.getNewValue().equals(true));
            if (swingNameValueField.isEnabled()
                    && swingChannelValueField.isEnabled()
                    && swingPasswordValueField.isEnabled()
                    && swingIdValueField.isEnabled()) {
                swingSetButton.setEnabled(true);
            } else {
                swingSetButton.setEnabled(false);
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_CHANNEL_VALIDITY)) {
            swingChannelValueField.setForeground(COLOR_OK);
            swingChannelValueField.setEnabled(evt.getNewValue().equals(true));
            if (swingNameValueField.isEnabled()
                    && swingChannelValueField.isEnabled()
                    && swingPasswordValueField.isEnabled()
                    && swingIdValueField.isEnabled()) {
                swingSetButton.setEnabled(true);
            } else {
                swingSetButton.setEnabled(false);
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_PASSWORD_VALIDITY)) {
            swingPasswordValueField.setForeground(COLOR_OK);
            swingPasswordValueField.setEnabled(evt.getNewValue().equals(true));
            if (swingNameValueField.isEnabled()
                    && swingChannelValueField.isEnabled()
                    && swingPasswordValueField.isEnabled()
                    && swingIdValueField.isEnabled()) {
                swingSetButton.setEnabled(true);
            } else {
                swingSetButton.setEnabled(false);
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_ID_VALIDITY)) {
            swingIdValueField.setForeground(COLOR_OK);
            swingIdValueField.setEnabled(evt.getNewValue().equals(true));
            if (swingNameValueField.isEnabled()
                    && swingChannelValueField.isEnabled()
                    && swingPasswordValueField.isEnabled()
                    && swingIdValueField.isEnabled()) {
                swingSetButton.setEnabled(true);
            } else {
                swingSetButton.setEnabled(false);
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_NAME_UPDATE)) {
            if (evt.getNewValue().equals(true)) {
                String s = duplexGroupImplementation.getFetchedDuplexGroupName();
                showValidGroupName(s);
                swingNameValueField.setLastQueriedValue(s);
            } else {
                disableGroupName();
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_CHANNEL_UPDATE)) {
            if (evt.getNewValue().equals(true)) {
                String s = duplexGroupImplementation.getFetchedDuplexGroupChannel();
                showValidGroupChannel(s);
                swingChannelValueField.setLastQueriedValue(s);
            } else {
                disableGroupChannel();
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_PASSWORD_UPDATE)) {
            if (evt.getNewValue().equals(true)) {
                String s = duplexGroupImplementation.getFetchedDuplexGroupPassword();
                showValidGroupPassword(s);
                swingPasswordValueField.setLastQueriedValue(s);
            } else {
                disableGroupPassword();
            }
        } else if (eventName.equals(LnDplxGrpInfoImpl.DPLX_PC_ID_UPDATE)) {
            if (evt.getNewValue().equals(true)) {
                String s = duplexGroupImplementation.getFetchedDuplexGroupId();
                showValidGroupId(s);
                swingIdValueField.setLastQueriedValue(s);
            } else {
                disableGroupId();
            }
        }
    }

    private void showValidGroupName(String gn) {
        swingNameValueField.setForeground(COLOR_OK);
        swingNameValueField.setBackground(COLOR_BG_UNEDITED);
        swingNameValueField.setEnabled(true);
        swingNameValueField.setText(gn);
    }

    private void disableGroupName() {
        swingNameValueField.setForeground(COLOR_OK);
        swingNameValueField.setBackground(COLOR_BG_UNEDITED);
        swingNameValueField.setEnabled(false);
        swingNameValueField.setText("????????");
    }

    private void showValidGroupChannel(String gc) {
        swingChannelValueField.setForeground(COLOR_OK);
        swingChannelValueField.setBackground(COLOR_BG_UNEDITED);
        swingChannelValueField.setEnabled(true);
        swingChannelValueField.setText(gc);
    }

    private void disableGroupChannel() {
        swingChannelValueField.setForeground(COLOR_OK);
        swingChannelValueField.setBackground(COLOR_BG_UNEDITED);
        swingChannelValueField.setEnabled(false);
        swingChannelValueField.setText("??");
    }

    private void showValidGroupPassword(String gp) {
        swingPasswordValueField.setForeground(COLOR_OK);
        swingPasswordValueField.setBackground(COLOR_BG_UNEDITED);
        swingPasswordValueField.setEnabled(true);
        swingPasswordValueField.setText(gp);
    }

    private void disableGroupPassword() {
        swingPasswordValueField.setForeground(COLOR_OK);
        swingPasswordValueField.setBackground(COLOR_BG_UNEDITED);
        swingPasswordValueField.setEnabled(false);
        swingPasswordValueField.setText("????");
    }

    private void showValidGroupId(String gi) {
        swingIdValueField.setForeground(COLOR_OK);
        swingIdValueField.setBackground(COLOR_BG_UNEDITED);
        swingIdValueField.setEnabled(true);
        swingIdValueField.setText(gi);
    }

    private void disableGroupId() {
        swingIdValueField.setForeground(COLOR_OK);
        swingIdValueField.setBackground(COLOR_BG_UNEDITED);
        swingIdValueField.setEnabled(false);
        swingIdValueField.setText("???");
    }

    // defines for colorizing the user input GUI elements and status line
    public final static java.awt.Color COLOR_MISMATCH_VAL = java.awt.Color.red.darker();
    public final static java.awt.Color COLOR_UNKN_VAL = java.awt.Color.yellow.brighter();
    public final static java.awt.Color COLOR_READ = null; // use default color for the component
    public final static java.awt.Color COLOR_BG_EDITED = java.awt.Color.orange; // use default color for the component
    public final static java.awt.Color COLOR_ERROR_VAL = java.awt.Color.black;
    public final static java.awt.Color COLOR_OK = java.awt.Color.black;
    public final static java.awt.Color COLOR_BG_OK = java.awt.Color.white;
    public final static java.awt.Color COLOR_BG_UNEDITED = java.awt.Color.white;
    public final static java.awt.Color COLOR_STATUS_OK = java.awt.Color.black;
    public final static java.awt.Color COLOR_STATUS_ERROR = java.awt.Color.red;
    public final static java.awt.Color COLOR_BG_ERROR = java.awt.Color.red;

    // helper for laying out the GUI
    public final static int DEFAULT_WINDOW_WIDTH = 200;

    /**
     * Nested class to create a DuplexGroupInfoPanel using old-style defaults.
     * This is most useful when adding DuplexGroupInfoPanel as a JMRI Start-up
     * action.
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemDuplexInfo"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    DuplexGroupInfoPanel.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    // make the table model read only
    static public class ResponsesTableModel extends DefaultTableModel {
         public ResponsesTableModel(String[] columns, int rows) {
             super(columns, rows);
         }
        @Override
        public boolean isCellEditable(int row, int column){
            return false;
        }
   }

   //    private final static Logger log = LoggerFactory.getLogger(DuplexGroupInfoPanel.class);

}
