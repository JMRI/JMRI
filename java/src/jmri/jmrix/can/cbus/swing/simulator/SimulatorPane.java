package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;
import javax.swing.Timer;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusCommandStation;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 * <p>
 * Methods are subject to change and should not be relied on at present.
 *
 * @author Steve Young Copyright (C) 2018
 * @since 4.15.1
 */
public class SimulatorPane extends jmri.jmrix.can.swing.CanPanel {

    private CbusCommandStation cs;
    private CbusSimulator _sim;

    private JPanel p1;
    private JPanel _csPanes;
    private JPanel _ndPanes;
    private JPanel _evPanes;
    private Timer _initTimer;
    
    private JScrollPane mainScroll;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        cs = (CbusCommandStation) memo.get(jmri.CommandStation.class);
        cs.startNetworkSim();
        _sim = cs.getNetworkSim();
        
        if ( _sim == null ) {
            log.debug("No Sim Manager started");
            // actual sim may be initializing so we'll wait for it
            _initTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ( _sim != null ) {
                        _initTimer.stop();
                        _initTimer = null;
                        init();
                    }
                }
            });
            _initTimer.setRepeats(true);
            _initTimer.start();
        } else {
            init();
        }
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemNetworkSim"));
        }
        return Bundle.getMessage("MenuItemNetworkSim");
    }

    public SimulatorPane() {
        super();
    }
    
    public void init() {
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel directionOptions = new JPanel();
        directionOptions.setLayout(new BoxLayout(directionOptions, BoxLayout.Y_AXIS));
        
        JPanel processOptions = new JPanel();
        JPanel sendOptions = new JPanel();
        
        JCheckBox processIn = new JCheckBox(Bundle.getMessage("processIn"));
        JCheckBox processOut = new JCheckBox(Bundle.getMessage("processOut"));
        JCheckBox sendIn = new JCheckBox(Bundle.getMessage("sendIn"));
        JCheckBox sendOut = new JCheckBox(Bundle.getMessage("sendOut"));
        
        processIn.setToolTipText(Bundle.getMessage("processInTip"));
        processOut.setToolTipText(Bundle.getMessage("processOutTip"));
        sendIn.setToolTipText(Bundle.getMessage("sendInTip"));
        sendOut.setToolTipText(Bundle.getMessage("sendOutTip"));
        
        processOut.setSelected(_sim.getProcessOut());
        processIn.setSelected(_sim.getProcessIn());
        sendOut.setSelected(_sim.getSendOut());
        sendIn.setSelected(_sim.getSendIn());
        
        processIn.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _sim.setProcessIn(processIn.isSelected());
            }
        }); 

        processOut.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _sim.setProcessOut(processOut.isSelected());
            }
        });

        sendIn.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _sim.setSendIn(sendIn.isSelected());
            }
        });

        sendOut.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _sim.setSendOut(sendOut.isSelected());
            }
        });

        processOptions.add(processOut);
        processOptions.add(sendIn);
        
        sendOptions.add(processIn);
        sendOptions.add(sendOut);
        
        directionOptions.add(processOptions);
        directionOptions.add(sendOptions);        
        
        p1 = new JPanel();        
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        
        _csPanes = new JPanel();
        _evPanes = new JPanel();
        _ndPanes = new JPanel();
        _csPanes.setLayout(new BoxLayout(_csPanes, BoxLayout.Y_AXIS));
        _evPanes.setLayout(new BoxLayout(_evPanes, BoxLayout.Y_AXIS));
        _ndPanes.setLayout(new BoxLayout(_ndPanes, BoxLayout.Y_AXIS));

        directionOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DirSettings")));
        _csPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CmndStations")));
        _evPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ResponseEvents")));
        _ndPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CbusNodes")));
        
        for ( int i=0 ; ( i < _sim.getNumCS() ) ; i++ ) {
            CsPane thispane = new CsPane(i,1); // id , type
            _csPanes.add(thispane);
            thispane.setVisible(true);
        }

        NdPane thispanend = new NdPane(1,0); // id , type
        _ndPanes.add(thispanend);
        
        EvResponderPane thispane = new EvResponderPane(0,1); // id , mode
        _evPanes.add(thispane);

        thispane.setVisible(true);
        _csPanes.setVisible(true);
        
        p1.add(directionOptions);
        p1.add(_csPanes);
        p1.add(_evPanes);
        p1.add(_ndPanes);
        
        mainScroll = new JScrollPane (p1);
        this.add(mainScroll);
    }
    
    
    public SimulatorPane getCSPane(){
        return this;
    }
    
    /**
     * Creates a Menu List
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        JMenu addMenu = new JMenu(Bundle.getMessage("MenuAdd"));
        
        JMenuItem newCs = new JMenuItem(Bundle.getMessage("CommandStation"));
        newCs.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                CsPane thispane = new CsPane(_sim.getNewCSID(0),0);
                _csPanes.add(thispane);
                revalidate();
            }
        });

        JMenuItem newEv = new JMenuItem(Bundle.getMessage("EventResponders"));
        newEv.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                EvResponderPane thispane = new EvResponderPane(_sim.getNewEvID(0),0);
                _evPanes.add(thispane);
                revalidate();
            }
        });

        JMenuItem newNd = new JMenuItem(Bundle.getMessage("CbusNode"));
        newNd.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                NdPane thispanend = new NdPane(_sim.getNewNdID(0),0);
                _ndPanes.add(thispanend);
                revalidate();
            }
        });

        addMenu.add(newCs);
        addMenu.add(newEv);
        addMenu.add(newNd);
        menuList.add(addMenu);
        
        return menuList;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.simulator.SimulatorPane";
    }
    
    public class CsPane extends JPanel {
        
        private JComboBox<String> _selectCs;
        private int _id;
        private int _type;
        private int _numSessions;
        private JPanel _singleCs;
        private JButton _resetCs;
        private JLabel _sessionText;
        private ArrayList<String> tooltips;
        
        public CsPane(int id, int type ) {
            super();
            
            _id = id;
            _type = type;
            _numSessions=0;
            _singleCs = new JPanel();
            _sessionText = new JLabel();
            _sessionText.setToolTipText(Bundle.getMessage("ActiveSess"));
            
            _selectCs = new JComboBox<String>();
            _selectCs.setEditable(false);
            
            ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
            _selectCs.setRenderer(renderer);
            
            updateSessionTotal();
            
            _sim.getCSFromId(_id).setPane(this);
            tooltips = new ArrayList<String>();
            String getSelected="";
            
            for (int i = 0; i < CbusSimulator.csTypes.size(); i++) {
                String option = CbusSimulator.csTypes.get(i);
                _selectCs.addItem(option);
                tooltips.add(CbusSimulator.csTypesTip.get(i));
                if ( i == _type ){
                    getSelected = option;
                }
            }
            
            _selectCs.setSelectedItem(getSelected);
            _selectCs.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String chosen = (String)_selectCs.getSelectedItem();
                    
                    for (int i = 0; i < CbusSimulator.csTypes.size(); i++) {
                        String option = CbusSimulator.csTypes.get(i);
                        if (option.equals(chosen)) {
                            log.debug("chosen {} {}",i,chosen);
                            _sim.getCSFromId(_id).setDummyType(i);
                        }
                    }
                }
            });

            renderer.setTooltips(tooltips);
            
            _resetCs = new JButton(Bundle.getMessage("Reset"));
            
            _singleCs.add(_selectCs);
            
            _singleCs.add(_sessionText);
            _singleCs.add(_resetCs);

            _singleCs.setBorder(BorderFactory.createEtchedBorder());
            _csPanes.add(_singleCs);
            
            _resetCs.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _sim.resetCs(_id);
                }
            });
        }
        
        void updateSessionTotal(){
            _sessionText.setText("<html> <h2> " + _numSessions + " </h2> </html>");
        }
        
        public void setNumSessions(int num){
            _numSessions=num;
            updateSessionTotal();
        }
        
        public int getId(){
            return _id;
        }
        
    }

    public class NdPane extends JPanel {
        
        private JComboBox<String> _selectNd;
        private int _id;
        private int _type;
        private int _nn;
        private JPanel _singleNd;
        private JButton _resetNd;
        private JLabel _sessionText;
        private ArrayList<String> tooltips;
        
        public NdPane(int id, int type ) {
            super();
            
            _id = id;
            _type = type;
            _nn = 0;
            _singleNd = new JPanel();
            _sessionText = new JLabel();
            
            _selectNd = new JComboBox<String>();
            _selectNd.setEditable(false);
            
            ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
            _selectNd.setRenderer(renderer);
            
            log.debug("id {} sim {}",_id,_sim);
            _sim.getNodeFromId(_id).setPane(this);
            
            tooltips = new ArrayList<String>();
            String getSelected="";
            
            for (int i = 0; i < CbusSimulator.ndTypes.size(); i++) {
                int intoption = CbusSimulator.ndTypes.get(i);
                String option = CbusOpCodes.getModuleType(165,intoption);
                _selectNd.addItem(option);
                tooltips.add(CbusOpCodes.getModuleTypeExtra(165,intoption));
                if ( intoption == _type ){
                    getSelected = option;
                }
            }
            
            _selectNd.setSelectedItem(getSelected);
            _selectNd.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String chosen = (String)_selectNd.getSelectedItem();
                    
                    for (int i = 0; i < CbusSimulator.ndTypes.size(); i++) {
                        int intoption = CbusSimulator.ndTypes.get(i);
                        String option = CbusOpCodes.getModuleType(165,intoption);
                        if (option.equals(chosen)) {
                            log.debug("chosen {} {}",i,chosen);
                            _sim.getNodeFromId(_id).setDummyType(intoption);
                            _type = intoption;
                        }
                    }
                    updateNode();
                }
            });

            renderer.setTooltips(tooltips);
            
            _resetNd = new JButton("FLiM");
            
            _singleNd.add(_selectNd);
            
            _singleNd.add(_sessionText);
            _singleNd.add(_resetNd);

            _singleNd.setBorder(BorderFactory.createEtchedBorder());
            _ndPanes.add(_singleNd);
            
            _resetNd.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _sim.resetNd(_id);
                }
            });
            
            updateNode();
        }
        
        private void updateNode(){
            if ( _type>0 ) { 
                _resetNd.setEnabled(true); 
            } else {
                _resetNd.setEnabled(false); 
            }
           _sessionText.setText("<html> <h2> " + _nn + " </h2> </html>");
        }
        
        public void setNodeNum(int num){
            _nn=num;
            updateNode();
        }
        
        public int getId(){
            return _id;
        }
        
    }

    public class EvResponderPane extends JPanel {
        
        private JComboBox<String> _selectMode;
        private int _id;
        private int _mode;
        private ArrayList<String> tooltips;
        private JSpinner _spinner;
        
        public EvResponderPane(int id, int mode ) {
            super();
            
            _id = id;
            _mode = mode;
            
            JLabel _nodeLabel = new JLabel("<html><h3>" + Bundle.getMessage("CbusNode") + " : </h3></html>");
            _nodeLabel.setToolTipText(Bundle.getMessage("simNodeSelect"));
        
            _selectMode = new JComboBox<String>();
            _selectMode.setEditable(false);
            
            ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
            _selectMode.setRenderer(renderer);
        
            tooltips = new ArrayList<String>();
            String getSelected="";
            
            for (int i = 0; i < CbusSimulator.evModes.size(); i++) {
                String option = CbusSimulator.evModes.get(i);
                _selectMode.addItem(option);
                tooltips.add(CbusSimulator.evModesTip.get(i));
                if ( i == _mode ){
                    getSelected = option;
                }
            }
            
            _selectMode.setSelectedItem(getSelected);
            _selectMode.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String chosen = (String)_selectMode.getSelectedItem();
                    
                    for (int i = 0; i < CbusSimulator.evModes.size(); i++) {
                        String option = CbusSimulator.evModes.get(i);
                        if (option.equals(chosen)) {
                            log.debug("chosen {} {}",i,chosen);
                            _sim.getEvRFromId(_id).setMode(i);
                        }
                    }
                }
            });
        
            renderer.setTooltips(tooltips);
        
            _spinner = new JSpinner(new SpinnerNumberModel(-1, -1, 65535, 1));
            JComponent comp = _spinner.getEditor();
            JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
            DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
            formatter.setCommitsOnValidEdit(true);
            _spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int minmax = (Integer) _spinner.getValue();
                    log.debug("value {}",minmax);
                    _sim.getEvRFromId(_id).setNode(minmax);
                }
            });
            _spinner.setToolTipText(Bundle.getMessage("simNodeSelect"));
        
            add(_selectMode);
            add(_nodeLabel);
            add(_spinner);
        
            setBorder(BorderFactory.createEtchedBorder());
            
            _evPanes.add(this);
        
        }
    }

    static public class ComboboxToolTipRenderer extends DefaultListCellRenderer {
        List<String> tooltips;
    
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                            int index, boolean isSelected, boolean cellHasFocus) {
    
            JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
    
            if (-1 < index && null != value && null != tooltips) {
                list.setToolTipText(tooltips.get(index));
            }
            return comp;
        }
    
        public void setTooltips(List<String> tooltips) {
            this.tooltips = tooltips;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemNetworkSim"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    SimulatorPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SimulatorPane.class);
}
