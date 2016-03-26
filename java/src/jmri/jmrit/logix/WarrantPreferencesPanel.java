package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
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
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright (C) 2015
 */
public class WarrantPreferencesPanel extends JPanel implements PreferencesPanel, ItemListener {
    
    private static final long serialVersionUID = 7088050123933847146L;
    static int STRUT_SIZE = 10;
    
    private WarrantPreferences _preferences;
    private boolean _isDirty = false;

    private JComboBox<ScaleData> _layoutScales;
    private JTextField  _searchDepth;
    private JTextField  _timeIncre;
    private JTextField  _rampIncre;
    private JTextField  _throttleScale;
    private int _interpretation = SignalSpeedMap.PERCENT_NORMAL;
    private ArrayList<DataPair<String, Float>> _speedNameMap;
    private SpeedNameTableModel _speedNameModel;
    private JTable  _speedNameTable;
    private ArrayList<DataPair<String, String>> _appearanceMap;
    private AppearanceTableModel _appearanceModel;
    private JTable  _appearanceTable;
    private ArrayList<DataPair<String, Integer>> _stepIncrementMap;

    public WarrantPreferencesPanel() {
        if (jmri.InstanceManager.getDefault(WarrantPreferences.class) == null) {
            InstanceManager.store(new WarrantPreferences(jmri.util.FileUtil.getProfilePath() +
                    "signal" + File.separator + "WarrantPreferences.xml"), WarrantPreferences.class);
        }
        _preferences = InstanceManager.getDefault(WarrantPreferences.class);
        //  set local prefs to match instance prefs
        //preferences.apply(WiThrottleManager.withrottlePreferencesInstance());
        initGUI();
        setGUI();
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
        add(applyPanel());
    }

