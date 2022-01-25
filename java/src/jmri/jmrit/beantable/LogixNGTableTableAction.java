package jmri.jmrit.beantable;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.jmrit.logixng.tools.swing.TableEditor;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.BeanSelectPanel;

import org.apache.commons.csv.CSVFormat;

/**
 * Swing action to create and register a LogixNG Table.
 * <p>
 Also contains the panes to create, edit, and delete a LogixNG.
 <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 * <p>
 *
 * @author Dave Duchamp Copyright (C) 2007 (LogixTableAction)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011 (LogixTableAction)
 * @author Matthew Harris copyright (c) 2009 (LogixTableAction)
 * @author Dave Sand copyright (c) 2017 (LogixTableAction)
 * @author Daniel Bergqvist copyright (c) 2019
 * @author Dave Sand copyright (c) 2021
 */
public class LogixNGTableTableAction extends AbstractLogixNGTableAction<NamedTable> {

    private final JRadioButton _typeExternalTable = new JRadioButton(Bundle.getMessage("LogixNG_typeExternalTable"));
    private final JRadioButton _typeInternalTable = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTable"));
    private final JRadioButton _typeInternalTableBasedOfTable = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTableBasedOfTable"));
    private final ButtonGroup _buttonGroup = new ButtonGroup();

    private final JRadioButton _internalBasedTable_CopyTable = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTableBasedOfTable_CopyTable"));
    private final JRadioButton _internalBasedTable_SameRowsAndColumns = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTableBasedOfTable_SameRowsAndColumns"));
    private final JRadioButton _internalBasedTable_SameRows = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTableBasedOfTable_SameRows"));
    private final JRadioButton _internalBasedTable_SameColumns = new JRadioButton(Bundle.getMessage("LogixNG_typeInternalTableBasedOfTable_SameColumns"));
    private final ButtonGroup _internalBasedTable_ButtonGroup = new ButtonGroup();

    private final JCheckBox _allowWrite = new JCheckBox(Bundle.getMessage("LogixNGTableTable_allowWrite"));
    private final JTextField _csvFileName = new JTextField(50);
    private final JCheckBox _storeTableDataInPanelFile = new JCheckBox(Bundle.getMessage("LogixNGTableTable_storeTableDataInPanelFile"));

    private enum NewTableType { External, Internal, InternalBasedOfTable }
    
    private NewTableType _newTableType;

    /**
     * Create a LogixNGTableAction instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public LogixNGTableTableAction(String s) {
        super(s);
    }

    /**
     * Create a LogixNGTableAction instance with default title.
     */
    public LogixNGTableTableAction() {
        this(Bundle.getMessage("TitleLogixNGTableTable"));
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLogixNGTableTable"));
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLogixNGTableTable");        // NOI18N
    }

    @Override
    protected AbstractLogixNGEditor<NamedTable> getEditor(BeanTableFrame<NamedTable> f, BeanTableDataModel<NamedTable> m, String sName) {
        return new TableEditor(m, sName);
    }

    @Override
    protected Manager<NamedTable> getManager() {
        return InstanceManager.getDefault(NamedTableManager.class);
    }

    @Override
    protected void enableAll(boolean enable) {
        // Not used by the tables table
    }

    @Override
    protected void setEnabled(NamedTable bean, boolean enable) {
        // Not used by the tables table
    }

    @Override
    protected boolean isEnabled(NamedTable bean) {
        return true;
    }

    @Override
    protected NamedTable createBean(String userName) {
        String systemName = InstanceManager.getDefault(NamedTableManager.class).getAutoSystemName();
        return createBean(systemName, userName);
    }

