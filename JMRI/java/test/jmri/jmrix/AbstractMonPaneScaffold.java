package jmri.jmrix;

import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/*
 *  Helper class for operating the Speedometer Frame.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class AbstractMonPaneScaffold extends ContainerOperator{

   public AbstractMonPaneScaffold(AbstractMonPane pane){
        super(pane);
   }

   public void checkTimeStampCheckBox(){
        new JCheckBoxOperator(this,Bundle.getMessage("ButtonShowTimestamps")).clickMouse();
   }

   public boolean getTimeStampCheckBoxValue(){
        return (new JCheckBoxOperator(this,Bundle.getMessage("ButtonShowTimestamps")).isSelected());
   }

   public void checkRawCheckBox(){
        new JCheckBoxOperator(this,Bundle.getMessage("ButtonShowRaw")).clickMouse();
   }

   public boolean getRawCheckBoxValue(){
        return (new JCheckBoxOperator(this,Bundle.getMessage("ButtonShowRaw")).isSelected());
   }

   public void checkOnTopCheckBox(){
        new JCheckBoxOperator(this,Bundle.getMessage("ButtonWindowOnTop")).clickMouse();
   }

   public boolean getOnTopCheckBoxValue(){
        return (new JCheckBoxOperator(this,Bundle.getMessage("ButtonWindowOnTop")).isSelected());
   }

   public void checkAutoScrollCheckBox(){
        new JCheckBoxOperator(this,Bundle.getMessage("ButtonAutoScroll")).clickMouse();
   }

   public boolean getAutoScrollCheckBoxValue(){
        return (new JCheckBoxOperator(this,Bundle.getMessage("ButtonAutoScroll")).isSelected());
   }

   public void clickFreezeButton(){
        new JToggleButtonOperator(this,Bundle.getMessage("ButtonFreezeScreen")).clickMouse();
   }

   public boolean getFreezeButtonState(){
        return (new JToggleButtonOperator(this,Bundle.getMessage("ButtonFreezeScreen")).isSelected());
   }

   public void clickEnterButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonAddMessage")).clickMouse();
   }

}
