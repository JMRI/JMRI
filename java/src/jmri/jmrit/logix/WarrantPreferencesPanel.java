package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.implementation.SignalSpeedMap;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Pete Cressman Copyright (C) 2015
 */
@ServiceProvider(service = PreferencesPanel.class)
public class WarrantPreferencesPanel extends JPanel implements PreferencesPanel, ItemListener {

    static int STRUT_SIZE = 10;

    private boolean _isDirty = false;

    private JComboBox<ScaleData> _layoutScales;
    private JTextField _searchDepth;
    private JTextField _timeIncre;
    private JTextField _rampIncre;
    private JTextField _throttleScale;
    private int _interpretation = SignalSpeedMap.PERCENT_NORMAL;
    private ArrayList<DataPair<String, Float>> _speedNameMap;
    private SpeedNameTableModel _speedNameModel;
    private JTable _speedNameTable;
    private ArrayList<DataPair<String, String>> _appearanceMap;
    private AppearanceTableModel _appearanceModel;
    private JTable _appearanceTable;
    private ArrayList<DataPair<String, Integer>> _stepIncrementMap;

    public WarrantPreferencesPanel() {
        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//        add(new JTitledSeparator(Bundle.getMessage("TitleWarrantPreferences")));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        leftPanel.add(layoutScalePanel());
        leftPanel.add(searchDepthPanel(true));
        leftPanel.add(timeIncrementPanel(true));
        leftPanel.add(throttleIncrementPanel(true));
        leftPanel.add(throttleScalePanel(true));
        rightPanel.add(speedNamesPanel());
        rightPanel.add(Box.createGlue());
        rightPanel.add(interpretationPanel());
        rightPanel.add(Box.createGlue());
        rightPanel.add(appearancePanel());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(leftPanel);
        panel.add(rightPanel);
        add(panel);
    }

