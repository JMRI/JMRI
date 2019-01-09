package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusCommandStation;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.cbus.swing.simulator.CsPane;
import jmri.jmrix.can.cbus.swing.simulator.EvResponderPane;
import jmri.jmrix.can.cbus.swing.simulator.NdPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 * <p>
 * @see CsPane
 * @see EvResponderPane
 * @see NdPane
 * @see DirectionPane
 * @author Steve Young Copyright (C) 2018 2019
 * @since 4.15.2
 */
public class SimulatorPane extends jmri.jmrix.can.swing.CanPanel {

    private CbusCommandStation cs;
    private CbusSimulator _sim;

    private JPanel p1;
    private JPanel _csPanes;
    private JPanel _ndPanes;
    private JPanel _evPanes;
    private Timer _initTimer;
    private Boolean _disposeSimOnWindowClose;
    private JScrollPane mainScroll;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        cs = (CbusCommandStation) memo.get(jmri.CommandStation.class);
        _sim = cs.getNetworkSim();
        
        if ( _sim == null ) {
            log.info("No CBUS Simulation Tools are currently started");
            // actual sim may be initializing so we'll wait for it
            _initTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ( _sim != null ) {
                        _initTimer.stop();
                        _initTimer = null;
                        init();
                    }
                log.info("Waiting for CBUS Simulation Tools to start");
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
        
        _disposeSimOnWindowClose=false;
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        p1 = new JPanel();        
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        
        _csPanes = new JPanel();
        _evPanes = new JPanel();
        _ndPanes = new JPanel();
        _csPanes.setLayout(new BoxLayout(_csPanes, BoxLayout.Y_AXIS));
        _evPanes.setLayout(new BoxLayout(_evPanes, BoxLayout.Y_AXIS));
        _ndPanes.setLayout(new BoxLayout(_ndPanes, BoxLayout.Y_AXIS));

        _csPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CmndStations")));
        _evPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ResponseEvents")));
        _ndPanes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CbusNodes")));
        
        for ( int i=0 ; ( i < _sim.getNumCS() ) ; i++ ) {
            CsPane thispane = new CsPane(_sim.getCS(i)); // id , type
            _csPanes.add(thispane);
            thispane.setVisible(true);
        }

        for ( int i=0 ; ( i < _sim.getNumNd() ) ; i++ ) {
            NdPane thispanend = new NdPane(_sim.getNd(i)); // id , type
            _ndPanes.add(thispanend);
            thispanend.setVisible(true);
        }
        
        for ( int i=0 ; ( i < _sim.getNumEv() ) ; i++ ) {
            EvResponderPane thispane = new EvResponderPane(_sim.getEv(i)); // id , mode
            _evPanes.add(thispane);
            thispane.setVisible(true);
        }
        
        _csPanes.setVisible(true);
        _evPanes.setVisible(true);
        _ndPanes.setVisible(true);        

        p1.add(_csPanes);
        p1.add(_ndPanes);
        p1.add(_evPanes);

        mainScroll = new JScrollPane (p1);
        this.add(mainScroll);
    }
    
    /**
     * Creates a Menu List
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        JMenu optionsMenu = new JMenu(Bundle.getMessage("OptionsMenu"));
        JMenu addMenu = new JMenu(Bundle.getMessage("MenuAdd"));
        
        JCheckBoxMenuItem closeSimOnDispose = new JCheckBoxMenuItem(Bundle.getMessage("StopSimWinClose"));
        closeSimOnDispose.setSelected(false);
        closeSimOnDispose.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _disposeSimOnWindowClose = closeSimOnDispose.isSelected();
            }
        });
        
        JMenuItem newCs = new JMenuItem(Bundle.getMessage("CommandStation"));
        newCs.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                CsPane thispane = new CsPane(_sim.getNewCS());
                _csPanes.add(thispane);
                revalidate();
            }
        });

        JMenuItem newEv = new JMenuItem(Bundle.getMessage("EventResponders"));
        newEv.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                EvResponderPane thispane = new EvResponderPane(_sim.getNewEv());
                _evPanes.add(thispane);
                revalidate();
            }
        });

        JMenuItem newNd = new JMenuItem(Bundle.getMessage("CbusNode"));
        newNd.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                NdPane thispanend = new NdPane(_sim.getNewNd());
                _ndPanes.add(thispanend);
                revalidate();
            }
        });


        optionsMenu.add(closeSimOnDispose);
        addMenu.add(newCs);
        addMenu.add(newEv);
        addMenu.add(newNd);
        
        menuList.add(optionsMenu);
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
        if ( _disposeSimOnWindowClose ) {
            cs.disposeNetworkSim();
        }
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
