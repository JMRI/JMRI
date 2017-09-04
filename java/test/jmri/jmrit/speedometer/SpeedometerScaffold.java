package jmri.jmrit.speedometer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/*
 *  Helper class for operating the Speedometer Frame.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class SpeedometerScaffold extends JFrameOperator {

   public SpeedometerScaffold(){
        super(Bundle.getMessage("TitleSpeedometer"));
   }

   public void pushToMetricButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonToMetric")).push();
   }

   public void pushToEnglishButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonToMetric")).push();
   }

   public void pushStartButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonStart")).push();
   }

   public void pushClearButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonClear")).push();
   }
   
   public void pushSaveButton(){
        new JButtonOperator(this,Bundle.getMessage("ButtonSave")).push();
   }

   public void setStartSensorValue(String value){
        new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("LabelStartSensor")).getLabelFor()).setText(value);
   }

   public void setStopSensor1Value(String value){
        new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("LabelStopSensor1")).getLabelFor()).setText(value);
   }

   public void setStopSensor2Value(String value){
        new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("LabelStopSensor2")).getLabelFor()).setText(value);
   }

   public void setDistance1Value(String value){
        new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("Distance1English")).getLabelFor()).setText(value);
   }

   public void setDistance2Value(String value){
        new JTextFieldOperator((JTextField) new JLabelOperator(this,Bundle.getMessage("Distance2English")).getLabelFor()).setText(value);
   }

   public String getResult1Text(){
        return ((JLabel) new JLabelOperator(this,Bundle.getMessage("Speed1English")).getLabelFor()).getText();
   }

   public String getResult2Text(){
        return ((JLabel) new JLabelOperator(this,Bundle.getMessage("Speed2English")).getLabelFor()).getText();
   }

   // time is problematic.  There is only one label text for both time values.
   public String getTimeText(){
        return ((JLabel) new JLabelOperator(this,Bundle.getMessage("Labeltime")).getLabelFor()).getText();
   }

}
