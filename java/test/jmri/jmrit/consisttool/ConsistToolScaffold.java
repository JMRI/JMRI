package jmri.jmrit.consisttool;

import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.jmrit.DccLocoAddressSelector;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/*
 *  Helper class for operating the Consist Tool Frame.
 *
 *  @author Paul Bender Copyright (C) 2018
 */
public class ConsistToolScaffold extends JFrameOperator {

   public ConsistToolScaffold(){
        super(Bundle.getMessage("ConsistToolTitle"));
   }

   // push consist type buttons.
   public void pushAdvancedConsistButton(){
        new JRadioButtonOperator(this,Bundle.getMessage("AdvancedConsistButtonText")).push();
   }

   public void pushCommandStationConsistButton(){
        new JRadioButtonOperator(this,Bundle.getMessage("CommandStationConsistButtonText")).push();
   }

   // fill out the consist address field
   public void setConsistAddressValue(String value){
	DccLocoAddressSelector sel = (DccLocoAddressSelector)(new JLabelOperator(this,Bundle.getMessage("AddressLabelText")).getLabelFor());
        int addr = Integer.parseInt(value);
	sel.setAddress(new DccLocoAddress(addr,addr>=100));
   }

   // push loco specific buttons at the top.
   public void pushAddButton(){
        new JButtonOperator(this,Bundle.getMessage("AddButtonText")).push();
   }

   public void pushResetButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonReset")).push();
   }

   // fill out the loco address field
   public void setLocoAddressValue(String value){
        //new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("LocoLabelText")).getLabelFor()).setText(value);
	DccLocoAddressSelector sel = (DccLocoAddressSelector)(new JLabelOperator(this,Bundle.getMessage("LocoLabelText")).getLabelFor());
        int addr = Integer.parseInt(value);
	sel.setAddress(new DccLocoAddress(addr,addr>=100));
   }


   // push the buttons at the bottom.
   public void pushDeleteButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonDelete")).push();
   }

   public void pushThrottleButton(){
        new JButtonOperator(this,Bundle.getMessage("ThrottleButtonText")).push();
   }

   public void pushReverseButton(){
        new JButtonOperator(this,Bundle.getMessage("ReverseButtonText")).push();
   }
   
   public void pushRestoreButton(){
        new JButtonOperator(this,Bundle.getMessage("RestoreButtonText")).push();
   }

}
