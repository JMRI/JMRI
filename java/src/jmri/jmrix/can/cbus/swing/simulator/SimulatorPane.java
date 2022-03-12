package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 *
 * @see CsPane
 * @see EvResponderPane
 * @see NdPane
 * @see DirectionPane
 * @author Steve Young Copyright (C) 2018 2019
 * @since 4.15.2
 */
public class SimulatorPane extends jmri.jmrix.can.swing.CanPanel {

    private CbusSimulator _sim;

    private JPanel p1;
    private JPanel _csPanes;
    private JPanel _ndPanes;
    private JPanel _evPanes;
    private Boolean _disposeSimOnWindowClose = false;
    private JScrollPane mainScroll;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        // todo - run on memo, not instance
        _sim = jmri.InstanceManager.getNullableDefault(CbusSimulator.class);
         
        if ( _sim == null ) {
            jmri.util.ThreadingUtil.runOnLayout( ()->{
                _sim = new CbusSimulator(memo);
            });
        }
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemNetworkSim"));
    }

    public SimulatorPane() {
        super();
    }
    
    private void init() {
        
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
            NdPane thispanend = new NdPane(_sim.getNd(i), memo); // id , type
            _ndPanes.add(thispanend);
            thispanend.setVisible(true);
        }
        // and add an Empty pane
        NdPane thispanend = new NdPane(null, memo); // id , type
        _ndPanes.add(thispanend);
        thispanend.setVisible(true);
        
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
        List<JMenu> menuList = new ArrayList<>();
        JMenu optionsMenu = new JMenu(Bundle.getMessage("OptionsMenu"));
        JMenu addMenu = new JMenu(Bundle.getMessage("MenuAdd"));
        
        JCheckBoxMenuItem closeSimOnDispose = new JCheckBoxMenuItem(Bundle.getMessage("StopSimWinClose"));
        closeSimOnDispose.setSelected(false);
        closeSimOnDispose.addActionListener ((ActionEvent e) -> {
            _disposeSimOnWindowClose = closeSimOnDispose.isSelected();
        });
        
        JMenuItem newCs = new JMenuItem(Bundle.getMessage("CommandStation"));
        newCs.addActionListener ((ActionEvent e) -> {
            CsPane thispane = new CsPane(_sim.getNewCS());
            _csPanes.add(thispane);
            revalidate();
        });

        JMenuItem newEv = new JMenuItem(Bundle.getMessage("EventResponders"));
        newEv.addActionListener ((ActionEvent e) -> {
            EvResponderPane thispane = new EvResponderPane(_sim.getNewEv());
            _evPanes.add(thispane);
            revalidate();
        });

        JMenuItem newNd = new JMenuItem(Bundle.getMessage("CbusNode"));
        newNd.addActionListener ((ActionEvent e) -> {
            NdPane thispanend = new NdPane( null, memo);
            _ndPanes.add(thispanend);
            revalidate();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        if ( _disposeSimOnWindowClose ) {
            _sim.dispose();
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
    // private final static Logger log = LoggerFactory.getLogger(SimulatorPane.class);
}
