package jmri.jmrit.beantable.block;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import jmri.Block;

/**
 * JComboBox to display / select block curvature.
 * @see jmri.Block
 * 
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class BlockCurvatureJComboBox extends JComboBox<String> {
    
    private static final String NONE_TEXT = Bundle.getMessage("BlockNone"); // NOI18N
    private static final String GRADUAL_TEXT = Bundle.getMessage("BlockGradual"); // NOI18N
    private static final String TIGHT_TEXT = Bundle.getMessage("BlockTight"); // NOI18N
    private static final String SEVERE_TEXT = Bundle.getMessage("BlockSevere"); // NOI18N
    private static final String[] CURVE_OPTIONS = {NONE_TEXT, GRADUAL_TEXT, TIGHT_TEXT, SEVERE_TEXT};
    
    /**
     * Create a new JComboBox to display / select block curvature.
     * <p>
     * Options are presented in localised String form and can be 
     * set / retrieved in Block Constant format.
     * <p>
     * Block.NONE, Block.GRADUAL, Block.TIGHT, Block.SEVERE
     * <p>
     * Defaults to No curvature.
     */
    public BlockCurvatureJComboBox(){
        super(new CurvatureComboBoxModel());
        setSelectedItem(NONE_TEXT);
    }
    
    /**
     * Create a new JComboBox to display / select block curvature.
     * <p>
     * Block.NONE, Block.GRADUAL, Block.TIGHT, Block.SEVERE
     * <p>
     * Defaults to No curvature if invalid number in curvature field.
     * @param curvature Block constant for Curvature.
     */
    public BlockCurvatureJComboBox(int curvature){
        super(new CurvatureComboBoxModel());
        setCurvature(curvature);
    }
    
    /**
     * Set UI properties for a JTable cell.
     */
    public void setJTableCellClientProperties(){
        putClientProperty("JComponent.sizeVariant", "small"); // NOI18N
        putClientProperty("JComboBox.buttonType", "square"); // NOI18N
    }
    
    /**
     * Set the Block Curvature by Block Constant format.
     * If unrecognised constant, does not error or change selected value.
     * @param blockCurve e.g. "Block.TIGHT" or "Block.NONE"
     */
    public final void setCurvature(int blockCurve){
        setSelectedItem(getStringFromCurvature(blockCurve));
    }
    
    /**
     * Get the String of Block Curvature from Block Constant format.
     * .e.g. Bundle.getMessage("BlockTight")
     * @param blockCurve Block Constant, e.g. Block.GRADUAL
     * @return localised String, or Bundle.getMessage("BlockNone") if unmatched.
     */
    public static String getStringFromCurvature(int blockCurve){
        switch (blockCurve) {
            case Block.GRADUAL:
                return GRADUAL_TEXT;
            case Block.TIGHT:
                return TIGHT_TEXT;
            case Block.SEVERE:
                return SEVERE_TEXT;
            default:
                return NONE_TEXT;
        }
    }
    
    /**
     * Get the Block Curvature in Block Constant format.
     * e.g. "Block.TIGHT" or "Block.NONE"
     * @return selected Block Curvature constant.
     */
    public int getCurvature(){
        return getCurvatureFromString((String)getSelectedItem());
    }
    
    /**
     * Get the Block Curvature in Block Constant format.e.g.
     * "Block.TIGHT" or "Block.NONE"
     * 
     * @param s localised String, e.g. Bundle.getMessage("BlockSevere")
     * @return Block Curvature constant, Block.NONE if String unrecognised.
     */
    public static int getCurvatureFromString(String s){
        if (GRADUAL_TEXT.equals(s)){
            return Block.GRADUAL;
        } else if (TIGHT_TEXT.equals(s)){
            return Block.TIGHT;
        } else if (SEVERE_TEXT.equals(s)){
            return Block.SEVERE;
        } else {
            return Block.NONE;
        }
    }

    /**
     * Get the Curvature Constant from a JComboBox passed as an Object.
     * For use in setValueAt() in Table Models.
     * @param obj the object which should be a JComboBox.
     * @return Block curvature Constant if JComboBox found and selected item 
     *         text matches, else Block.NONE
     */    
    public static int getCurvatureFromObject(Object obj){
        if (obj instanceof BlockCurvatureJComboBox){
            BlockCurvatureJComboBox jcb = (BlockCurvatureJComboBox) obj;
            return getCurvatureFromString((String) jcb.getSelectedItem());
        }
        return Block.NONE;
    }
    
    private static class CurvatureComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
        
        private String selection = null;

        @Override
        public String getElementAt(int index) {
            return CURVE_OPTIONS[index];
        }

        @Override
        public int getSize() {
            return CURVE_OPTIONS.length;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selection = (String) anItem; // to select and register an
        } // item from the pull-down list

        // Methods implemented from the interface ComboBoxModel
        @Override
        public String getSelectedItem() {
            return selection; // to add the selection to the combo box
        }
    }
    
}
