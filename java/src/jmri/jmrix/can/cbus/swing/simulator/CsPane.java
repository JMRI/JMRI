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
import jmri.jmrix.can.cbus.simulator.CbusDummyCS;
import jmri.util.swing.ComboBoxToolTipRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for viewing and setting simulated network objects.
 * @see SimulatorPane
 * @author Steve Young Copyright (C) 2018 2019
 * @since 4.15.2
 */
public class CsPane extends JPanel {
    
    private CbusDummyCS _cs;
    private JComboBox<String> _selectCs;
    private int _id;
    private int _type;
    private int _numSessions;
    private JButton _resetCs;
    private JLabel _sessionText;
    private ArrayList<String> tooltips;
    
    public CsPane(CbusDummyCS cs ) {
        super();
        _cs=cs;
        if ( _cs != null ) {
            init();
        }
    }
        
    private void init() {
        _type = _cs.getDummyType();
        _numSessions=_cs.getNumberSessions();
        _sessionText = new JLabel();
        _sessionText.setToolTipText(Bundle.getMessage("ActiveSess"));
        
        _selectCs = new JComboBox<String>();
        _selectCs.setEditable(false);
        
        ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();
        _selectCs.setRenderer(renderer);
        
        updateSessionTotal();
        
        _cs.setPane(this);
        tooltips = new ArrayList<String>();
        String getSelected="";
        
        for (int i = 0; i < _cs.csTypes.size(); i++) {
            String option = _cs.csTypes.get(i);
            _selectCs.addItem(option);
            tooltips.add(_cs.csTypesTip.get(i));
            if ( i == _type ){
                getSelected = option;
            }
        }
        
        _selectCs.setSelectedItem(getSelected);
        _selectCs.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                String chosen = (String)_selectCs.getSelectedItem();
                
                for (int i = 0; i < _cs.csTypes.size(); i++) {
                    String option = _cs.csTypes.get(i);
                    if (option.equals(chosen)) {
                        log.debug("chosen {} {}",i,chosen);
                        _cs.setDummyType(i);
                    }
                }
            }
        });
        renderer.setTooltips(tooltips);
        
        _resetCs = new JButton(Bundle.getMessage("Reset"));
        
        JPanel topPane = new JPanel();
        topPane.add(_selectCs);
        topPane.add(_sessionText);
        topPane.add(_resetCs);

        DirectionPane dp = new DirectionPane(_cs);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        
        add(topPane);
        add(dp);
        
        _resetCs.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                _cs.resetCS();
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

    private final static Logger log = LoggerFactory.getLogger(CsPane.class);
}
