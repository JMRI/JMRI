package jmri.jmrix.can.cbus.swing.simulator;

import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.swing.simulator.DirectionPane;
import jmri.jmrix.can.cbus.swing.simulator.SimulatorPane;

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
    private CbusDummyNode _cs;
    private int _type;
    private int _nn;
    private JButton _resetNd;
    private JLabel _sessionText;
    private ArrayList<String> tooltips;
    
    public NdPane(CbusDummyNode cs ) {
        super();
        _cs = cs;
        init();
    }
    
    public NdPane() {
        super();
    }
        
    private void init() {

        _type = _cs.getDummyType();
        _nn = 0;

        _sessionText = new JLabel();
        
        _selectNd = new JComboBox<String>();
        _selectNd.setEditable(false);
        
        SimulatorPane.ComboboxToolTipRenderer renderer = new SimulatorPane.ComboboxToolTipRenderer();
        _selectNd.setRenderer(renderer);
        
        _cs.setPane(this);
        
        tooltips = new ArrayList<String>();
        String getSelected="";
        
        for (int i = 0; i < CbusDummyNode.ndTypes.size(); i++) {
            int intoption = CbusDummyNode.ndTypes.get(i);
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
                
                for (int i = 0; i < CbusDummyNode.ndTypes.size(); i++) {
                    int intoption = CbusDummyNode.ndTypes.get(i);
                    String option = CbusOpCodes.getModuleType(165,intoption);
                    if (option.equals(chosen)) {
                        log.debug("chosen {} {}",i,chosen);
                        _cs.setDummyType(intoption);
                        _type = intoption;
                    }
                }
                updateNode();
            }
        });

        renderer.setTooltips(tooltips);
        
        _resetNd = new JButton("FLiM");
        
        DirectionPane dp = new DirectionPane(_cs);
        
        JPanel topPane = new JPanel();
        
        topPane.add(_selectNd);
        
        topPane.add(_sessionText);
        topPane.add(_resetNd);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        
        add(topPane);
        add(dp);
        
        _resetNd.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _cs.flimButton();
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
    
    private final static Logger log = LoggerFactory.getLogger(SimulatorPane.class);
    
}