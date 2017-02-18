package jmri.jmrix.lenz.swing.lv102;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import javax.swing.JComboBox;

/*
 *  Helper class for operating the LV102Frame and the  LV102InternalFrame
 *  it contains.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class LV102FrameScaffold extends JFrameOperator{

   private JInternalFrameOperator IFrameOperator = null;

   public LV102FrameScaffold(){
        super(Bundle.getMessage("LV102Config"));
        IFrameOperator = new JInternalFrameOperator(this,Bundle.getMessage("LV102Power"));
   }

   public void pushCloseButton(){
        new JToggleButtonOperator(this,"Close").push();
   }

   public void pushResetButton(){
        new JToggleButtonOperator(IFrameOperator,Bundle.getMessage("LV102ResetButtonLabel")).push();
   }

   public void pushDefaultButton(){
        new JToggleButtonOperator(IFrameOperator,Bundle.getMessage("LV102DefaultButtonLabel")).push();
   }

   public void pushWriteSettingsButton(){
        new JToggleButtonOperator(IFrameOperator,Bundle.getMessage("LV102WriteSettingsButtonLabel")).push();
   }

   public String getSelectedVoltage(){
        return ((String) ((JComboBox) new JLabelOperator(IFrameOperator,Bundle.getMessage("LV102Track")).getLabelFor()).getSelectedItem());
   }

   public String getSelectedELineValue(){
        return ((String) ((JComboBox) new JLabelOperator(IFrameOperator,Bundle.getMessage("LV102ELine")).getLabelFor()).getSelectedItem());
   }

}
