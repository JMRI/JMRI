package jmri.jmrit.throttle.actions;

import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.jmrit.throttle.implementation.ThrottleFrame;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;

/**
 * Actions implementation for a Throttle Window
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
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

    public ThrottleWindowActionsFactory(ThrottleControllersUIContainer tw) {
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
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                incrementSpeed(throttle, throttle.getSpeedIncrement());
            }            
        });
        ret.put("decelerate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                incrementSpeed(throttle, -throttle.getSpeedIncrement());
            }            
        });
        ret.put("accelerateMore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                incrementSpeed(throttle, throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
            }            
        });
        ret.put("decelerateMore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                incrementSpeed(throttle, -throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
            }            
        });        
        ret.put("idle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                if (throttle!=null) throttle.setSpeedSetting(0);
            }            
        });
        ret.put("stop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                if (throttle!=null) throttle.setSpeedSetting(-1);
            }            
        });
        ret.put("forward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                if (throttle!=null) throttle.setIsForward(true);
            }            
        });
        ret.put("reverse", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                if (throttle!=null) throttle.setIsForward(false);
            }            
        });
        ret.put("switchDirection", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
                if (throttle!=null) throttle.setIsForward(!throttle.getIsForward());
            }            
        });         
        
        // function buttons
        for (int i=0; i < tpwkc.getNbFunctionsKeys(); i++) {
            ret.put("fn_"+i+"_Pressed", new FnActionPressed(i));
            ret.put("fn_"+i+"_Released", new FnActionReleased(i));
        }
        
        // Throttle inner window cycling
        ret.put("nextJInternalFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw.getCurentThrottleController() instanceof ThrottleFrame) {
                    ((ThrottleFrame)tw.getCurentThrottleController()).activateNextJInternalFrame();
                }                
            }            
        });
        ret.put("previousJInternalFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw.getCurentThrottleController() instanceof ThrottleFrame) {
                    ((ThrottleFrame)tw.getCurentThrottleController()).activatePreviousJInternalFrame();
                }                
            }            
        });
        ret.put("showControlPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw.getCurentThrottleController() instanceof ThrottleFrame) {
                    toFront(((ThrottleFrame)tw.getCurentThrottleController()).getControlPanelJIF());
                }
            }            
        });
        ret.put("showFunctionPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw.getCurentThrottleController() instanceof ThrottleFrame) {
                    toFront(((ThrottleFrame)tw.getCurentThrottleController()).getFunctionPanelJIF());
                }
            }            
        });
        ret.put("showAddressPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw.getCurentThrottleController() instanceof ThrottleFrame) {
                    toFront(((ThrottleFrame)tw.getCurentThrottleController()).getAddressPanelJIF());
                }
            }            
        });
        
        // Throttle frames control
        ret.put("nextThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw instanceof ThrottleWindow) {
                    ((ThrottleWindow)tw).nextThrottleFrame();
                }
            }            
        });
        ret.put("previousThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw instanceof ThrottleWindow) {
                    ((ThrottleWindow)tw).previousThrottleFrame();
                }
            }            
        }); 
        ret.put("nextRunningThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw instanceof ThrottleWindow) {
                    ((ThrottleWindow)tw).nextRunningThrottleFrame();
                }
            }            
        });
        ret.put("previousRunningThrottleFrame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tw instanceof ThrottleWindow) {
                    ((ThrottleWindow)tw).previousRunningThrottleFrame();
                }
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
    
    private class FnActionPressed extends AbstractAction {
        private final int fn;

        FnActionPressed(int fn) {
            this.fn = fn;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            DccThrottle throttle = tw.getCurentThrottleController().getFunctionThrottle();
            if ((throttle!=null) && (throttle.getFunctionMomentary(fn))) {
                throttle.setFunction(fn, true );
            } 
        }                
    }
    
    private class FnActionReleased extends AbstractAction {
        private final int fn;

        FnActionReleased(int fn) {
            this.fn = fn;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            DccThrottle throttle = tw.getCurentThrottleController().getFunctionThrottle();
            if (throttle!=null) {
                throttle.setFunction(fn, ! throttle.getFunction(fn));
            }
        }                
    }
}
