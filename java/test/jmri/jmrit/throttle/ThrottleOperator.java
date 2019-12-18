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
import org.netbeans.jemmy.operators.JSpinnerOperator;
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

   // type the address value.
   public void typeAddressValue(int address){
    JInternalFrameOperator ifo = getAddressPanelOperator();
    JTextFieldOperator jtfo = new JTextFieldOperator(ifo); // only one text field in the address panel.
    jtfo.typeText(""+address);
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

   public boolean setButtonEnabled(){
        return (new JButtonOperator(this,Bundle.getMessage("ButtonSet"))).isEnabled();
   }

   public void pushDispatchButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonDispatch")).push();
   }
   public void pushReleaseButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonRelease")).push();
   }

   public boolean releaseButtonEnabled(){
        return (new JButtonOperator(this,Bundle.getMessage("ButtonRelease"))).isEnabled();
   }
   
   public boolean dispatchButtonEnabled(){
        return (new JButtonOperator(this,Bundle.getMessage("ButtonDispatch"))).isEnabled();
   }

   public void answerStealQuestion(boolean steal){
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("StealRequestTitle"));
        if(steal) {
           (new JButtonOperator(jdo,Bundle.getMessage("ButtonYes"))).doClick();
        } else {
           (new JButtonOperator(jdo,Bundle.getMessage("ButtonNo"))).doClick();
        }
   }

    public void answerShareQuestion(boolean share){
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ShareRequestTitle"));
        if(share) {
           (new JButtonOperator(jdo,Bundle.getMessage("ButtonYes"))).doClick();
        } else {
           (new JButtonOperator(jdo,Bundle.getMessage("ButtonNo"))).doClick();
        }
    }
    
    // Steal / Share / Cancel dialogue operators
    public void answerStealShareQuestionSteal(){
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("StealShareRequestTitle"));
        new JButtonOperator(jdo,Bundle.getMessage("StealButton")).doClick();
    }
    
    public void answerStealShareQuestionShare(){
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("StealShareRequestTitle"));
        new JButtonOperator(jdo,Bundle.getMessage("ShareButton")).doClick();
    }
    
    public void answerStealShareQuestionCancel(){
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("StealShareRequestTitle"));
        new JButtonOperator(jdo,Bundle.getMessage("CancelButton")).doClick();
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
	    jpmo.pushMenuNoBlock(Bundle.getMessage("MenuItemProperties"));
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

   public void openControlPanelPopupMenu(){
        JInternalFrameOperator jifo  = getControlPanelOperator();
        jifo.clickForPopup();
        JPopupMenuOperator jpmo = new JPopupMenuOperator();
	    jpmo.pushMenuNoBlock(Bundle.getMessage("ControlPanelProperties"));
   }

   public void setSpeedStepDisplay(){
	    openControlPanelPopupMenu();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleEditSpeedControlPanel"));
        (new JRadioButtonOperator(jdo,Bundle.getMessage("ButtonDisplaySpeedSteps"))).doClick();
        (new JButtonOperator(jdo,Bundle.getMessage("ButtonOK"))).doClick();
         
   }

   public void setSpeedSpinner(int i){
        new JSpinnerOperator(getControlPanelOperator()).setValue(i);
   }

   public void speedSpinnerMaximum(){
        new JSpinnerOperator(getControlPanelOperator()).scrollToMaximum();
   }

   public void speedSpinnerMinimum(){
        new JSpinnerOperator(getControlPanelOperator()).scrollToMinimum();
   }

}
