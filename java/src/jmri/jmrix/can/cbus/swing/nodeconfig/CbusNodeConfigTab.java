package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JOptionPane;
import jmri.jmrix.can.cbus.node.CbusNode;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019, 2020
 */
abstract public class CbusNodeConfigTab extends jmri.jmrix.can.swing.CanPanel implements PropertyChangeListener {
    
    protected CbusNode nodeOfInterest;
    private final NodeConfigToolPane mainpane;
    private boolean _activeDialogue;

    /**
     * Create a new instance of CbusNodeSetupPane.
     * @param main the main NodeConfigToolPane this is a pane of.
     */
    protected CbusNodeConfigTab( NodeConfigToolPane main ) {
        super();
        mainpane = main;
        if (main != null ){
            super.initComponents(main.getMemo());
        }
        super.setLayout(new BorderLayout() );
        _activeDialogue = false;
    }
    
    /**
     * Get the Main Node Manager Pane.
     * @return Manager Pane
     */
    protected NodeConfigToolPane getMainPane(){
        return mainpane;
    }
    
    /**
     * Set the Node displayed in the Pane.
     * Checks for node unchanged and
     * disposes listeners on previous node.
     * @param node New CbusNode to display
     */
    public final void setNode(CbusNode node){
        if ( nodeOfInterest != null ) {
            if (nodeOfInterest.equals(node)){
            return;
            }
            disposeOfNode(nodeOfInterest);
        }
        if ( node != null ) {
            nodeOfInterest = node;
            nodeOfInterest.addPropertyChangeListener(this);
            changedNode(node);
            this.setVisible(true);
            validate();
            repaint();
        }
    }
    
    /**
     * Stop the tab or Node selection switching from the Pane.
     * Defaults to false
     * @return true to veto, false to not veto
     */
    protected boolean getVetoBeingChanged(){
        return false;
    }
    
    /**
     * Set that a Dialog box is open within the Pane.
     * @param newVal true if being displayed, else false
     */
    protected void setActiveDialog(boolean newVal){
        _activeDialogue = newVal;
    }
    
    /**
     * Get if a Dialog box is open within the Pane.
     * @return true if displaying dialog, else false
     */
    protected boolean getActiveDialog(){
        return _activeDialogue;
    }
    
    /**
     * Get the Tab Index within the Main Node Manager Pane
     * @return index for this pane
     */
    protected final int getTabIndex(){
        return getMainPane().getTabs().indexOf(this);
    }
    
    /**
     * Extending classes must implement this for changed node notifications
     * @param node The new Node
     */
    protected abstract void changedNode(@Nonnull CbusNode node);
    
    /**
     * Remove any update listeners for the node.
     * @param node Node to remove listeners for
     */
    @OverridingMethodsMustInvokeSuper // to remove Node Property Change Listener
    protected void disposeOfNode(@Nonnull CbusNode node){
        node.removePropertyChangeListener(this);
    }
    
    /**
     * Get the index of the node in the main Node Table
     * @return Row Number for the CbusNode nodeOfInterest
     */
    protected int getNodeRow(){
        return getMainPane().nodeTable.convertRowIndexToView(getMainPane().
            nodeModel.getNodeRowFromNodeNum(nodeOfInterest.getNodeNumber()));
    }
    
    /**
     * Reset the Main Node Manager pane to that of the vetoing Pane.
     */
    protected void resetViewToVeto(){
        getMainPane().nodeTable.getSelectionModel().clearSelection();
        getMainPane().nodeTable.getSelectionModel().setSelectionInterval(getNodeRow(),getNodeRow());
        getMainPane().tabbedPane.setSelectedIndex(getTabIndex());
    }
    
    /**
     * Get a Cancel Edit / Save Edit / Continue Edit Dialog.
     * @param adviceString Extra text to display in box
     * @return true to veto, else false to proceed and continue.
     */
    protected boolean getCancelSaveEditDialog(String adviceString){
        setActiveDialog(true);
        resetViewToVeto();
        int selectedValue = JOptionPane.showOptionDialog(this.getParent(),
            "<html>" + adviceString + "<br>" + Bundle.getMessage("ContinueEditQuestion")+"</html>"
            ,Bundle.getMessage("WarningTitle") + " " + nodeOfInterest,
            JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, null,
            new String[]{Bundle.getMessage("CancelEdit"), Bundle.getMessage("SaveEdit"), Bundle.getMessage("ContinueEdit")},
            Bundle.getMessage("ContinueEdit")); // default choice
        
        setActiveDialog(false);
        switch (selectedValue) {
            case 0:
                cancelOption();
                return false;
            case 1:
                saveOption();
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Custom Cancel action for getCancelSaveEditDialog.
     */
    protected void cancelOption() {}
    
    /**
     * Custom Save action for getCancelSaveEditDialog.
     */
    protected void saveOption() {}
    
    /**
     * {@inheritDoc}
     * Get updates from the CbusNode nodeOfInterest
     */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
        if (nodeOfInterest!=null){
            disposeOfNode(nodeOfInterest);
        }
        super.dispose();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeConfigTab.class);
    
}
