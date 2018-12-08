package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrix.can.cbus.CbusFilter;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates Panel for displaying a single filter
 * @author Steve Young Copyright (C) 2018
 */
public class CbusFilterPanel extends JPanel {
    
    private int _index;
    private CbusFilterFrame _filterFrame;
    private String _textLabel;
    private Boolean _catHead;
    private int _countFilter;
    private int _countPass;
    private int _category;
    private JLabel fLabel;
    private JPanel evPane;
    private Boolean _available;
    private JSpinner spinner;
    private String _buttonText = Bundle.getMessage("ButtonPass");
    
    private Color greenish = new Color(110, 235, 131);
    private Color redish = new Color(255, 132, 84);
    private Color amberish = new Color(228, 255, 26);

    // Buttons to enable/disable filters
    protected JToggleButton enableButton;
    protected JToggleButton catButton;

    /**
     * Create a new instance of CbusFilterPanel.
     */
    public CbusFilterPanel(Boolean available, CbusFilterFrame filterFrame, int index, String textLabel, Boolean catHead, int category) {
        super();
        _index = index;
        _filterFrame = filterFrame;
        _textLabel = textLabel;
        _countFilter = 0;
        _countPass = 0;
        _category = category;
        _catHead = catHead;
        _available = available;
        initComponents();
    }

    protected CbusFilterPanel() {
        super();
    }

