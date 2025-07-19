package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.util.WhereUsed;
import jmri.swing.NamedBeanComboBox;
import jmri.util.FileUtil;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Create a where used report based on the selected bean.  The selection combo box is
 * based on the selected type.
 * <P>
 * This class is a copy of jmri.jmrit.whereused.WhereUsedFrame, but it only shows
 * where a bean is used by LogixNG. On the other hand, it shows where in the LogixNG
 * tree the bean is used.
 *
 * @author Dave Sand         Copyright (C) 2020
 * @author Daniel Bergqvist  Copyright (C) 2023
 */
public class WhereUsedFrame extends jmri.util.JmriJFrame {
    ItemType _itemType = ItemType.NONE;
    JComboBox<ItemType> _itemTypeBox;

    NamedBean _itemBean;
    NamedBeanComboBox<?> _itemNameBox = new NamedBeanComboBox<>(
                        InstanceManager.getDefault(SensorManager.class));

    JPanel _topPanel;
    JPanel _bottomPanel;
    JPanel _scrolltext = new JPanel();
    JTextArea _textArea = new JTextArea();
    JButton _createButton;
    JLabel itemNameLabel;

    public WhereUsedFrame() {
        super(true, true);
        setTitle(Bundle.getMessage("WhereUsed_Title"));  // NOI18N
        createFrame();
        addHelpMenu("package.jmri.jmrit.logixng.LogixNGWhereUsed", true);  // NOI18N
    }

    /**
     * Create the window frame.  The top part contains the item type, the item name
     * combo box, and a Create button.  The middle contains the scrollable "where used" text area and the
     * bottom part has a button for saving the content to a file.
     */
    void createFrame() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Build the top panel
        buildTopPanel();
        contentPane.add(_topPanel, BorderLayout.NORTH);

        // Build an empty where used listing
        JScrollPane scrollPane;
        buildWhereUsedListing(ItemType.NONE, null);
        scrollPane = new JScrollPane(_scrolltext);
        contentPane.add(scrollPane);

        // Build the bottom panel
        buildBottomPanel();
        contentPane.add(_bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    void buildTopPanel() {
        _topPanel = new JPanel();
        JLabel itemTypeLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("WhereUsed_LabelItemType")));  // NOI18N
        _topPanel.add(itemTypeLabel);
        _itemTypeBox = new JComboBox<>();
        itemTypeLabel.setLabelFor(_itemTypeBox);
        for (ItemType itemType : ItemType.values()) {
            _itemTypeBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_itemTypeBox);
        _topPanel.add(_itemTypeBox);

        itemNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("WhereUsed_LabelItemName")));  // NOI18N
        _topPanel.add(itemNameLabel);
        itemNameLabel.setLabelFor(_itemNameBox);
        _topPanel.add(_itemNameBox);
        _itemTypeBox.addActionListener((e) -> {
            _itemType = _itemTypeBox.getItemAt(_itemTypeBox.getSelectedIndex());
            setItemNameBox(_itemType);
        });

        _createButton = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
        _createButton.addActionListener((e) -> buildWhereUsedListing(_itemType, _itemBean));

        _topPanel.add(_createButton);
        _itemNameBox.setEnabled(false);
        _createButton.setEnabled(false);
    }

    void buildBottomPanel() {
        _bottomPanel = new JPanel();
        _bottomPanel.setLayout(new BorderLayout());

        JButton saveButton = new JButton(Bundle.getMessage("WhereUsed_SaveButton"));   // NOI18N
        saveButton.setToolTipText(Bundle.getMessage("WhereUsed_SaveButtonHint"));      // NOI18N
        _bottomPanel.add(saveButton, BorderLayout.EAST);
        saveButton.addActionListener((ActionEvent e) -> saveWhereUsedPressed());
    }

    /**
     * Create a new NamedBeanComboBox based on the item type and refresh the panel.
     * A selection listener saves the selection and enables the Create button.
     * @param itemType The enum for the selected item type.
     */
    void setItemNameBox(ItemType itemType) {
        _createButton.setEnabled(false);
        buildWhereUsedListing(ItemType.NONE, null);
        NamedBeanComboBox<?> newNameBox = createNameBox(itemType);
        if (newNameBox == null) {
            _itemNameBox.setSelectedIndex(-1);
            _itemNameBox.setEnabled(false);
            return;
        }
        _itemNameBox = newNameBox;
        itemNameLabel.setLabelFor(newNameBox);
        _itemNameBox.setSelectedIndex(-1);
        _topPanel.remove(3);
        _topPanel.add(_itemNameBox, 3);

        _itemNameBox.setEnabled(true);
        _itemNameBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                _itemBean = (NamedBean) e.getItem();
                _createButton.setEnabled(true);
            }
        });
        pack();
        repaint();
    }

    /**
     * Build the where used content and update the JScrollPane.
     * <p>
     * The selected object is passed to the appropriate detail class which returns a populated textarea.
     * The textarea is formatted and inserted into a scrollable panel.
     * @param type Indicated type of item being examined
     * @param bean The bean being examined
     */
    void buildWhereUsedListing(ItemType type, NamedBean bean) {
        if (type != ItemType.NONE && bean != null) {
            String str = WhereUsed.whereUsed(bean);
            if (str.isEmpty()) {
                str = Bundle.getMessage("WhereUsed_NotInUse");
            }
            _textArea.setText(str);
        } else {
            _textArea.setText("");
        }
        _textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        _textArea.setTabSize(4);
        _textArea.setEditable(false);
        _textArea.setCaretPosition(0);
        if (_scrolltext.getComponentCount() > 0) {
            _scrolltext.remove(0);
        }
        _scrolltext.add(_textArea);
        pack();
        repaint();
    }

    JFileChooser userFileChooser = new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());

    /**
     * Save the where used textarea content to a text file.
     */
    void saveWhereUsedPressed() {
        userFileChooser.setApproveButtonText(Bundle.getMessage("SaveDialogApprove"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("SaveDialogTitle"));  // NOI18N
        userFileChooser.rescanCurrentDirectory();

        String itemName = _itemNameBox.getSelectedItemDisplayName();
        String fileName = Bundle.getMessage("SaveFileName", (itemName == null) ? "Unknown" : itemName);  // NOI18N
        userFileChooser.setSelectedFile(new File(fileName));
        int retVal = userFileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            log.debug("Save where used content stopped, no file selected");  // NOI18N
            return;  // give up if no file selected or cancel pressed
        }
        File file = userFileChooser.getSelectedFile();
        log.debug("Save where used content to '{}'", file);  // NOI18N

        if (file.exists()) {
            Object[] options = {Bundle.getMessage("SaveDuplicateReplace"),  // NOI18N
                    Bundle.getMessage("SaveDuplicateAppend"),  // NOI18N
                    Bundle.getMessage("ButtonCancel")};               // NOI18N
            int selectedOption = JmriJOptionPane.showOptionDialog(null,
                    Bundle.getMessage("SaveDuplicatePrompt", file.getName(),
                            Bundle.getMessage("SaveDuplicateAppend"),
                            Bundle.getMessage("SaveDuplicateReplace")), // NOI18N
                    Bundle.getMessage("SaveDuplicateTitle"),   // NOI18N
                    JmriJOptionPane.DEFAULT_OPTION,
                    JmriJOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 2 || selectedOption == -1) {
                log.debug("Save where used content stopped, file replace/append cancelled");  // NOI18N
                return;  // Cancel selected or dialog box closed
            }
            if (selectedOption == 0) {
                FileUtil.delete(file);  // Replace selected
            }
        }

        // Create the file content
        try {
            FileUtil.appendTextToFile(file, _textArea.getText());
        } catch (IOException e) {
            log.error("Unable to write where used content to '{}', exception", file, e);  // NOI18N
        }
    }

    /**
     * Create a combo name box for name selection.
     *
     * @param itemType The selected bean type
     * @return a combo box based on the item type or null if no match
     */
    NamedBeanComboBox<?> createNameBox(ItemType itemType) {
        NamedBeanComboBox<?> nameBox;
        switch (itemType) {
            case TURNOUT:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
                break;
            case SENSOR:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class));
                break;
            case LIGHT:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(LightManager.class));
                break;
            case SIGNALHEAD:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class));
                break;
            case SIGNALMAST:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class));
                break;
            case REPORTER:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(ReporterManager.class));
                break;
            case MEMORY:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(MemoryManager.class));
                break;
            case ROUTE:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(RouteManager.class));
                break;
            case OBLOCK:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class));
                break;
            case BLOCK:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(BlockManager.class));
                break;
            case SECTION:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SectionManager.class));
                break;
            case WARRANT:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(WarrantManager.class));
                break;
            case ENTRYEXIT:
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(EntryExitPairs.class));
                break;
            case AUDIO:
                Predicate<Audio> filter = (bean) -> { return bean.getSubType() != Audio.BUFFER; };
                nameBox = new NamedBeanComboBox<>(InstanceManager.getDefault(AudioManager.class),
                        null, jmri.NamedBean.DisplayOptions.DISPLAYNAME, filter);
                break;
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
        TURNOUT("BeanNameTurnout"),
        SENSOR("BeanNameSensor"),
        LIGHT("BeanNameLight"),
        SIGNALHEAD("BeanNameSignalHead"),
        SIGNALMAST("BeanNameSignalMast"),
        REPORTER("BeanNameReporter"),
        MEMORY("BeanNameMemory"),
        ROUTE("BeanNameRoute"),
        OBLOCK("BeanNameOBlock"),
        BLOCK("BeanNameBlock"),
        SECTION("BeanNameSection"),
        WARRANT("BeanNameWarrant"),
        ENTRYEXIT("BeanNameEntryExit"),
        AUDIO("BeanNameAudio");

        private final String _bundleKey;

        ItemType(String bundleKey) {
            _bundleKey = bundleKey;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedFrame.class);

}
