package jmri.jmrit.roster.swing;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/*
 *  Helper class for operating the RosterFrame Frame.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class RosterFrameScaffold extends JFrameOperator{

   public RosterFrameScaffold(String frameTitle){
        super(frameTitle);
   }

   public void pushIdentifyButton(){
        new JButtonOperator(this,"Identify").push();
   }

   public boolean isIdentifyButtonEnabled(){
        return new JButtonOperator(this,"Identify").isEnabled();
   }

}
