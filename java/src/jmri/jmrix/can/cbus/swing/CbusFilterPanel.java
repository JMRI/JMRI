package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.SwingConstants;
import jmri.jmrit.catalog.NamedIcon;
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
    
    private Color greenish = new Color(110, 235, 131);
    private Color redish = new Color(255, 132, 84);
    private Color amberish = new Color(228, 255, 26);

    // Buttons to enable/disable filters
    protected JToggleButton enableButton;
    protected JToggleButton catButton;

    /**
     * Create a new instance of CbusFilterPanel.
     */
    public CbusFilterPanel(CbusFilterFrame filterFrame, int index, String textLabel, Boolean catHead, int category) {
        super();
        _index = index;
        _filterFrame = filterFrame;
        _textLabel = textLabel;
        _countFilter = 0;
        _countPass = 0;
        _category = category;
        _catHead = catHead;
        initComponents();
    }

    protected CbusFilterPanel() {
        super();
    }

    public void initComponents() {
        // log.debug("init components");
        
        double _iconScale = 0.25;
        double _iconScaleb = 0.25;
        
        NamedIcon collapsed = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        collapsed.scale(_iconScale,this);
        NamedIcon showing = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        showing.scale(_iconScaleb,this);
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
            evPane.add(fLabel);
        }
        enableButton = new JToggleButton();
        enableButton.setUI(new BasicToggleButtonUI()); //Removes selectColor
        enableButton.setText(Bundle.getMessage("ButtonPass"));
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
                    txt.append(enableButton.getText());
                    if (_catHead){
                        txt.append(" ");
                        txt.append(Bundle.getMessage("All"));
                    }
                    _filterFrame.updateListeners(txt.toString());
                });
            }
        });
        
        this.setVisible( _catHead || _category==0 );
        log.debug("completed init components");
    }
    
    private void resetenableButton(){
        if (enableButton.isSelected()) {
            enableButton.setText(Bundle.getMessage("ButtonFilter"));
            enableButton.setToolTipText(Bundle.getMessage("ButtonFilterTip"));
            enableButton.setBackground(redish);
        } else {
            enableButton.setText(Bundle.getMessage("ButtonPass"));
            enableButton.setToolTipText(Bundle.getMessage("ButtonPassTip"));
            enableButton.setBackground(greenish);
        }
    }
    
    protected void setMixed() {
        enableButton.setText(("Mixed"));
        enableButton.setToolTipText(("ButtonMixedTip"));
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
        fLabel.setText(newTextString());
        catButton.setText(newTextString());
    }
    
    protected void incrementPass() {
        _countPass++;
        fLabel.setText(newTextString());
        catButton.setText(newTextString());
    }
    
    private String newTextString() {
        StringBuilder t = new StringBuilder();
        t.append("<html> ( <span style='color:green'>");
        t.append(_countPass);
        t.append("</span> / <span style='color:red'>");
        t.append(_countFilter);
        t.append("</span> ) ");
        t.append(_textLabel);
        t.append("</html>");
        return t.toString();
    }
    
    protected Boolean getButton() {
        return enableButton.isSelected();
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
