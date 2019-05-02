package jmri.jmrix.lenz.swing.lv102;

import javax.swing.JComboBox;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/*
 *  Helper class for operating the LV102Frame and the  LV102InternalFrame
 *  it contains.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class LV102FrameScaffold extends JFrameOperator{

   private JInternalFrameOperator IFrameOperator = null;

   public LV102FrameScaffold(){
        super(Bundle.getMessage("MenuItemLV102ConfigurationManager"));
        IFrameOperator = new JInternalFrameOperator(this,Bundle.getMessage("LV102Power"));
   }

   public void pushCloseButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonClose")).push();
   }

   public void pushResetButton(){
        new JButtonOperator(IFrameOperator,Bundle.getMessage("LV102ResetButtonLabel")).push();
   }

   public void pushDefaultButton(){
        new JButtonOperator(IFrameOperator,Bundle.getMessage("ButtonResetDefaults")).push();
   }

   public void pushWriteSettingsButton(){
        new JToggleButtonOperator(IFrameOperator,Bundle.getMessage("LV102WriteSettingsButtonLabel")).push();
   }

   public String getSelectedVoltage(){
        return ((String) ((JComboBox<?>) new JLabelOperator(IFrameOperator,Bundle.getMessage("LV102Track")).getLabelFor()).getSelectedItem());
   }

   public String getSelectedELineValue(){
        return ((String) ((JComboBox<?>) new JLabelOperator(IFrameOperator,Bundle.getMessage("LV102ELine")).getLabelFor()).getSelectedItem());
   }

}
