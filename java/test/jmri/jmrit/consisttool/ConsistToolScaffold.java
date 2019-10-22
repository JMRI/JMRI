package jmri.jmrit.consisttool;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.jmrit.DccLocoAddressSelector;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.ComponentChooser;

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
        new JButtonOperator(this, Bundle.getMessage("ButtonAddText")).push();
   }

   public void pushResetButton(){
        new JButtonOperator(this, Bundle.getMessage("ButtonReset")).push();
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
        JButtonOperator jbo = new JButtonOperator(this, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                String tooltip = ((JButton)comp).getToolTipText();
                if(tooltip!=null) {
                    return tooltip.equals(Bundle.getMessage("DeleteButtonToolTip"));
                } else {
                    return false;
                }
            }
            @Override
            public String getDescription() {
                return "tooltip for delete button";
            }
        }
        );
	jbo.push();
   }

   // push the buttons at the bottom and dismiss the resulting question dialog.
   public void pushDeleteWithDismiss(){
	pushDeleteButton();
	// and dismiss the dialog that appears by pressing OK.
	JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("QuestionTitle"));
        new JButtonOperator(jdo,Bundle.getMessage("ButtonYes")).push();
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

   public void startRosterScan(){
        JMenuBarOperator jmbo = new JMenuBarOperator(this); // there's only one menubar
        JMenuOperator jmo = new JMenuOperator(jmbo, Bundle.getMessage("MenuFile"));  // NOI18N
        jmo.push();
        JMenuItemOperator jmio = new JMenuItemOperator(this,Bundle.getMessage("ScanConsists"));
        jmio.push();
   }

}