    public void initComponents() {
        // log.debug("init components");
        
        double _iconScale = 0.25;
        
        NamedIcon collapsed = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        collapsed.scale(_iconScale,this);
        NamedIcon showing = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        showing.scale(_iconScale,this);
        showing.setRotation(3, this);

        this.setLayout(new GridLayout(1,1,30,30)); // row, col, hgap, vgap

        // Pane to hold Event
        evPane = new JPanel();
        evPane.setBackground(Color.white);
        
        catButton = new JToggleButton();
        fLabel = new JLabel(_textLabel,SwingConstants.RIGHT);
        if (_catHead){
            evPane.setLayout(new GridLayout(1,2,0,10));
            catButton.setBackground(Color.white);
            catButton.setText(_textLabel);
           // catButton.setPreferredSize(new Dimension(200, 30));
            catButton.setIcon(collapsed);
            catButton.setHorizontalAlignment(SwingConstants.RIGHT);
            catButton.setHorizontalTextPosition(JLabel.LEFT);
            catButton.setFocusPainted(false);
            catButton.setBorderPainted(false);
            evPane.add(catButton);
            
            catButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (catButton.isSelected()) {
                        // log.warn("selected category index {}",_index);
                        catButton.setIcon(showing);
                    } else {
                        // log.warn("deselected category index {} ",_index);
                        catButton.setIcon(collapsed);
                    }
                    ThreadingUtil.runOnGUIEventually( ()->{   
                        _filterFrame.showFiltersChanged(_index,catButton.isSelected(),_category);
                    });
                }
            });
            
        }
        else {
            evPane.setLayout(new GridLayout(1,2,40,10));
        
            if ( _index == CbusFilter.CFEVENTMIN ||
                    _index == CbusFilter.CFEVENTMAX ||
                    _index == CbusFilter.CFNODEMIN ||
                    _index == CbusFilter.CFNODEMAX
                ) {
                JPanel spinnerAndButton = new JPanel();
                spinnerAndButton.setLayout(new GridLayout(1,2,0,0));
                spinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
                JComponent comp = spinner.getEditor();
                JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
                DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
                formatter.setCommitsOnValidEdit(true);
                spinner.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        int minmax = (Integer) spinner.getValue();
                        if (_index == CbusFilter.CFEVENTMIN){
                            _filterFrame.minEvChanged(minmax);
                            _filterFrame.updateListeners(Bundle.getMessage("MinEventSet",minmax));
                        }
                        else if (_index == CbusFilter.CFEVENTMAX){
                            _filterFrame.maxEvChanged(minmax);
                            _filterFrame.updateListeners(Bundle.getMessage("MaxEventSet",minmax));
                        }
                        else if (_index == CbusFilter.CFNODEMIN){
                            _filterFrame.minNdChanged(minmax);
                            _filterFrame.updateListeners(Bundle.getMessage("MinNodeSet",minmax));
                        }
                        else if (_index == CbusFilter.CFNODEMAX){
                            _filterFrame.maxNdChanged(minmax);
                            _filterFrame.updateListeners(Bundle.getMessage("MaxNodeSet",minmax));
                        }
                    }
                });
                
                spinnerAndButton.setBackground(Color.white);
                spinnerAndButton.add(spinner);
                spinnerAndButton.add(fLabel);
                
                evPane.add(spinnerAndButton);
            
            } else {
                evPane.add(fLabel);
            }
        }
        enableButton = new JToggleButton();
        enableButton.setUI(new BasicToggleButtonUI()); //Removes selectColor
        enableButton.setText( newTextString() );
        enableButton.setBackground(greenish);
        enableButton.setFocusPainted(false);
        enableButton.setToolTipText(Bundle.getMessage("ButtonPassTip"));
        evPane.add(enableButton);
        evPane.setVisible(true);
        this.add(evPane);
        
        // connect actions to buttons
        enableButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetenableButton();
                ThreadingUtil.runOnGUIEventually( ()->{ 
                    _filterFrame.checkBoxChanged(_index,enableButton.isSelected(),_category,_catHead);
                    StringBuilder txt = new StringBuilder();
                    txt.append(_textLabel);
                    txt.append(": ");
                    txt.append(_buttonText);
                    if (_catHead){
                        txt.append(" ");
                        txt.append(Bundle.getMessage("All"));
                    }
                    _filterFrame.updateListeners(txt.toString());
                });
            }
        });
        
        this.setVisible( _available && (_catHead || _category==0) );
        // log.debug("completed init components index {} title {}",_index,_textLabel);
    }
    
    private void resetenableButton(){
        if (enableButton.isSelected()) {
            _buttonText = Bundle.getMessage("ButtonFilter");
            enableButton.setText( newTextString() );
            enableButton.setToolTipText(Bundle.getMessage("ButtonFilterTip"));
            enableButton.setBackground(redish);
        } else {
            _buttonText = Bundle.getMessage("ButtonPass");
            enableButton.setText( newTextString() );
            enableButton.setToolTipText(Bundle.getMessage("ButtonPassTip"));
            enableButton.setBackground(greenish);
        }
    }
    
    protected void setMixed() {
        
        _buttonText = Bundle.getMessage("ButtonMixed");
        enableButton.setText( newTextString() );
        enableButton.setToolTipText(Bundle.getMessage("ButtonMixedTip"));
        enableButton.setBackground(amberish);
        enableButton.setSelected(false);
    }
    
    protected int getIndex() {
        return _index;
    }
    
    protected int getCategory() {
        return _category;
    }
    
    protected void visibleFilter(Boolean showornot) {
        this.setVisible(showornot);
    }
    
    protected Boolean iscatHead() {
        return _catHead;
    }

    protected void incrementFilter() {
        _countFilter++;
        enableButton.setText(newTextString());
    }
    
    protected void incrementPass() {
        _countPass++;
        enableButton.setText(newTextString());
    }
    
    private String newTextString() {
        StringBuilder t = new StringBuilder();
        t.append(_buttonText);
        t.append(" ( ");
        t.append(_countPass);
        t.append(" / ");
        t.append(_countFilter);
        t.append(" ) ");
        return t.toString();
    }
    
    protected Boolean getButton() {
        return enableButton.isSelected();
    }
    
    protected Boolean getVisible() {
        return this.isVisible();
    }

    protected Boolean getAvailable() {
        return _available;
    }
    
    protected void setNode( int node, Boolean filter, Boolean show ) {
        log.debug("panel {} setavailable node {} filter {} show {} ",_index,node, filter,show );
        _available=true;
        _textLabel=Bundle.getMessage("CbusNode") + Integer.toString(node);
        fLabel.setText(_textLabel);
        this.setVisible(show);
        setPass(filter);
    }
    
    protected void setPass(Boolean trueorfalse) {
        // log.debug("id {} set pass {}",_index,trueorfalse);
        enableButton.setSelected(trueorfalse);
        resetenableButton();
    }
    
    protected void setToolTip(String text) {
        fLabel.setToolTipText(text);
        catButton.setToolTipText(text);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusFilterPanel.class);
}
