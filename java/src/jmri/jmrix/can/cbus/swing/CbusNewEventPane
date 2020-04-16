package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import java.awt.GridLayout;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.text.DefaultFormatter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrix.can.cbus.CbusFilterType;
import jmri.util.ThreadingUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Creates Panel for displaying a single filter
 * @author Steve Young Copyright (C) 2018, 2020
 */
public class CbusFilterPanel extends JPanel {
    
    private final CbusFilterType _fType;
    private final int _index;
    private final CbusFilterFrame _filterFrame;
    private String _textLabel;
    private int _countFilter=0;
    private int _countPass=0;
    private JLabel fLabel;
    private JPanel evPane;
    private boolean _available;
    private String _buttonText = Bundle.getMessage("ButtonPass");
    
    private final static Color greenish = new Color(110, 235, 131);
    private final static Color redish = new Color(255, 132, 84);
    private final static Color amberish = new Color(228, 255, 26);

    // Buttons to enable/disable filters
    private JToggleButton enableButton;
    private JToggleButton catButton;

    /**
     * Create a new CbusFilterPanel for filtering any Nodes which are heard
     * This is created at Frame startup, though #initComponents
     * is not called until the Node is actually set by #setNode
     * 
     * @param filterFrame parent Frame
     * @param index Filter Index
     */
    public CbusFilterPanel(CbusFilterFrame filterFrame, int index) {
        super();
        _index = index;
        _filterFrame = filterFrame;
        _fType = CbusFilterType.CFNODES;
        _textLabel = Bundle.getMessage("CbusNodes");
        _available = false;
        setVisible(false);
    }
    
    /**
     * Create a new CbusFilterPanel for filtering any CbusFilterType.
     * This is created at Frame startup, #initComponents
     * is called straight away.
     * 
     * @param filterFrame parent Frame
     * @param fType Filter to display
     */
    public CbusFilterPanel(CbusFilterFrame filterFrame, CbusFilterType fType){
        super();
         _index = fType.ordinal();
        _filterFrame = filterFrame;
        _fType = fType;
        _textLabel = _fType.getName();
        _available=true;
        initComponents();
    }
    
    private static double _iconScale = 0.25;
    
    private NamedIcon getCollapsed() {
        NamedIcon collapsed = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        collapsed.scale(_iconScale, this);
        return collapsed;
    }
    
    private NamedIcon getShowing() {
        NamedIcon showing = new NamedIcon("resources/icons/decorations/ArrowStyle2.png", "resources/icons/decorations/ArrowStyle2.png");
        showing.scale(_iconScale,this);
        showing.setRotation(3, this);
        return showing;
    }

    /**
     * Initialise the Pane.
     */
    protected final void initComponents() {
        
        this.setLayout(new GridLayout(1,1,30,30)); // row, col, hgap, vgap

        // Pane to hold Event
        evPane = new JPanel();
        evPane.setBackground(Color.white);
        
        catButton = new JToggleButton();
        fLabel = new JLabel(_textLabel,SwingConstants.RIGHT);
        fLabel.setToolTipText(getFilterType().getToolTip());
        if (getFilterType().isCategoryHead()){
            evPane.setLayout(new GridLayout(1,2,0,10));
            configCatButton(catButton);
            evPane.add(catButton);
        }
        else {
            evPane.setLayout(new GridLayout(1,2,40,10));
        
            if ( _fType.showSpinners() ) {
                evPane.add(getNewSpinnerAndButton(_fType));
            } else {
                evPane.add(fLabel);
            }
        }
        enableButton = getNewEnableButton();
        
        evPane.add(enableButton);
        evPane.setVisible(true);
        this.add(evPane);
        this.setVisible( getFilterType().alwaysDisplay() );
    }
    