    private void setGUI() {
        _preferences.apply();
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
        ScaleData sc = makeCustomItem(_preferences.getScale());
        _layoutScales.addItem(sc);
        if (_layoutScales.getSelectedIndex()<0) {
            _layoutScales.setSelectedItem(sc);          
        }
        Dimension dim = _layoutScales.getPreferredSize();
        dim.width = 3*dim.width/2;
        _layoutScales.setPreferredSize(dim);
        _layoutScales.addItemListener(this);
        _layoutScales.setToolTipText(Bundle.getMessage("ToolTipLayoutScale"));
        JLabel label= new JLabel(Bundle.getMessage("LabelLayoutScale"));
        label.setToolTipText(Bundle.getMessage("ToolTipLayoutScale"));
        panel.add(label);
        JPanel p = new JPanel();
        p.add(_layoutScales);
//        p.add(Box.createVerticalGlue());
        panel.add(p);
        return panel;
    }
    private ScaleData makeCustomItem(float scale) {
        int cnt = 0;
        while (cnt <_layoutScales.getItemCount()) {
            if (_layoutScales.getItemAt(cnt).scale == scale) {
                _layoutScales.setSelectedItem(_layoutScales.getItemAt(cnt));
                return new CustomDialog("custom", 0.0f);
            }
            cnt++;
        }
        _layoutScales.setSelectedIndex(-1);
        return new CustomDialog(Bundle.getMessage("custom"), scale);
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange()==ItemEvent.SELECTED) {
            ScaleData sd = (ScaleData)e.getItem();
            if (sd instanceof CustomDialog) {
                boolean ok = false;
                while (!ok) {
                    float scale = 0.0f;
                    String str = JOptionPane.showInputDialog(this, Bundle.getMessage("customInput"),
                            Bundle.getMessage("customTitle"), JOptionPane.QUESTION_MESSAGE);
                    try {
                        if (str==null) {
                            sd.scale = 0.0f;
                            makeCustomItem(_preferences.getScale());
                            ok = true;
                        } else {
                            scale = Float.parseFloat(str);
                            if (scale <= 1.0f) {
                                throw new NumberFormatException();
                            }
                            sd.scale = scale;
                            _preferences.setScale(scale);
                            _isDirty = true;
                            ok = true;                          
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("customError", str),
                                Bundle.getMessage("customTitle"), JOptionPane.ERROR_MESSAGE);
                    }                   
                }
            } else {
                _preferences.setScale(sd.scale);
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
        _searchDepth =  new JTextField(5);
        _searchDepth.setText(Integer.toString(_preferences.getSearchDepth()));
        JPanel p = new JPanel();
        p.add(WarrantRoute.makeTextBoxPanel(vertical, _searchDepth, "SearchDepth", "ToolTipSearchDepth"));
        _searchDepth.setColumns(5);
        p.setToolTipText(Bundle.getMessage("ToolTipSearchDepth"));
        return p;
    }
    private JPanel throttleScalePanel(boolean vertical) {
        _throttleScale =  new JTextField(5);
        _throttleScale.setText(Float.toString(_preferences.getThrottleScale()));
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
        
        _speedNameMap = new ArrayList<DataPair<String, Float>>();
        Iterator<Entry<String, Float>> it = _preferences.getSpeedNameEntryIterator();
        while (it.hasNext()) {
            Entry<String, Float> ent = it.next();
            _speedNameMap.add(new DataPair<String, Float>(ent.getKey(), ent.getValue())); 
        }
        _speedNameModel = new SpeedNameTableModel();
        _speedNameTable = new JTable(_speedNameModel);
        for (int i=0; i<_speedNameModel.getColumnCount(); i++) {
            int width = _speedNameModel.getPreferredWidth(i);
            _speedNameTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        ActionListener insertAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertSpeedNameRow();
            }
        };
        ActionListener deleteAction = new ActionListener() {
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

        _appearanceMap = new ArrayList<DataPair<String, String>>();
        Iterator<Entry<String, String>> it = _preferences.getAppearanceEntryIterator();
        while (it.hasNext()) {
            Entry<String, String> ent = it.next();
            _appearanceMap.add(new DataPair<String, String>(ent.getKey(), ent.getValue())); 
        }
        _appearanceModel = new AppearanceTableModel();
        _appearanceTable = new JTable(_appearanceModel);
        for (int i=0; i<_appearanceModel.getColumnCount(); i++) {
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
        dim.height = height*5;
        scrollPane.getViewport().setPreferredSize(dim);
        table.setToolTipText(Bundle.getMessage(toolTip));
        scrollPane.setToolTipText(Bundle.getMessage(toolTip));
        tablePanel.add(scrollPane);
        tablePanel.add(Box.createVerticalStrut(STRUT_SIZE));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

        if (insertAction!=null) {
            JButton insertButton =  new JButton(Bundle.getMessage("buttonInsertRow"));
            insertButton.addActionListener(insertAction);
            buttonPanel.add(insertButton);
            buttonPanel.add(Box.createVerticalStrut(2*STRUT_SIZE));         
        }

        if (removeAction!=null) {
            JButton deleteButton =  new JButton(Bundle.getMessage("buttonDeleteRow"));
            deleteButton.addActionListener(removeAction);
            buttonPanel.add(deleteButton);          
        }
        tablePanel.add(buttonPanel);
        return tablePanel;
    }
    private void insertSpeedNameRow() {
        int row = _speedNameTable.getSelectedRow();
        if (row<0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                    
            return;
        }
        _speedNameMap.add(row, new DataPair<String, Float>("", 0f));
        _speedNameModel.fireTableDataChanged();     
    }
    private void deleteSpeedNameRow() {
        int row = _speedNameTable.getSelectedRow();
        if (row<0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                    
            return;
        }
        _speedNameMap.remove(row);
        _speedNameModel.fireTableDataChanged();     
    }
    
    private JPanel interpretationPanel() {
        _interpretation = _preferences.getInterpretation();
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
        JLabel label= new JLabel(Bundle.getMessage("LabelInterpretation"));
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
        if (_interpretation==interp) {
            button.setSelected(true);
        }
    }
    
    private JPanel timeIncrementPanel(boolean vertical) {
        _timeIncre =  new JTextField(5);
        _timeIncre.setText(Integer.toString(_preferences.getTimeIncre()));
        JPanel p = new JPanel();
        p.add(WarrantFrame.makeTextBoxPanel(vertical, _timeIncre, "TimeIncrement", "ToolTipTimeIncrement"));
        p.setToolTipText(Bundle.getMessage("ToolTipTimeIncrement"));
        return p;
    }
    private JPanel throttleIncrementPanel(boolean vertical) {
        _rampIncre =  new JTextField(5);
        _rampIncre.setText(Float.toString(_preferences.getThrottleIncre()));
        JPanel p = new JPanel();
        p.add(WarrantFrame.makeTextBoxPanel(vertical, _rampIncre, "RampIncrement","ToolTipRampIncrement"));
        p.setToolTipText(Bundle.getMessage("ToolTipRampIncrement"));
        _rampIncre.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = _rampIncre.getText();
                boolean showdialog = false;
                try {
                    float incr = Float.parseFloat(text);
                    showdialog = (incr<0.002f || incr>0.2f);
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
     * Compare GUI vaules with Preferences.  When different, update Preferences
     * and set _isDirty flag.
     */
    private void setValues() {
        int depth = _preferences.getSearchDepth();
        try {
             depth =Integer.parseInt(_searchDepth.getText());
        } catch (NumberFormatException nfe) {
            _searchDepth.setText(Integer.toString(_preferences.getSearchDepth()));
        }
        if (_preferences.getSearchDepth() != depth) {
            _preferences.setSearchDepth(depth);
            _isDirty = true;
        }
        
        if (_preferences.getInterpretation()!=_interpretation) {
            _preferences.setInterpretation(_interpretation);
            _isDirty = true;
        }
        
        int time = _preferences.getTimeIncre();
        try {
            time =Integer.parseInt(_timeIncre.getText());
            if (time < 200) {
                time = 200;
                JOptionPane.showMessageDialog(null, Bundle.getMessage("timeWarning"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                    
                _timeIncre.setText(Integer.toString(_preferences.getTimeIncre()));
            }
        } catch (NumberFormatException nfe) {
            _timeIncre.setText(Integer.toString(_preferences.getTimeIncre()));
        }
        if (_preferences.getTimeIncre() != time) {
            _preferences.setTimeIncre(time);
            _isDirty = true;
        }
        
        float scale = _preferences.getThrottleIncre();
        try {
            scale = Float.parseFloat(_rampIncre.getText());
        } catch (NumberFormatException nfe) {
            _rampIncre.setText(Float.toString(_preferences.getThrottleIncre()));
        }
        if (_preferences.getThrottleIncre() != scale) {
            _preferences.setThrottleIncre(scale);
            _isDirty = true;
        }

        scale = _preferences.getThrottleScale();
        try {
            scale = Float.parseFloat(_throttleScale.getText());
        } catch (NumberFormatException nfe) {
            _throttleScale.setText(Float.toString(_preferences.getThrottleScale()));
        }
        if (_preferences.getThrottleScale() != scale) {
            _preferences.setThrottleScale(scale);
            _isDirty = true;
        }

        boolean different = false;
        javax.swing.table.TableCellEditor tce = _speedNameTable.getCellEditor();
        if (tce!=null) {
            tce.stopCellEditing();
        }
        if (_preferences.getSpeedNamesSize() != _speedNameMap.size()) {
            different = true;
        } else {
            for (int i=0; i<_speedNameMap.size(); i++) {
                DataPair<String, Float> dp = _speedNameMap.get(i);
                String name = dp.getKey();
                if (_preferences.getSpeedNameValue(name)==null 
                        || _preferences.getSpeedNameValue(name).floatValue() != dp.getValue().floatValue()) {
                    different = true;
                    break;
                }
            }
        } if (different) {
            _preferences.setSpeedNames(_speedNameMap);
            _isDirty = true;
        }
    
        different = false;
        tce = _appearanceTable.getCellEditor();
        if (tce!=null) {
            tce.stopCellEditing();
        }
        if (_preferences.getAppeaancesSize() != _appearanceMap.size()) {
            different = true;
        } else {
            for (int i=0; i<_appearanceMap.size(); i++) {
                DataPair<String, String> dp = _appearanceMap.get(i);
                String name = dp.getKey();
                if (_preferences.getAppearanceValue(name)==null || !_preferences.getAppearanceValue(name).equals(dp.getValue())) {
                    different = true;
                    break;
                }
            }
        } if (different) {
            _preferences.setAppearances(_appearanceMap);
            _isDirty = true;
        }
    }
    
    private JPanel applyPanel() {
        JPanel panel = new JPanel();
        JButton applyB = new JButton(Bundle.getMessage("ButtonApply"));
        applyB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setValues();
                if (_isDirty) {
                    _preferences.apply();
                }
            }
        });
        panel.add(new JLabel(Bundle.getMessage("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
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
            _preferences.apply();
            _preferences.save();
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

    class DataPair<K, V> {
        K key;
        V value;
        
        DataPair(K k, V v) {
            key = k;
            value = v;          
        }
        K getKey() { return key; }
        void setKey(K k) { key = k; }
        V getValue() { return value; }
        void setValue(V v) { value = v; }
    }
    /************************* SpeedName Table ******************************/
    class SpeedNameTableModel extends AbstractTableModel {
        
        private static final long serialVersionUID = 7088050123933847145L;

        public SpeedNameTableModel() {
            super();
        }
        public int getColumnCount () {
            return 2;
        }
        public int getRowCount() {
            return _speedNameMap.size();
        }
        @Override
        public String getColumnName(int col) {
            if (col==0) {
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
            if (col==0) {
                return new JTextField(15).getPreferredSize().width;             
            }
            return new JTextField(8).getPreferredSize().width;
        }
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _speedNameMap.size()){
                log.error("row is greater than aspect speedNames size");
                return "";
            }
            DataPair<String, Float> data = _speedNameMap.get(row);
            if (data == null){
                log.error("Aspect speedName data is null!");
                return "";
            }
            if (col==0) {
                return data.getKey();            
            }
            return data.getValue();
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, Float> data = _speedNameMap.get(row);
            String str = (String)value;
            String msg = null;
            if (str==null || data==null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data!=null) {
                if (col==0) {
                    data.setKey((String)value);            
                } else {
                    try { 
                        float f = Float.parseFloat((String)value);
                        if (f < 0) {
                            msg = Bundle.getMessage("InvalidNumber", (String)value); 
                        } else {
                            data.setValue(f);                                   
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("MustBeFloat", (String)value); 
                    }
                 }
                if (msg!=null) {
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                    
                } else {
                    fireTableRowsUpdated(row, row);             
                }                
            }
        }
    }
    /************************* appearance Table ******************************/
    class AppearanceTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 7088050123933847144L;

        public AppearanceTableModel() {
            super();
        }
        @Override
        public int getColumnCount () {
            return 2;
        }
        @Override
        public int getRowCount() {
            return _appearanceMap.size();
        }
        @Override
        public String getColumnName(int col) {
            if (col==0) {
                return Bundle.getMessage("appearance");
            }
            return Bundle.getMessage("speedName");
        }
        @Override
        public boolean isCellEditable(int row, int col) {
            return (col!=0);
        }
        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }
        public int getPreferredWidth(int col) {
            if (col==0) {
                return new JTextField(15).getPreferredSize().width;             
            }
            return new JTextField(15).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _appearanceMap.size()){
                log.error("row is greater than appearance names size");
                return "";
            }
            DataPair<String, String> data = _appearanceMap.get(row);
            if (data == null){
                log.error("Appearance name data is null!");
                return "";
            }
            if (col==0) {
                return data.getKey();            
            }
            return data.getValue();
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, String> data = _appearanceMap.get(row);
            String str = (String)value;
            String msg = null;
            if (str==null || data==null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data!=null) {
                if (col==0) {
                    data.setKey((String)value);            
                } else {
                    data.setValue((String)value);                                   
                }
                if (msg!=null) {
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                    
                } else {
                    fireTableRowsUpdated(row, row);             
                }                
            }
        }
    }
    /************************* Throttle Step Increment Table ******************************/
    class StepIncrementTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 7088050123933847143L;

        public StepIncrementTableModel() {
            super();
        }
        @Override
        public int getColumnCount () {
            return 2;
        }
        @Override
        public int getRowCount() {
            return _stepIncrementMap.size();
        }
        @Override
        public String getColumnName(int col) {
            if (col==0) {
                return Bundle.getMessage("throttleStepMode");
            }
            return Bundle.getMessage("rampIncrement");
        }
        @Override
        public boolean isCellEditable(int row, int col) {
            return (col!=0);
        }
        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            if (col==0) {
                return new JTextField(15).getPreferredSize().width;             
            }
            return new JTextField(5).getPreferredSize().width;
        }
        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _stepIncrementMap.size()){
                log.error("row is greater than throttle step modes size");
                return "";
            }
            DataPair<String, Integer> data = _stepIncrementMap.get(row);
            if (data == null){
                log.error("Throttle step data is null!");
                return "";
            }
            if (col==0) {
                return data.getKey();            
            }
            return data.getValue();
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
            DataPair<String, Integer> data = _stepIncrementMap.get(row);
            String str = (String)value;
            String msg = null;
            if (str==null || data==null) {
                msg = Bundle.getMessage("NoData");
            }
            if (data!=null) {
                if (col==0) {
                    data.setKey((String)value);            
                } else {
                    try { 
                        Integer f = Integer.parseInt((String)value);
                        if (f < 1) {
                            msg = Bundle.getMessage("InvalidNumber", (String)value); 
                        } else {
                            data.setValue(f);                                   
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("InvalidNumber", (String)value); 
                    }
                 }
                if (msg!=null) {
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
