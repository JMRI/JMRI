package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.*;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeCanListener;
import jmri.jmrix.can.cbus.simulator.*;
import jmri.util.swing.ComboBoxToolTipRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 * <p>
 * Methods are subject to change and should not be relied on at present.
 *
 * @author Steve Young Copyright (C) 2019
 * @since 4.15.2
 */
public class NdPane extends JPanel {
    
    private JComboBox<String> _selectNd;
    private CbusDummyNode _node;
    private JButton _flimButton;
    private JLabel _sessionText;
    private DirectionPane directionPane;
    private final CanSystemConnectionMemo _memo;
    
    public NdPane(CbusDummyNode nd, CanSystemConnectionMemo sysmemo ) {
        super();
        _node = nd;
        _memo = sysmemo;
        init();
    }
        
    private void init() {

        _sessionText = new JLabel();

        _selectNd = new JComboBox<>();
        _selectNd.setEditable(false);

        ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();
        _selectNd.setRenderer(renderer);

        ArrayList<String> tooltips = new ArrayList<>();

        _selectNd.addItem("None");
        tooltips.add("Select a module to start the Simulation.");

        CbusSimulatedModuleProvider.getInstancesCollection().forEach(module -> {
            log.debug("found SPI {}", module.getModuleType());
            _selectNd.addItem(module.getModuleType());
            tooltips.add(module.getToolTipText());
            if ( module.matchesManuAndModuleId(_node) ) {
                _selectNd.setSelectedItem(module.getModuleType());
            }
        });

        renderer.setTooltips(tooltips);
        _selectNd.addActionListener(this::moduleSelectorChanged);

        _flimButton = new JButton("FLiM"); // NOI18N
        _flimButton.addActionListener ((ActionEvent e) -> {
            _node.flimButton();
        });

        JPanel topPane = new JPanel();
        topPane.add(_selectNd);
        topPane.add(_sessionText);
        topPane.add(_flimButton);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        add(topPane);

        directionPane = new DirectionPane(null);
        add( directionPane );

        updateNodeGui();
    }

    private void moduleSelectorChanged(ActionEvent e) {

        String chosen = (String)_selectNd.getSelectedItem();
        log.debug("Selected module {} {}",chosen,e);
        
        CbusSimulator sim = jmri.InstanceManager.getNullableDefault(CbusSimulator.class);

        if (_node != null) {
            // todo - use memo, not instancemanager
            if ( sim != null ) {
                sim.removeNode(_node);
            } else {
                log.warn("No Simulator Running to deregister Node {}",_node);
            }
            _node.dispose();
            _node = null;
        }

        CbusSimulatedModuleProvider providerNode = CbusSimulatedModuleProvider.getProviderByName(chosen);
        if ( providerNode != null ) {
            _node = providerNode.getNewDummyNode( _memo, 0);

            // todo - use memo, not instancemanager
            if ( sim != null ) {
                sim.addNode(_node);
            } else {
                log.warn("No Simulator Running to register Node {}",_node);
            }
        }
        updateNodeGui();
    }

    public void updateNodeGui(){
        _flimButton.setEnabled( _node != null );
        if ( _node != null ) {
            _node.setPane(this); // todo - move to property lkistener for node number changes
            
            CbusNodeCanListener ncl = _node.getCanListener();
            if ( ncl instanceof CbusSimCanListener ) {
                directionPane.setSimCanListener( (CbusSimCanListener) ncl);
            }
           _sessionText.setText("<html> <h2> " + _node.getNodeNumber() + " </h2> </html>");
        } else {
            directionPane.setSimCanListener(null);
            _sessionText.setText("<html> <h2>   </h2> </html>");
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(NdPane.class);
    
}
