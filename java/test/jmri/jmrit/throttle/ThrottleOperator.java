package jmri.jmrit.throttle;

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
public class ThrottleOperator extends JFrameOperator {

   public ThrottleOperator(){
        this(Bundle.getMessage("ThrottleTitle"));
   }

   public ThrottleOperator(String title){
        super(title);
   }


   // Address Panel Operations
   public void pushSetButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonSet")).push();
   }
   public void pushDispatchButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonDispatch")).push();
   }
   public void pushReleaseButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonRelease")).push();
   }

}
