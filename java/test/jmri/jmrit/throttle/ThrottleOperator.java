package jmri.jmrit.throttle;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.jmrit.DccLocoAddressSelector;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/*
 *  Helper class for operating the Throttle Frame.
 *
 *  @author Paul Bender Copyright (C) 2018
 */
public class ThrottleOperator extends JFrameOperator {

   public ThrottleOperator(){
        this(Bundle.getMessage("ThrottleTitle"));
   }

   public ThrottleOperator(String title){
        super(title);
   }

   // Address Panel Operations
   public JInternalFrameOperator getAddressPanelOperator(){
       return new JInternalFrameOperator(this, 
		       Bundle.getMessage("ThrottleMenuViewAddressPanel"));
   }

   // get the address value.
   public DccLocoAddress getAddressValue(){
	AddressPanel ap = (AddressPanel) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof AddressPanel ) 
			   return true; 
		      else return false; 
	          } 
                  @Override
	          public String getDescription() { 
		      return "Find AddressSelector"; 
	          }
	});
	return ap.getCurrentAddress();
   }

   // get the consist address value.
   public DccLocoAddress getConsistAddressValue(){
	AddressPanel ap = (AddressPanel) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof AddressPanel ) 
			   return true; 
		      else return false; 
	          } 
                  @Override
	          public String getDescription() { 
		      return "Find AddressSelector"; 
	          }
	});
	return ap.getConsistAddress();
   }

   // set the address value.
   public void setAddressValue(DccLocoAddress addr){
	AddressPanel ap = (AddressPanel) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof AddressPanel ) 
			   return true; 
		      else return false; 
	          } 
                  @Override
	          public String getDescription() { 
		      return "Find AddressSelector"; 
	          }
	});
	ap.setCurrentAddress(addr);
   }

   public void pushSetButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonSet")).push();
   }
   public void pushDispatchButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonDispatch")).push();
   }
   public void pushReleaseButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonRelease")).push();
   }

   // Function panel operations
   public void pushFunctionButton(String Function){
        new JButtonOperator(this,Function).push();
   }

   public FunctionButton getFunctionButton(int function){
	FunctionButton retval = (FunctionButton) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof FunctionButton ) {
			   if(((FunctionButton)c).getIdentity()==function){ 
			      return true; 
			   } else {
                              return false;
                           }
		      } else {
			return false;
                      }
	          } 
                  @Override
	          public String getDescription() { 
		      return "Find Function Button"; 
	          }
	});
        return retval;
   }

}