    @Override
    protected NamedTable createBean(String systemName, String userName) {
        if (_typeExternalTable.isSelected()) {
            String fileName = _csvFileName.getText();
            if (fileName == null || fileName.isEmpty()) {
                JOptionPane.showMessageDialog(addLogixNGFrame,
                        Bundle.getMessage("LogixNGError2"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return InstanceManager.getDefault(NamedTableManager.class)
                    .newCSVTable(systemName, userName, fileName, CSVFormat.Predefined.TDF);
        } else if (_typeInternalTable.isSelected()) {
            // Open table editor
        } else {
            log.error("No table type selected");
            throw new RuntimeException("No table type selected");
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void deleteBean(NamedTable bean) {
        try {
            InstanceManager.getDefault(NamedTableManager.class).deleteBean(bean, "DoDelete");
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error(e.getMessage());
        }
    }

    @Override
    protected boolean browseMonoSpace() {
        return true;
    }

    @Override
    protected String getBeanText(NamedTable bean) {
        int maxColumnWidth = 0;
        int columnWidth[] = new int[bean.numColumns()+1];
        String[][] cells = new String[bean.numRows()+1][];
        for (int row=0; row <= bean.numRows(); row++) {
            cells[row] = new String[bean.numColumns()+1];
            for (int col=0; col <= bean.numColumns(); col++) {
                Object value = bean.getCell(row, col);
                cells[row][col] = value != null ? value.toString() : "<null>";
                columnWidth[col] = Math.max(columnWidth[col], cells[row][col].length());
                maxColumnWidth = Math.max(maxColumnWidth, columnWidth[col]);
            }
        }
        StringBuilder columnLine = new StringBuilder();
        while (columnLine.length()+2 < maxColumnWidth) {
            columnLine.append("----------------------");
        }
        String columnPadding = String.format("%"+Integer.toString(maxColumnWidth)+"s", "");
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int col=0; col <= bean.numColumns(); col++) {
            sb.append(columnLine.substring(0,columnWidth[col]+2));
            sb.append("+");
            if (col == bean.numColumns()) sb.append(String.format("%n"));
        }
        for (int row=0; row <= bean.numRows(); row++) {
            sb.append("|");
            for (int col=0; col <= bean.numColumns(); col++) {
                sb.append(" ");
                sb.append((cells[row][col]+columnPadding).substring(0,columnWidth[col]));
                sb.append(" |");
                if (col == bean.numColumns()) sb.append(String.format("%n"));
            }
            sb.append("+");
            for (int col=0; col <= bean.numColumns(); col++) {
                sb.append(columnLine.substring(0,columnWidth[col]+2));
                sb.append("+");
                if (col == bean.numColumns()) sb.append(String.format("%n"));
            }
        }
        return sb.toString();
    }

    @Override
    protected String getAddTitleKey() {
        return "TitleLogixNGTableTable";
    }

    @Override
    protected String getCreateButtonHintKey() {
        return "LogixNGTableCreateButtonHint";
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixNGTableTable";  // NOI18N
    }

    private JButton createFileChooser() {
        JButton selectFileButton = new JButton("..."); // "File" replaced by ...
        selectFileButton.setMaximumSize(selectFileButton.getPreferredSize());
        selectFileButton.setToolTipText(Bundle.getMessage("LogixNG_FileButtonHint"));  // NOI18N
        selectFileButton.addActionListener((ActionEvent e) -> {
            JFileChooser csvFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
            csvFileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv", "txt")); // NOI18N
            csvFileChooser.rescanCurrentDirectory();
            int retVal = csvFileChooser.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // set selected file location
                try {
                    _csvFileName.setText(FileUtil.getPortableFilename(csvFileChooser.getSelectedFile().getCanonicalPath()));
                } catch (java.io.IOException ex) {
                    log.error("exception setting file location: {}", ex);  // NOI18N
                    _csvFileName.setText("");
                }
            }
        });
        return selectFileButton;
    }

    @Override
    protected boolean letUserSelectTypeToAdd() {
        AtomicBoolean result = new AtomicBoolean(false);
        
        JDialog dialog = new JDialog(this.getFrame(), Bundle.getMessage("LogixNGTableTable_SelectTableType"), true);    // NOI18N
        
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        _buttonGroup.add(_typeExternalTable);
        _buttonGroup.add(_typeInternalTable);
        _buttonGroup.add(_typeInternalTableBasedOfTable);

        _typeExternalTable.setSelected(true);
        _typeInternalTable.setEnabled(false);
        _typeInternalTableBasedOfTable.setEnabled(false);

        contentPane.add(new JLabel(Bundle.getMessage("LogixNGTableTable_SelectTableType")));    // NOI18N

        contentPane.add(_typeExternalTable);
        contentPane.add(_typeInternalTable);
        contentPane.add(_typeInternalTableBasedOfTable);

        JLabel _tableBeanLabel = new JLabel("Table to base on");
        BeanSelectPanel<NamedTable> _tableBeanPanel =
                new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);

        _typeInternalTableBasedOfTable.addChangeListener((evt) -> {
            _tableBeanLabel.setEnabled(_typeInternalTableBasedOfTable.isSelected());
            _tableBeanPanel.getBeanCombo().setEnabled(_typeInternalTableBasedOfTable.isSelected());
            _internalBasedTable_CopyTable.setEnabled(_typeInternalTableBasedOfTable.isSelected());
            _internalBasedTable_SameRowsAndColumns.setEnabled(_typeInternalTableBasedOfTable.isSelected());
            _internalBasedTable_SameRows.setEnabled(_typeInternalTableBasedOfTable.isSelected());
            _internalBasedTable_SameColumns.setEnabled(_typeInternalTableBasedOfTable.isSelected());
        });
        _tableBeanLabel.setEnabled(_typeInternalTableBasedOfTable.isSelected());
        _tableBeanPanel.getBeanCombo().setEnabled(_typeInternalTableBasedOfTable.isSelected());
        _internalBasedTable_CopyTable.setSelected(true);
        _internalBasedTable_CopyTable.setEnabled(_typeInternalTableBasedOfTable.isSelected());
        _internalBasedTable_SameRowsAndColumns.setEnabled(_typeInternalTableBasedOfTable.isSelected());
        _internalBasedTable_SameRows.setEnabled(_typeInternalTableBasedOfTable.isSelected());
        _internalBasedTable_SameColumns.setEnabled(_typeInternalTableBasedOfTable.isSelected());

        JPanel panelInternalBasedTable = new JPanel();
        panelInternalBasedTable.setLayout(new BoxLayout(panelInternalBasedTable, BoxLayout.Y_AXIS));
        panelInternalBasedTable.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));

        JPanel panelInternalBasedTable_SelectTable = new JPanel();
        panelInternalBasedTable_SelectTable.add(_tableBeanLabel);
        _tableBeanLabel.setLabelFor(_tableBeanPanel);
        panelInternalBasedTable_SelectTable.add(_tableBeanPanel);
        panelInternalBasedTable.add(panelInternalBasedTable_SelectTable);

        _internalBasedTable_ButtonGroup.add(_internalBasedTable_CopyTable);
        _internalBasedTable_ButtonGroup.add(_internalBasedTable_SameRowsAndColumns);
        _internalBasedTable_ButtonGroup.add(_internalBasedTable_SameRows);
        _internalBasedTable_ButtonGroup.add(_internalBasedTable_SameColumns);
        panelInternalBasedTable.add(_internalBasedTable_CopyTable);
        panelInternalBasedTable.add(_internalBasedTable_SameRowsAndColumns);
        panelInternalBasedTable.add(_internalBasedTable_SameRows);
        panelInternalBasedTable.add(_internalBasedTable_SameColumns);
        contentPane.add(panelInternalBasedTable);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((evt) -> {
            dialog.dispose();
        });
        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N
        
        // OK
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        panel5.add(ok);
        ok.addActionListener((evt) -> {
            _newTableType = null;
            if (_typeExternalTable.isSelected()) _newTableType = NewTableType.External;
            if (_typeInternalTable.isSelected()) _newTableType = NewTableType.Internal;
            if (_typeInternalTableBasedOfTable.isSelected()) _newTableType = NewTableType.InternalBasedOfTable;
            result.set(true);
            dialog.dispose();
        });
//        ok.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
            }
        });
        contentPane.add(panel5);
        
        dialog.pack();
        dialog.setVisible(true);
        
        if (result.get() && (addLogixNGFrame != null)) {
            addLogixNGFrame.dispose();
            addLogixNGFrame = null;
        }
        
        return result.get();
    }

    /**
     * Create or copy bean frame.
     *
     * @param titleId   property key to fetch as title of the frame (using Bundle)
     * @param startMessageId part 1 of property key to fetch as user instruction on
     *                  pane, either 1 or 2 is added to form the whole key
     * @return the button JPanel
     */
    @Override
    protected JPanel makeAddFrame(String titleId, String startMessageId) {
        addLogixNGFrame = new JmriJFrame(Bundle.getMessage(titleId));
        addLogixNGFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixNGTableTable", true);     // NOI18N
        addLogixNGFrame.setLocation(50, 30);
        Container contentPane = addLogixNGFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;

        p.add(_sysNameLabel, c);
        _sysNameLabel.setLabelFor(_systemName);
        c.gridy = 1;
        p.add(_userNameLabel, c);
        _userNameLabel.setLabelFor(_addUserName);
        c.gridy = 2;
        p.add(new JLabel(Bundle.getMessage("LogixNG_CsvFileName")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_systemName, c);
        c.gridy = 1;
        p.add(_addUserName, c);

        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(_autoSystemName, c);

        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_allowWrite, c);

        if (_newTableType == NewTableType.External) {
            _allowWrite.setSelected(false);
            _allowWrite.setEnabled(true);

            c.gridx = 1;
            c.gridy = 3;
            createFileChooser();
            p.add(createFileChooser(), c);

            c.gridx = 2;        // make room for file selector
            c.gridwidth = GridBagConstraints.REMAINDER;
            p.add(_csvFileName, c);
        } else if (_newTableType == NewTableType.Internal) {
            // For this table, allow write is always enabled
            _allowWrite.setSelected(true);
            _allowWrite.setEnabled(false);

            c.gridx = 0;
            c.gridy = 3;
            c.weightx = 0.0;
            c.anchor = GridBagConstraints.EAST;
            c.fill = 0;

            JLabel _numRowsLabel = new JLabel("Num rows, exluding header");
            JLabel _numColumnsLabel = new JLabel("Num columns, exluding header");
            JTextField _numRows = new JTextField(5);
            JTextField _numColumns = new JTextField(5);

            p.add(_numRowsLabel, c);
            _numRowsLabel.setLabelFor(_numRows);
            c.gridy = 4;
            p.add(_numColumnsLabel, c);
            _numColumnsLabel.setLabelFor(_numColumns);

            c.gridx = 1;
            c.gridy = 3;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = 0;
            p.add(_numRows, c);
            c.gridy = 4;
            p.add(_numColumns, c);

            c.gridx = 1;
            c.gridy = 5;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 0.0;
            c.fill = 0;
            p.add(_storeTableDataInPanelFile, c);
            _storeTableDataInPanelFile.setSelected(false);
        } else if (_newTableType == NewTableType.InternalBasedOfTable) {
            _allowWrite.setSelected(true);
            _allowWrite.setEnabled(true);

            c.gridx = 1;
            c.gridy = 3;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 0.0;
            c.fill = 0;
            p.add(_storeTableDataInPanelFile, c);
            _storeTableDataInPanelFile.setSelected(false);

            c.gridx = 0;
            c.gridy = 4;
            c.weightx = 0.0;
            c.anchor = GridBagConstraints.EAST;
            c.fill = 0;

            JLabel _tableBeanLabel = new JLabel("Table to base on");
            BeanSelectPanel<NamedTable> _tableBeanPanel =
                    new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);

            p.add(_tableBeanLabel, c);
            _tableBeanLabel.setLabelFor(_tableBeanPanel);

            c.gridx = 1;
            c.gridy = 4;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 0.0;
            c.fill = 0;
            p.add(_tableBeanPanel, c);
        } else {
            throw new RuntimeException("Invalid table type: "+_newTableType.name());
        }