    private JPanel layoutScalePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        _layoutScales = new JComboBox<ScaleData>();
        _layoutScales.addItem(new ScaleData("G", 20.3f));
        _layoutScales.addItem(new ScaleData("L", 38f));
        _layoutScales.addItem(new ScaleData("O", 43f));
        _layoutScales.addItem(new ScaleData("S", 64f));
        _layoutScales.addItem(new ScaleData("OO", 76.2f));
        _layoutScales.addItem(new ScaleData("HO", 87.1f));
        _layoutScales.addItem(new ScaleData("TT", 120f));
        _layoutScales.addItem(new ScaleData("N", 160f));
        _layoutScales.addItem(new ScaleData("Z", 220f));
        _layoutScales.addItem(new ScaleData("T", 480f));
        ScaleData sc = makeCustomItem(WarrantPreferences.getDefault().getLayoutScale());
        _layoutScales.addItem(sc);
        if (_layoutScales.getSelectedIndex() < 0) {
            _layoutScales.setSelectedItem(sc);
        }
        Dimension dim = _layoutScales.getPreferredSize();
        dim.width = 3 * dim.width / 2;
        _layoutScales.setPreferredSize(dim);
        _layoutScales.addItemListener(this);
        _layoutScales.setToolTipText(Bundle.getMessage("ToolTipLayoutScale"));
        JLabel label = new JLabel(Bundle.getMessage("LabelLayoutScale"));
        label.setToolTipText(Bundle.getMessage("ToolTipLayoutScale"));
        panel.add(label);
        JPanel p = new JPanel();
        p.add(_layoutScales);
//        p.add(Box.createVerticalGlue());
        panel.add(p);
        return panel;
    }

    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "fixed number of possible values")
    private ScaleData makeCustomItem(float scale) {
        int cnt = 0;
        while (cnt < _layoutScales.getItemCount()) {
            if (_layoutScales.getItemAt(cnt).scale == scale) {
                _layoutScales.setSelectedItem(_layoutScales.getItemAt(cnt));
                return new CustomDialog("custom", 0.0f);
            }
            cnt++;
        }
        _layoutScales.setSelectedIndex(-1);
        return new CustomDialog(Bundle.getMessage("custom"), scale);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        WarrantPreferences preferences = WarrantPreferences.getDefault();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ScaleData sd = (ScaleData) e.getItem();
            if (sd instanceof CustomDialog) {
                boolean ok = false;
                while (!ok) {
                    float scale = 0.0f;
                    String str = JOptionPane.showInputDialog(this, Bundle.getMessage("customInput"),
                            Bundle.getMessage("customTitle"), JOptionPane.QUESTION_MESSAGE);
                    try {
                        if (str == null) {
                            sd.scale = 0.0f;
                            makeCustomItem(preferences.getLayoutScale());
                            ok = true;
                        } else {
                            scale = Float.parseFloat(str);
                            if (scale <= 1.0f) {
                                throw new NumberFormatException();
                            }
                            sd.scale = scale;
                            preferences.setLayoutScale(scale);
                            _isDirty = true;
                            ok = true;
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("customError", str),
                                Bundle.getMessage("customTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                preferences.setLayoutScale(sd.scale);
                _isDirty = true;
            }
        }
    }

    class ScaleData {

        float scale;
        String scaleName;

        ScaleData(String scaleName, float scale) {
            this.scale = scale;
            this.scaleName = scaleName;
        }

        @Override
        public String toString() {
            return Bundle.getMessage("scaleItem", scaleName, Float.toString(scale));
        }
    }

    class CustomDialog extends ScaleData {

        CustomDialog(String scaleName, float scale) {
            super(scaleName, scale);
        }

        @Override
        public String toString() {
            if (scale < 1.0f) {
                return Bundle.getMessage("custom");
            }
            return super.toString();
        }
    }

    private JPanel searchDepthPanel(boolean vertical) {
        _searchDepth = new JTextField(5);
        _searchDepth.setText(Integer.toString(WarrantPreferences.getDefault().getSearchDepth()));
        JPanel p = new JPanel();
        p.add(WarrantRoute.makeTextBoxPanel(vertical, _searchDepth, "SearchDepth", "ToolTipSearchDepth"));
        _searchDepth.setColumns(5);
        p.setToolTipText(Bundle.getMessage("ToolTipSearchDepth"));
        return p;
    }

    private JPanel throttleScalePanel(boolean vertical) {
        _throttleScale = new JTextField(5);
        _throttleScale.setText(Float.toString(WarrantPreferences.getDefault().getThrottleScale()));
        JPanel p = new JPanel();
        p.add(WarrantFrame.makeTextBoxPanel(vertical, _throttleScale, "ThrottleScale", "ToolTipThrottleScale"));
        _throttleScale.setColumns(8);
        p.setToolTipText(Bundle.getMessage("ToolTipThrottleScale"));
        return p;
    }

    private JPanel speedNamesPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("LabelSpeedNameTable"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        _speedNameMap = new ArrayList<>();
        Iterator<Entry<String, Float>> it = WarrantPreferences.getDefault().getSpeedNameEntryIterator();
        while (it.hasNext()) {
            Entry<String, Float> ent = it.next();
            _speedNameMap.add(new DataPair<>(ent.getKey(), ent.getValue()));
        }
        _speedNameModel = new SpeedNameTableModel();
        _speedNameTable = new JTable(_speedNameModel);
        for (int i = 0; i < _speedNameModel.getColumnCount(); i++) {
            int width = _speedNameModel.getPreferredWidth(i);
            _speedNameTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        ActionListener insertAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertSpeedNameRow();
            }
        };
        ActionListener deleteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSpeedNameRow();
            }
        };
        panel.add(tablePanel(_speedNameTable, "ToolTipSpeedNameTable", insertAction, deleteAction));
        return panel;
    }

    private JPanel appearancePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("LabelAppearanceTable"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        _appearanceMap = new ArrayList<>();
        Iterator<Entry<String, String>> it = WarrantPreferences.getDefault().getAppearanceEntryIterator();
        while (it.hasNext()) {
            Entry<String, String> ent = it.next();
            _appearanceMap.add(new DataPair<>(ent.getKey(), ent.getValue()));
        }
        _appearanceModel = new AppearanceTableModel();
        _appearanceTable = new JTable(_appearanceModel);
        for (int i = 0; i < _appearanceModel.getColumnCount(); i++) {
            int width = _appearanceModel.getPreferredWidth(i);
            _appearanceTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        panel.add(tablePanel(_appearanceTable, "ToolTipAppearanceTable", null, null));
        return panel;
    }

    static private JPanel tablePanel(JTable table, String toolTip, ActionListener insertAction, ActionListener removeAction) {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.LINE_AXIS));
        JScrollPane scrollPane = new JScrollPane(table);
        int height = table.getRowHeight();
        Dimension dim = table.getPreferredSize();
        dim.height = height * 5;
        scrollPane.getViewport().setPreferredSize(dim);
        table.setToolTipText(Bundle.getMessage(toolTip));
        scrollPane.setToolTipText(Bundle.getMessage(toolTip));
        tablePanel.add(scrollPane);
        tablePanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

        if (insertAction != null) {
            JButton insertButton = new JButton(Bundle.getMessage("buttonInsertRow"));
            insertButton.addActionListener(insertAction);
            buttonPanel.add(insertButton);
            buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        }

        if (removeAction != null) {
            JButton deleteButton = new JButton(Bundle.getMessage("buttonDeleteRow"));
            deleteButton.addActionListener(removeAction);
            buttonPanel.add(deleteButton);
        }
        tablePanel.add(buttonPanel);
        return tablePanel;
    }

    private void insertSpeedNameRow() {
        int row = _speedNameTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _speedNameMap.add(row, new DataPair<String, Float>("", 0f));
        _speedNameModel.fireTableDataChanged();
    }

    private void deleteSpeedNameRow() {
        int row = _speedNameTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _speedNameMap.remove(row);
        _speedNameModel.fireTableDataChanged();
    }

    private JPanel interpretationPanel() {
        _interpretation = WarrantPreferences.getDefault().getInterpretation();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        ButtonGroup group = new ButtonGroup();
        makeButton(buttonPanel, group, "percentNormal", "ToolTipPercentNormal", SignalSpeedMap.PERCENT_NORMAL);
        makeButton(buttonPanel, group, "percentThrottle", "ToolTipPercentThrottle", SignalSpeedMap.PERCENT_THROTTLE);
        makeButton(buttonPanel, group, "speedMph", "ToolTipSpeedMph", SignalSpeedMap.SPEED_MPH);
        makeButton(buttonPanel, group, "speedKmph", "ToolTipSpeedKmph", SignalSpeedMap.SPEED_KMPH);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JPanel p = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("LabelInterpretation"));
        label.setToolTipText(Bundle.getMessage("ToolTipInterpretation"));
        p.setToolTipText(Bundle.getMessage("ToolTipInterpretation"));
        p.add(label);
        panel.add(p);
        panel.add(buttonPanel, Box.CENTER_ALIGNMENT);
        return panel;
    }

    private void makeButton(JPanel panel, ButtonGroup group, String name, String tooltip, int interp) {
        JRadioButton button = new JRadioButton(Bundle.getMessage(name));
        group.add(button);
        panel.add(button);
        button.setToolTipText(Bundle.getMessage(tooltip));
        button.addActionListener(new ActionListener() {
            int value;
            JRadioButton but;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (but.isSelected()) {
                    _interpretation = value;
                }
            }

            ActionListener init(JRadioButton b, int num) {
                but = b;
                value = num;
                return this;
            }
        }.init(button, interp));
        if (_interpretation == interp) {
            button.setSelected(true);
        }
    }

    private JPanel timeIncrementPanel(boolean vertical) {
        _timeIncre = new JTextField(5);
        _timeIncre.setText(Integer.toString(WarrantPreferences.getDefault().getTimeIncrement()));
        JPanel p = new JPanel();
        p.add(WarrantFrame.makeTextBoxPanel(vertical, _timeIncre, "TimeIncrement", "ToolTipTimeIncrement"));
        p.setToolTipText(Bundle.getMessage("ToolTipTimeIncrement"));
        return p;
    }

    private JPanel throttleIncrementPanel(boolean vertical) {
        _rampIncre = new JTextField(5);
        _rampIncre.setText(Float.toString(WarrantPreferences.getDefault().getThrottleIncrement()));
        JPanel p = new JPanel();
        p.add(WarrantFrame.makeTextBoxPanel(vertical, _rampIncre, "RampIncrement", "ToolTipRampIncrement"));
        p.setToolTipText(Bundle.getMessage("ToolTipRampIncrement"));
        _rampIncre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = _rampIncre.getText();
                boolean showdialog = false;
                try {
                    float incr = Float.parseFloat(text);
                    showdialog = (incr < 0.002f || incr > 0.2f);
                } catch (NumberFormatException nfe) {
                    showdialog = true;
                }
                if (showdialog) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("rampIncrWarning", text),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        return p;
    }

    /* alternative UI test
    private JPanel throttleIncrementPanel(boolean vertical) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Ramp step amount"));
        JPanel p = new JPanel();
        p.add(new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(1,1,20,1 )));
        p.add(new JLabel(" steps, in"));
        panel.add(p);
        p = new JPanel();
        String[] modes = {"14", "28", "128"};
        p.add(new JComboBox<String>(modes));
        p.add(new JLabel(" stepmode"));
        panel.add(p);
        return panel;
    }*/

    /**
     * Compare GUI values with Preferences. When different, update Preferences
     * and set _isDirty flag.
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "fixed number of possible values")
    private void setValues() {
        WarrantPreferences preferences = WarrantPreferences.getDefault();
        int depth = preferences.getSearchDepth();
        try {
            depth = Integer.parseInt(_searchDepth.getText());
        } catch (NumberFormatException nfe) {
            _searchDepth.setText(Integer.toString(preferences.getSearchDepth()));
        }
        if (preferences.getSearchDepth() != depth) {
            preferences.setSearchDepth(depth);
            _isDirty = true;
        }

        if (preferences.getInterpretation() != _interpretation) {
            preferences.setInterpretation(_interpretation);
            _isDirty = true;
        }

        int time = preferences.getTimeIncrement();
        try {
            time = Integer.parseInt(_timeIncre.getText());
            if (time < 200) {
                time = 200;
                JOptionPane.showMessageDialog(null, Bundle.getMessage("timeWarning"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                _timeIncre.setText(Integer.toString(preferences.getTimeIncrement()));
            }
        } catch (NumberFormatException nfe) {
            _timeIncre.setText(Integer.toString(preferences.getTimeIncrement()));
        }
        if (preferences.getTimeIncrement() != time) {
            preferences.setTimeIncrement(time);
            _isDirty = true;
        }

        float scale = preferences.getThrottleIncrement();
        try {
            scale = Float.parseFloat(_rampIncre.getText());
        } catch (NumberFormatException nfe) {
            _rampIncre.setText(Float.toString(preferences.getThrottleIncrement()));
        }
        if (preferences.getThrottleIncrement() != scale) {
            preferences.setThrottleIncrement(scale);
            _isDirty = true;
        }

        scale = preferences.getThrottleScale();
        try {
            scale = Float.parseFloat(_throttleScale.getText());
        } catch (NumberFormatException nfe) {
            _throttleScale.setText(Float.toString(preferences.getThrottleScale()));
        }
        if (preferences.getThrottleScale() != scale) {
            preferences.setThrottleScale(scale);
            _isDirty = true;
        }

        boolean different = false;
        javax.swing.table.TableCellEditor tce = _speedNameTable.getCellEditor();
        if (tce != null) {
            tce.stopCellEditing();
        }
        if (preferences.getSpeedNamesSize() != _speedNameMap.size()) {
            different = true;
        } else {
            for (int i = 0; i < _speedNameMap.size(); i++) {
                DataPair<String, Float> dp = _speedNameMap.get(i);
                String name = dp.getKey();
                if (preferences.getSpeedNameValue(name) == null
                        || preferences.getSpeedNameValue(name).floatValue() != dp.getValue().floatValue()) {
                    different = true;
                    break;
                }
            }
        }
        if (different) {
            preferences.setSpeedNames(_speedNameMap);
            _isDirty = true;
        }

        different = false;
        tce = _appearanceTable.getCellEditor();
        if (tce != null) {
            tce.stopCellEditing();
        }
        if (preferences.getAppeaancesSize() != _appearanceMap.size()) {
            different = true;
        } else {
            for (int i = 0; i < _appearanceMap.size(); i++) {
                DataPair<String, String> dp = _appearanceMap.get(i);
                String name = dp.getKey();
                if (preferences.getAppearanceValue(name) == null || !preferences.getAppearanceValue(name).equals(dp.getValue())) {
                    different = true;
                    break;
                }
            }
        }
        if (different) {
            preferences.setAppearances(_appearanceMap);
            _isDirty = true;
        }
    }

    @Override
    public String getPreferencesItem() {
        return "WARRANTS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("TitleWarrantPreferences");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return Bundle.getMessage("ToolTipLayoutScale");
    }

    @Override
    public void savePreferences() {
        setValues();
        if (_isDirty) {
            WarrantPreferences.getDefault().apply();
            WarrantPreferences.getDefault().save();
            _isDirty = false;
        }
    }

    @Override
    public boolean isDirty() {
        return this._isDirty;
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    /**
     * Retain the key/value pair of a Map or Dictionary as a pair.
     * 
     * @param <K> key class
     * @param <V> value class
     */
    // Can uses of DataPair be replaced with used of Map.Entry or AbstractMap.SimpleEntry?
    static class DataPair<K, V> implements Entry<K, V> {

        K key;
        V value;

        DataPair(K k, V v) {
            key = k;
            value = v;
        }

        DataPair(Entry<K, V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }
        
        @Override
        public K getKey() {
            return key;
        }

        void setKey(K k) {
            key = k;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V v) {
            value = v;
            return value;
        }

    }

    /* *********************** SpeedName Table ******************************/
    class SpeedNameTableModel extends AbstractTableModel {

        public SpeedNameTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return _speedNameMap.size();
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return Bundle.getMessage("speedName");
            }
            return Bundle.getMessage("speedValue");
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            if (col == 0) {
                return new JTextField(15).getPreferredSize().width;
            }
            return new JTextField(8).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _speedNameMap.size()) {
                log.error("row is greater than aspect speedNames size");
                return "";
            }
            DataPair<String, Float> data = _speedNameMap.get(row);
            if (data == null) {
                log.error("Aspect speedName data is null!");
                return "";
            }
            if (col == 0) {
                return data.getKey();
            }
            return data.getValue();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, Float> data = _speedNameMap.get(row);
            String str = (String) value;
            String msg = null;
            if (str == null || data == null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data != null) {
                if (col == 0) {
                    data.setKey((String) value);
                } else {
                    try {
                        float f = Float.parseFloat((String) value);
                        if (f < 0) {
                            msg = Bundle.getMessage("InvalidNumber", (String) value);
                        } else {
                            data.setValue(f);
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("MustBeFloat", (String) value);
                    }
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    /* *********************** appearance Table ******************************/
    class AppearanceTableModel extends AbstractTableModel {

        public AppearanceTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return _appearanceMap.size();
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return Bundle.getMessage("appearance");
            }
            return Bundle.getMessage("speedName");
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col != 0);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            if (col == 0) {
                return new JTextField(15).getPreferredSize().width;
            }
            return new JTextField(15).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _appearanceMap.size()) {
                log.error("row is greater than appearance names size");
                return "";
            }
            DataPair<String, String> data = _appearanceMap.get(row);
            if (data == null) {
                log.error("Appearance name data is null!");
                return "";
            }
            if (col == 0) {
                return data.getKey();
            }
            return data.getValue();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, String> data = _appearanceMap.get(row);
            String str = (String) value;
            String msg = null;
            if (str == null || data == null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data != null) {
                if (col == 0) {
                    data.setKey((String) value);
                } else {
                    data.setValue((String) value);
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    /* *********************** Throttle Step Increment Table *****************************/
    class StepIncrementTableModel extends AbstractTableModel {

        public StepIncrementTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return _stepIncrementMap.size();
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return Bundle.getMessage("throttleStepMode");
            }
            return Bundle.getMessage("rampIncrement");
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col != 0);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            if (col == 0) {
                return new JTextField(15).getPreferredSize().width;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _stepIncrementMap.size()) {
                log.error("row is greater than throttle step modes size");
                return "";
            }
            DataPair<String, Integer> data = _stepIncrementMap.get(row);
            if (data == null) {
                log.error("Throttle step data is null!");
                return "";
            }
            if (col == 0) {
                return data.getKey();
            }
            return data.getValue();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, Integer> data = _stepIncrementMap.get(row);
            String str = (String) value;
            String msg = null;
            if (str == null || data == null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data != null) {
                if (col == 0) {
                    data.setKey((String) value);
                } else {
                    try {
                        Integer f = Integer.parseInt((String) value);
                        if (f < 1) {
                            msg = Bundle.getMessage("InvalidNumber", (String) value);
                        } else {
                            data.setValue(f);
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("InvalidNumber", (String) value);
                    }
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantPreferencesPanel.class.getName());
}