    private void configCatButton(@Nonnull JToggleButton button){
    
        button.setBackground(Color.white);
        button.setText(_textLabel);
        button.setIcon(getCollapsed());
        button.setHorizontalAlignment(SwingConstants.RIGHT);
        button.setHorizontalTextPosition(JLabel.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener((java.awt.event.ActionEvent e) -> {
            if (button.isSelected()) {
                // log.warn("selected category index {}",_index);
                button.setIcon(getShowing());
            } else {
                // log.warn("deselected category index {} ",_index);
                button.setIcon(getCollapsed());
            }
            ThreadingUtil.runOnGUIEventually( ()->{
                _filterFrame.showFiltersChanged(_index,button.isSelected(),getFilterType());
            });
        });
    }
    
    private JPanel getNewSpinnerAndButton(@Nonnull CbusFilterType fType){
    
        JPanel spinnerAndButton = new JPanel();
        spinnerAndButton.setLayout(new GridLayout(1,2,0,0));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
        spinner.setEditor(editor);
        
        JFormattedTextField field = (JFormattedTextField) editor.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        spinner.addChangeListener((ChangeEvent e) -> {
            _filterFrame.setMinMax(fType,(Integer) spinner.getValue() );
        });

        spinnerAndButton.setBackground(Color.white);
        spinnerAndButton.add(spinner);
        spinnerAndButton.add(fLabel);
    
        return spinnerAndButton;
    }
    
    private JToggleButton getNewEnableButton(){
        JToggleButton newButton = new JToggleButton();
        newButton.setUI(new BasicToggleButtonUI()); //Removes selectColor
        newButton.setText( newTextString() );
        newButton.setBackground(greenish);
        newButton.setFocusPainted(false);
        newButton.setToolTipText(Bundle.getMessage("ButtonPassTip"));

        // connect actions to buttons
        newButton.addActionListener((java.awt.event.ActionEvent e) -> {
            resetenableButton();
            ThreadingUtil.runOnGUIEventually( ()->{
                _filterFrame.checkBoxChanged(_index,enableButton.isSelected(),getFilterType());
                StringBuilder txt = new StringBuilder();
                txt.append(_textLabel)
                .append(": ")
                .append(_buttonText);
                if (getFilterType().isCategoryHead()){
                    txt.append(" ")
                    .append(Bundle.getMessage("All"));
                }
                _filterFrame.updateListeners(txt.toString());
            });
        });
        return newButton;
    }
    
    /**
     * Reset the Filter Button.
     */
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
    
    /**
     * If panel is displaying a parent category, some children categories may
     * be both Active and Inactive.
     */
    protected void setMixed() {
        _buttonText = Bundle.getMessage("ButtonMixed");
        enableButton.setText( newTextString() );
        enableButton.setToolTipText(Bundle.getMessage("ButtonMixedTip"));
        enableButton.setBackground(amberish);
        enableButton.setSelected(false);
    }
    
    /**
     * Get the Filter Index, value for main Filter.
     * @return Filter Index.
     */
    protected final int getIndex() {
        return _index;
    }
    
    /**
     * Show or Hide the Filter Panel
     * @param showornot true to show, false to hide.
     */
    protected void visibleFilter(boolean showornot) {
        this.setVisible(showornot);
    }
    
    /**
     * Get the Filter Type in use by the panel.
     * @return Filter Type ENUM
     */
    @Nonnull
    protected final CbusFilterType getFilterType(){
        return _fType;
    }

    /**
     * Increment the number of Filtered Frames.
     * Updates text label with counts.
     */
    protected void incrementFilter() {
        _countFilter++;
        enableButton.setText(newTextString());
    }
    
    /**
     * Increment the number of allowed Frames.
     * Updates text label with counts.
     */
    protected void incrementPass() {
        _countPass++;
        enableButton.setText(newTextString());
    }
    
    /**
     * Get the button text string, with text label and filter / pass counts.
     * @return Full text string
     */
    @Nonnull
    private String newTextString() {
        StringBuilder t = new StringBuilder();
        t.append(_buttonText)
        .append(" ( ")
        .append(_countPass)
        .append(" / ")
        .append(_countFilter)
        .append(" ) ");
        return t.toString();
    }
    
    /**
     * Get if the button is currently set to filter.
     * @return true if filtering, else false.
     */
    protected boolean getButton() {
        return enableButton.isSelected();
    }
    
    /**
     * Get if the Panel is visible
     * @return true if visible, else false.
     */
    protected boolean getVisible() {
        return this.isVisible();
    }

    /**
     * Get if the Panel is in use.
     * @return true if in use by a Filter / Node, else false.
     */
    protected boolean getAvailable() {
        return _available;
    }
    
    /**
     * Set the Panel for a Node.
     * @param node Node Number
     * @param filter true to Start filter active, false to pass.
     * @param show true to display, false to hide.
     */
    protected void setNode( int node, boolean filter, boolean show ) {
        initComponents();
        _available=true;
        _textLabel=Bundle.getMessage("CbusNode") + Integer.toString(node);
        fLabel.setText(_textLabel);
        this.setVisible(show);
        setPass(filter);
    }
    
    /**
     * Set the panel button to display Pass / Filter.
     * @param trueorfalse true to pass, false to filter.
     */
    protected void setPass(boolean trueorfalse) {
        enableButton.setSelected(trueorfalse);
        resetenableButton();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusFilterPanel.class);
}