/*
        _buttonGroup.add(_typeExternalTable);
        _buttonGroup.add(_typeInternalTable);
        _typeExternalTable.setSelected(true);
        _typeInternalTable.setEnabled(false);
*/
        _addUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint"));    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixNGSystemNameHint"));   // NOI18N
        contentPane.add(p);
/*
        JPanel panel98 = new JPanel();
        panel98.setLayout(new FlowLayout());
        JPanel panel99 = new JPanel();
        panel99.setLayout(new BoxLayout(panel99, BoxLayout.Y_AXIS));
        panel99.add(_typeExternalTable, c);
        panel99.add(_typeInternalTable, c);
        panel98.add(panel99);
        contentPane.add(panel98);
*/
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGTableMessage1"));  // NOI18N
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGTableMessage2"));  // NOI18N
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(this::cancelAddPressed);
        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        addLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });
        contentPane.add(panel5);

        _autoSystemName.addItemListener((ItemEvent e) -> {
            autoSystemName();
        });
        return panel5;
    }

    @Override
    protected void getListenerRefsIncludingChildren(NamedTable table, java.util.List<String> list) {
        // Do nothing
    }

    @Override
    protected boolean hasChildren(NamedTable table) {
        // Tables doesn't have children
        return false;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTableTableAction.class);

}
