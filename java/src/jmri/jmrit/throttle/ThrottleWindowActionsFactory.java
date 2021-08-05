package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;

import jmri.DccThrottle;
import jmri.InstanceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lionel Jeanson
 */
public class ThrottleWindowActionsFactory extends ThrottleWindowActions {
   
    private final List<String> actionStrings = new ArrayList<>(Arrays.asList(
        "accelerate", "decelerate", "accelerateMore", "decelerateMore", "idle", "stop",
        "forward", "reverse", "switchDirection",
        "nextJInternalFrame", "previousJInternalFrame", "showControlPanel", "showFunctionPanel", "showAddressPanel",
        "nextThrottleFrame", "previousThrottleFrame", "nextRunningThrottleFrame", "previousRunningThrottleFrame",
        "nextThrottleWindow", "previousThrottleWindow"
    ) );

    public ThrottleWindowActionsFactory(ThrottleWindow tw) {
        super(tw);
        completeActionStrings();
    }
        
    private void completeActionStrings() {
        for (int i=0; i < tpwkc.getNbFunctionsKeys(); i++) {
            actionStrings.add("fn_"+i+"_Pressed");
            actionStrings.add("fn_"+i+"_Released");
        }
    }
    
    public ActionMap buildActionMap() {
        ActionMap ret = new ActionMap();
        
        // Throttle commands
        ret.put("accelerate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                incrementSpeed(throttle, throttle.getSpeedIncrement());
            }            
        });
        ret.put("decelerate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                incrementSpeed(throttle, -throttle.getSpeedIncrement());
            }            
        });
        ret.put("accelerateMore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                incrementSpeed(throttle, throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
            }            
        });
        ret.put("decelerateMore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                incrementSpeed(throttle, -throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
            }            
        });        
        ret.put("idle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                if (throttle!=null) throttle.setSpeedSetting(0);
            }            
        });
        ret.put("stop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                if (throttle!=null) throttle.setSpeedSetting(-1);
            }            
        });
        ret.put("forward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                if (throttle!=null) throttle.setIsForward(true);
            }            
        });
        ret.put("reverse", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                if (throttle!=null) throttle.setIsForward(false);
            }            
        });
        ret.put("switchDirection", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
                if (throttle!=null) throttle.setIsForward(!throttle.getIsForward());
            }            
        });         
        
        // function buttons
        for (int i=0; i < tpwkc.getNbFunctionsKeys(); i++) {
            ret.put("fn_"+i+"_Pressed", new fnActionPressed(i));
            ret.put("fn_"+i+"_Released", new fnActionReleased(i));
        }
        
        // Throttle inner window cycling
        ret.put("nextJInternalFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.getCurrentThrottleFrame().activateNextJInternalFrame();
            }            
        });
        ret.put("previousJInternalFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.getCurrentThrottleFrame().activatePreviousJInternalFrame();
            }            
        });
        ret.put("showControlPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               toFront(tw.getCurrentThrottleFrame().getControlPanel());
            }            
        });
        ret.put("showFunctionPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               toFront(tw.getCurrentThrottleFrame().getFunctionPanel());
            }            
        });
        ret.put("showAddressPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toFront(tw.getCurrentThrottleFrame().getAddressPanel());
            }            
        });
        
        // Throttle frames control
        ret.put("nextThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.nextThrottleFrame();
            }            
        });
        ret.put("previousThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.previousThrottleFrame();
            }            
        }); 
        ret.put("nextRunningThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.nextRunningThrottleFrame();
            }            
        });
        ret.put("previousRunningThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tw.previousRunningThrottleFrame();
            }            
        });        
        // Throttle windows control
        ret.put("nextThrottleWindow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForNextThrottleWindow();
            }            
        });
        ret.put("previousThrottleWindow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForPreviousThrottleWindow();
            }            
        });         
        return ret;        
    }
    
    private class fnActionPressed extends AbstractAction {
        private final int fn;

        fnActionPressed(int fn) {
            this.fn = fn;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
            if ((throttle!=null) && (throttle.getFunctionMomentary(fn) || ( !tw.getCurrentThrottleFrame().getFunctionPanel().getFunctionButtons()[fn].getIsLockable()))) {
                throttle.setFunction(fn, true );
            } 
        }                
    }
    
    private class fnActionReleased extends AbstractAction {
        private final int fn;

        fnActionReleased(int fn) {
            this.fn = fn;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
            if (throttle!=null) throttle.setFunction(fn, ! throttle.getFunction(fn));
        }                
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleWindowActionsFactory.class);        
}
