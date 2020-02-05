package jmri.jmrit.whereused;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import jmri.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JComboBoxUtil;

public class WhereUsedFrame extends jmri.util.JmriJFrame {

    public WhereUsedFrame() {
        super(true, true);
        setTitle(Bundle.getMessage("TitleWhereUsed"));  // NOI18N
        createFrame();
    }

    ItemType _itemType = ItemType.NONE;
    JComboBox<ItemType> _itemTypeBox;

    String _itemName;
    NamedBeanComboBox<?> _itemNameBox = new NamedBeanComboBox<Sensor>(
                        InstanceManager.getDefault(SensorManager.class));
    JPanel _topPanel;
    JPanel _scrolltext = new JPanel();
    JTextArea _textContent;

    /**
     * Create the window frame.  The top part contains the item type and item name
     * combo boxes, the middle contains the scrollable where used text area and the
     * bottom part has a button for saving the content to a file.
     */
    void createFrame() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Build the top panel
        _topPanel = new JPanel();
        _topPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelItemType"))));  // NOI18N
        _itemTypeBox = new JComboBox<>();
        for (ItemType itemType : ItemType.values()) {
            _itemTypeBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_itemTypeBox);
        _topPanel.add(_itemTypeBox);

        _topPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelItemName"))));  // NOI18N
        _topPanel.add(_itemNameBox);
        _itemNameBox.setEnabled(false);
        _itemTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _itemType = _itemTypeBox.getItemAt(_itemTypeBox.getSelectedIndex());
                setItemNameBox(_itemType);
            }
        });
        contentPane.add(_topPanel, BorderLayout.NORTH);

        // Build the where used listing
        JScrollPane scrollPane = null;

        buildWhereUsedListing();
        scrollPane = new JScrollPane(_scrolltext);
        contentPane.add(scrollPane);

        // Build the bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JButton saveBrowse = new JButton(Bundle.getMessage("SaveButton"));   // NOI18N
        saveBrowse.setToolTipText(Bundle.getMessage("SaveButtonHint"));      // NOI18N
        bottomPanel.add(saveBrowse, BorderLayout.EAST);
        saveBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWhereUsedPressed();
            }
        });
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Create a new NamedBeanComboBox based on the item type and refresh the panel.
     * A selection listener invokes the buildWhereUsedListing method.
     * @param itemType The enum for the selected item type.
     */
    void setItemNameBox(ItemType itemType) {
        NamedBeanComboBox<?> newNameBox = createNameBox(itemType);
        if (newNameBox == null) {
            _itemNameBox.setSelectedIndex(-1);
            _itemNameBox.setEnabled(false);
            return;
        }
        _itemNameBox = newNameBox;
        _itemNameBox.setSelectedIndex(-1);
        _topPanel.remove(3);
        _topPanel.add(_itemNameBox);

        _itemNameBox.setEnabled(true);
        _itemNameBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getModifiers() == 0) {
                    log.info("ignore event");
                    return;
                }
                Object src = e.getSource();
                if (!(src instanceof NamedBeanComboBox)) {
                    return;
                }
                buildWhereUsedListing();
            }
        });
        pack();
    }

    /**
     * Build the where used content and update the JScrollPane.
     * <p>
     * A textarea is created and then passed to the detail class be to populated, along with
     * the selected object.  When the updated textarea is returned, final setup is done
     * along with refreshing the scrollable panel.
     */
    void buildWhereUsedListing() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText(null);
        textArea.setTabSize(4);
        textArea.setEditable(false);

        switch (_itemType) {
            case SENSOR:
                Sensor sensor = (Sensor) _itemNameBox.getSelectedItem();
                SensorWhereUsed.getSensorWhereUsed(sensor, textArea);
                break;

            case TURNOUT:
                Turnout turnout = (Turnout) _itemNameBox.getSelectedItem();
                textArea.append("turnout = " + turnout.getDisplayName());
                log.info("turnout = {}", turnout.getDisplayName());
                // Invoke textarea
                break;

            default:
                textArea.append(" -- nothing yet --");
                break;
        }

        textArea.setCaretPosition(0);
        if (_scrolltext.getComponentCount() > 0) {
            _scrolltext.remove(0);
        }
        _scrolltext.add(textArea);
        pack();
        repaint();
        return;
    }

    /**
     * Save the where used textarea content to a text file.
     */
    void saveWhereUsedPressed() {
//         userFileChooser.setApproveButtonText(Bundle.getMessage("BrowserSaveDialogApprove"));  // NOI18N
//         userFileChooser.setDialogTitle(Bundle.getMessage("BrowserSaveDialogTitle"));  // NOI18N
//         userFileChooser.rescanCurrentDirectory();
//         // Default to logix system name.txt
//         userFileChooser.setSelectedFile(new File(_curLogix.getSystemName() + ".txt"));  // NOI18N
//         int retVal = userFileChooser.showSaveDialog(null);
//         if (retVal != JFileChooser.APPROVE_OPTION) {
//             log.debug("Save browser content stopped, no file selected");  // NOI18N
//             return;  // give up if no file selected or cancel pressed
//         }
//         File file = userFileChooser.getSelectedFile();
//         log.debug("Save browser content to '{}'", file);  // NOI18N
//
//         if (file.exists()) {
//             Object[] options = {Bundle.getMessage("BrowserSaveDuplicateReplace"),  // NOI18N
//                     Bundle.getMessage("BrowserSaveDuplicateAppend"),  // NOI18N
//                     Bundle.getMessage("ButtonCancel")};               // NOI18N
//             int selectedOption = JOptionPane.showOptionDialog(null,
//                     Bundle.getMessage("BrowserSaveDuplicatePrompt", file.getName()), // NOI18N
//                     Bundle.getMessage("BrowserSaveDuplicateTitle"),   // NOI18N
//                     JOptionPane.DEFAULT_OPTION,
//                     JOptionPane.WARNING_MESSAGE,
//                     null, options, options[0]);
//             if (selectedOption == 2 || selectedOption == -1) {
//                 log.debug("Save browser content stopped, file replace/append cancelled");  // NOI18N
//                 return;  // Cancel selected or dialog box closed
//             }
//             if (selectedOption == 0) {
//                 FileUtil.delete(file);  // Replace selected
//             }
//         }
//
//         // Create the file content
//         String tStr = Bundle.getMessage("BrowserLogix") + " " + _curLogix.getSystemName() + "    "  // NOI18N
//                 + _curLogix.getUserName() + "    "
//                 + (Boolean.valueOf(_curLogix.getEnabled())
//                         ? Bundle.getMessage("BrowserEnabled")    // NOI18N
//                         : Bundle.getMessage("BrowserDisabled"));  // NOI18N
//         JTextArea textContent = buildConditionalListing();
//         try {
//             // ADD Logix Header inforation first
//             FileUtil.appendTextToFile(file, tStr);
//             FileUtil.appendTextToFile(file, textContent.getText());
//         } catch (IOException e) {
//             log.error("Unable to write browser content to '{}', exception: '{}'", file, e);  // NOI18N
//         }
    }

    /**
     * Create a combo name box for name selection.
     *
     * @param itemType The selected variable or action type
     * @return nameBox A combo box based on the item type or null if no match
     */
    NamedBeanComboBox<?> createNameBox(ItemType itemType) {
        NamedBeanComboBox<?> nameBox;
        switch (itemType) {
            case SENSOR:
                nameBox = new NamedBeanComboBox<Sensor>(
                        InstanceManager.getDefault(SensorManager.class));
                break;
            case TURNOUT:
                nameBox = new NamedBeanComboBox<Turnout>(
                        InstanceManager.getDefault(TurnoutManager.class));
                break;
//             case LIGHT:       // 3
//                 nameBox = new NamedBeanComboBox<Light>(
//                         InstanceManager.getDefault(LightManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case SIGNALHEAD:  // 4
//                 nameBox = new NamedBeanComboBox<SignalHead>(
//                         InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case SIGNALMAST:  // 5
//                 nameBox = new NamedBeanComboBox<SignalMast>(
//                         InstanceManager.getDefault(SignalMastManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case MEMORY:      // 6
//                 nameBox = new NamedBeanComboBox<Memory>(
//                         InstanceManager.getDefault(MemoryManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case LOGIX:       // 7
//                 nameBox = new NamedBeanComboBox<Logix>(
//                         InstanceManager.getDefault(LogixManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case WARRANT:     // 8
//                 nameBox = new NamedBeanComboBox<Warrant>(
//                         InstanceManager.getDefault(WarrantManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case OBLOCK:      // 10
//                 nameBox = new NamedBeanComboBox<OBlock>(
//                         InstanceManager.getDefault(OBlockManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case ENTRYEXIT:   // 11
//                 nameBox = new NamedBeanComboBox<DestinationPoints>(
//                         InstanceManager.getDefault(EntryExitPairs.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
//             case OTHER:   // 14
//                 nameBox = new NamedBeanComboBox<Route>(
//                         InstanceManager.getDefault(jmri.RouteManager.class), null, DisplayOptions.DISPLAYNAME);
//                 break;
            default:
                return null;             // Skip any other items.
        }
        nameBox.setEditable(false);
        nameBox.setValidatingInput(false);
        JComboBoxUtil.setupComboBoxMaxRows(nameBox);
        return nameBox;
    }

    /**
     * The item types.  A bundle key for each type is stored with the type to
     * create a language dependent toString result.
     */
    enum ItemType {
        NONE("ItemTypeNone"),
        SENSOR("ItemTypeSensor"),
        TURNOUT("ItemTypeTurnout");
//         LIGHT(ITEM_TYPE_LIGHT, IsStateVar.IS_STATE_VAR, "ItemTypeLight"),
//         SIGNALHEAD(ITEM_TYPE_SIGNALHEAD, IsStateVar.IS_STATE_VAR, "ItemTypeSignalHead"),
//         SIGNALMAST(ITEM_TYPE_SIGNALMAST, IsStateVar.IS_STATE_VAR, "ItemTypeSignalMast"),
//         MEMORY(ITEM_TYPE_MEMORY, IsStateVar.IS_STATE_VAR, "ItemTypeMemory"),
//         CONDITIONAL(ITEM_TYPE_CONDITIONAL, IsStateVar.IS_STATE_VAR, "ItemTypeConditional"),  // used only by ConditionalVariable
//         LOGIX(ITEM_TYPE_LOGIX, IsStateVar.IS_STATE_VAR, "ItemTypeLogix"),                    // used only by ConditionalAction
//         WARRANT(ITEM_TYPE_WARRANT, IsStateVar.IS_STATE_VAR, "ItemTypeWarrant"),
//         CLOCK(ITEM_TYPE_CLOCK, IsStateVar.IS_STATE_VAR, "ItemTypeClock"),
//         OBLOCK(ITEM_TYPE_OBLOCK, IsStateVar.IS_STATE_VAR, "ItemTypeOBlock"),
//         ENTRYEXIT(ITEM_TYPE_ENTRYEXIT, IsStateVar.IS_STATE_VAR, "ItemTypeEntryExit"),
//
//         AUDIO(ITEM_TYPE_AUDIO, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeAudio"),
//         SCRIPT(ITEM_TYPE_SCRIPT, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeScript"),
//         OTHER(ITEM_TYPE_OTHER, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeOther");

        private final String _bundleKey;

        private ItemType(String bundleKey) {
            _bundleKey = bundleKey;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedFrame.class);

}
