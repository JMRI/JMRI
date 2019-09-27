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
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
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
    private JButton _resetNd;
    private JLabel _sessionText;
    private ArrayList<String> tooltips;
    
    public NdPane(CbusDummyNode nd ) {
        super();
        _node = nd;
        init();
    }
    
    public NdPane() {
        super();
    }
        
    private void init() {

        _sessionText = new JLabel();
        
        _selectNd = new JComboBox<String>();
        _selectNd.setEditable(false);
        
        ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();
        _selectNd.setRenderer(renderer);
        
        _node.setPane(this);
        
        tooltips = new ArrayList<String>();
        String getSelected="";
        
        for (int i = 0; i < CbusDummyNode.ndTypes.size(); i++) {
            int intoption = CbusDummyNode.ndTypes.get(i);
            String option = CbusNodeConstants.getModuleType(165,intoption);
            _selectNd.addItem(option);
            tooltips.add(CbusNodeConstants.getModuleTypeExtra(165,intoption));
            if ( intoption == _node.getParameter(3) ){ // module type
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
                    String option = CbusNodeConstants.getModuleType(165,intoption);
                    if (option.equals(chosen)) {
                        log.debug("chosen {} {}",i,chosen);
                        _node.setDummyType(165,intoption);
                    }
                }
                updateNode();
            }
        });

        renderer.setTooltips(tooltips);
        
        _resetNd = new JButton("FLiM");
        
        DirectionPane dp = new DirectionPane(_node);
        
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
                _node.flimButton();
            }
        });
        
        updateNode();
    }
    
    private void updateNode(){
        if ( _node.getParameter(3)>0 ) { // module type set
            _resetNd.setEnabled(true); 
        } else {
            _resetNd.setEnabled(false); 
        }
       _sessionText.setText("<html> <h2> " + _node.getNodeNumber() + " </h2> </html>");
    }
    
    public void setNodeNum(int num){
        _node.setNodeNumber(num);
        updateNode();
    }
    
    private final static Logger log = LoggerFactory.getLogger(SimulatorPane.class);
    
}