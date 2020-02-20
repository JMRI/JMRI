package jmri.jmrix.can.cbus.swing.simulator;

import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.simulator.CbusEventResponder;
import jmri.util.swing.ComboBoxToolTipRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 * <p>
 * Methods are subject to change and should not be relied on at present.
 *
 * @author Steve Young Copyright (C) 2018 2019
 * @since 4.15.2
 */
public class EvResponderPane extends JPanel {
    
    private final CbusEventResponder _evr;
    private JComboBox<String> _selectMode;
    private int _mode;
    private ArrayList<String> tooltips;
    private JSpinner _spinner;
    
    public EvResponderPane( CbusEventResponder evr) {
        super();
        
        _evr = evr;
        if ( _evr != null ){
            init();
        }
        
    }
    
    private void init() {
        
        _mode = _evr.getMode();
        
        JLabel _nodeLabel = new JLabel("<html><h3>" + Bundle.getMessage("CbusNode") + " : </h3></html>");
        _nodeLabel.setToolTipText(Bundle.getMessage("simNodeSelect"));
    
        _selectMode = new JComboBox<>();
        _selectMode.setEditable(false);
        
        ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();
        _selectMode.setRenderer(renderer);
    
        tooltips = new ArrayList<>();
        String getSelected="";
        
        for (int i = 0; i < _evr.evModes.size(); i++) {
            String option = _evr.evModes.get(i);
            _selectMode.addItem(option);
            tooltips.add(_evr.evModesTip.get(i));
            if ( i == _mode ){
                getSelected = option;
            }
        }
        
        _selectMode.setSelectedItem(getSelected);
        _selectMode.addActionListener ((ActionEvent e) -> {
            String chosen = (String)_selectMode.getSelectedItem();
            
            for (int i = 0; i < _evr.evModes.size(); i++) {
                String option = _evr.evModes.get(i);
                if (option.equals(chosen)) {
                    log.debug("chosen {} {}",i,chosen);
                    _evr.setMode(i);
                }
            }
        });
        renderer.setTooltips(tooltips);
    
        _spinner = new JSpinner(new SpinnerNumberModel(_evr.getNode(), -1, 65535, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(_spinner, "#");
        _spinner.setEditor(editor);
        JFormattedTextField field = (JFormattedTextField) editor.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        _spinner.addChangeListener((ChangeEvent e) -> {
            int minmax = (Integer) _spinner.getValue();
            log.debug("value {}",minmax);
            _evr.setNode(minmax);
        });
        _spinner.setToolTipText(Bundle.getMessage("simNodeSelect"));
    
        DirectionPane dp = new DirectionPane(_evr);
    
        JPanel topPane = new JPanel();
        topPane.add(_selectMode);
        topPane.add(_nodeLabel);
        topPane.add(_spinner);
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
    
        add(topPane);
        add(dp);

    }
    
    private final static Logger log = LoggerFactory.getLogger(EvResponderPane.class);

}
