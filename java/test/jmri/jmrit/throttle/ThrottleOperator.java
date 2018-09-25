package jmri.jmrit.throttle;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.DccLocoAddress;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.util.swing.JemmyUtil;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JSliderOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

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

   private AddressPanel getAddressPanel(){
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
	return ap;
   }

   // get the address value.
   public DccLocoAddress getAddressValue(){
	AddressPanel ap = getAddressPanel();
	return ap.getCurrentAddress();
   }

   // get the consist address value.
   public DccLocoAddress getConsistAddressValue(){
	AddressPanel ap = getAddressPanel();
	return ap.getConsistAddress();
   }

   // set the address value.
   public void setAddressValue(DccLocoAddress addr){
	AddressPanel ap = getAddressPanel();
	ap.setCurrentAddress(addr);
   }

   // get the consist address value.
   public jmri.Throttle getAttachedThrottle(){
	AddressPanel ap = getAddressPanel();
	return ap.getThrottle();
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
   public JInternalFrameOperator getFunctionPanelOperator(){
       return new JInternalFrameOperator(this, 
		       Bundle.getMessage("ThrottleMenuViewFunctionPanel"));
   }

   public void pushFunctionButton(String Function){
        new JButtonOperator(getFunctionPanelOperator(),Function).push();
   }

   public void pushAlt1Button(){
	JToggleButton alt1Button = (JToggleButton) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof JToggleButton ) {
			   if(((JToggleButton)c).getText().equals("*")){ 
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
	JemmyUtil.enterClickAndLeave(alt1Button);
   }

   public void pushAlt2Button(){
	JToggleButton alt1Button = (JToggleButton) findSubComponent(
	       new ComponentChooser() { 
                  @Override
       	          public boolean checkComponent(Component c) { 
		      if (c instanceof JToggleButton ) {
			   if(((JToggleButton)c).getText().equals("#")){ 
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
	JemmyUtil.enterClickAndLeave(alt1Button);
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

   public void openFunctionPopupMenu(int function){
        FunctionButton fb = getFunctionButton(function);
        JToggleButtonOperator jbo = new JToggleButtonOperator(fb);
        jbo.clickForPopup();
        JPopupMenuOperator jpmo = new JPopupMenuOperator();
	jpmo.pushMenu(Bundle.getMessage("MenuItemProperties"));
   }

   public void toggleFunctionMomentary(int function){
	openFunctionPopupMenu(function);
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ButtonEditFunction"));
        (new JCheckBoxOperator(jdo,Bundle.getMessage("CheckBoxLockable"))).doClick();
        (new JButtonOperator(jdo,Bundle.getMessage("ButtonOK"))).doClick();
        
   }

   // Control (Speed and Direction) panel operations
   public JInternalFrameOperator getControlPanelOperator(){
       return new JInternalFrameOperator(this, 
		       Bundle.getMessage("ThrottleMenuViewControlPanel"));
   }

   public void pushStopButton(){
        new JButtonOperator(getControlPanelOperator(),
			Bundle.getMessage("ButtonStop")).push();
   }

   public void pushEStopButton(){
        new JButtonOperator(getControlPanelOperator(),
			Bundle.getMessage("ButtonStop")).push();
   }

   public void pushIdleButton(){
        new JButtonOperator(getControlPanelOperator(),
			Bundle.getMessage("ButtonIdle")).push();
   }

   public void pushForwardButton(){
        new JRadioButtonOperator(getControlPanelOperator(),
			Bundle.getMessage("ButtonForward")).push();
   }

   public void pushReverseButton(){
        new JRadioButtonOperator(getControlPanelOperator(),
			Bundle.getMessage("ButtonReverse")).push();
   }

   public int getSpeedSliderValue(){
        return new JSliderOperator(getControlPanelOperator()).getValue();
   }

   public void setSpeedSlider(int i){
        new JSliderOperator(getControlPanelOperator()).setValue(i);
   }

   public void slideSpeedSlider(int i){
        new JSliderOperator(getControlPanelOperator()).scrollToValue(i);
   }

   public void speedSliderMaximum(){
        new JSliderOperator(getControlPanelOperator()).scrollToMaximum();
   }

   public void speedSliderMinimum(){
        new JSliderOperator(getControlPanelOperator()).scrollToMinimum();
   }


}
