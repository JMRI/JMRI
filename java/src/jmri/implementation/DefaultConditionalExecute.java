package jmri.implementation;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import javax.swing.Timer;

import jmri.*;
import jmri.implementation.DefaultConditional.TimeSensor;
import jmri.implementation.DefaultConditional.TimeTurnout;
import jmri.jmrit.Sound;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.script.JmriScriptEngineManager;
import jmri.script.ScriptOutput;

/**
 * Helper class for DefaultConditional that executes the  actions of a
 * DefaultConditional.
 * @author Daniel Bergqvist (C) 2021
 */
public class DefaultConditionalExecute {

    private final DefaultConditional conditional;

    DefaultConditionalExecute(@Nonnull DefaultConditional conditional) {
        this.conditional = conditional;
    }

    void setTurnout(@Nonnull ConditionalAction action, Turnout t, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (t == null) {
            errorList.add("invalid turnout name in action - " + action.getDeviceName());  // NOI18N
        } else {
            int act = action.getActionData();
            if (act == Route.TOGGLE) {
                int state = t.getKnownState();
                if (state == Turnout.CLOSED) {
                    act = Turnout.THROWN;
                } else {
                    act = Turnout.CLOSED;
                }
            }
            t.setCommandedState(act);
            increaseCounter(actionCount);
        }
    }

    void delayedTurnout(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull TimeTurnout timeTurnout, boolean reset, String devName) {
        if (reset) action.stopTimer();
        if (!action.isTimerActive()) {
            // Create a timer if one does not exist
            Timer timer = action.getTimer();
            if (timer == null) {
                action.setListener(timeTurnout);
                timer = new Timer(2000, action.getListener());
                timer.setRepeats(true);
            }
            // Start the Timer to set the turnout
            int value = conditional.getMillisecondValue(action);
            if (value < 0) {
                return;
            }
            timer.setInitialDelay(value);
            action.setTimer(timer);
            action.startTimer();
            increaseCounter(actionCount);
        } else {
            log.warn("timer already active on request to start delayed turnout action - {}", devName);
        }
    }

    void cancelTurnoutTimers(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        ConditionalManager cmg = jmri.InstanceManager.getDefault(jmri.ConditionalManager.class);
        java.util.Iterator<Conditional> iter = cmg.getNamedBeanSet().iterator();
        while (iter.hasNext()) {
            String sname = iter.next().getSystemName();

            Conditional c = cmg.getBySystemName(sname);
            if (c == null) {
                errorList.add("Conditional null during cancel turnout timers for "  // NOI18N
                        + action.getDeviceName());
                continue; // no more processing of this one
            }

            c.cancelTurnoutTimer(devName);
            increaseCounter(actionCount);
        }
    }

    void lockTurnout(@Nonnull ConditionalAction action, Turnout tl, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (tl == null) {
            errorList.add("invalid turnout name in action - " + action.getDeviceName());  // NOI18N
        } else {
            int act = action.getActionData();
            if (act == Route.TOGGLE) {
                if (tl.getLocked(Turnout.CABLOCKOUT)) {
                    act = Turnout.UNLOCKED;
                } else {
                    act = Turnout.LOCKED;
                }
            }
            if (act == Turnout.LOCKED) {
                tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            } else if (act == Turnout.UNLOCKED) {
                tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            }
            increaseCounter(actionCount);
        }
    }

    void setSignalAppearance(@Nonnull ConditionalAction action, SignalHead h, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (h == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            h.setAppearance(action.getActionData());
            increaseCounter(actionCount);
        }
    }

    void setSignalHeld(@Nonnull ConditionalAction action, SignalHead h, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (h == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            h.setHeld(true);
            increaseCounter(actionCount);
        }
    }

    void clearSignalHeld(@Nonnull ConditionalAction action, SignalHead h, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (h == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            h.setHeld(false);
            increaseCounter(actionCount);
        }
    }

    void setSignalDark(@Nonnull ConditionalAction action, SignalHead h, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (h == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            h.setLit(false);
            increaseCounter(actionCount);
        }
    }

    void setSignalLit(@Nonnull ConditionalAction action, SignalHead h, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (h == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            h.setLit(true);
            increaseCounter(actionCount);
        }
    }

    void triggerRoute(@Nonnull ConditionalAction action, Route r, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (r == null) {
            errorList.add("invalid Route name in action - " + action.getDeviceName());  // NOI18N
        } else {
            r.setRoute();
            increaseCounter(actionCount);
        }
    }

    void setSensor(@Nonnull ConditionalAction action, Sensor sn, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        if (sn == null) {
            errorList.add("invalid Sensor name in action - " + action.getDeviceName());  // NOI18N
        } else {
            int act = action.getActionData();
            if (act == Route.TOGGLE) {
                int state = sn.getState();
                if (state == Sensor.ACTIVE) {
                    act = Sensor.INACTIVE;
                } else {
                    act = Sensor.ACTIVE;
                }
            }
            try {
                sn.setKnownState(act);
                increaseCounter(actionCount);
            } catch (JmriException e) {
                log.warn("Exception setting Sensor {} in action", devName);  // NOI18N
            }
        }
    }

    void delayedSensor(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull TimeSensor timeSensor, int delay, boolean reset, String devName) {
        if (reset) action.stopTimer();
        if (!action.isTimerActive()) {
            // Create a timer if one does not exist
            Timer timer = action.getTimer();
            if (timer == null) {
                action.setListener(timeSensor);
                timer = new Timer(2000, action.getListener());
                timer.setRepeats(true);
            }
            // Start the Timer to set the sensor
            if (delay < 0) {
                return;
            }
            timer.setInitialDelay(delay);
            action.setTimer(timer);
            action.startTimer();
            increaseCounter(actionCount);
        } else {
            log.warn("timer already active on request to start delayed sensor action - {}", devName);
        }
    }

    void cancelSensorTimers(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        ConditionalManager cm = jmri.InstanceManager.getDefault(jmri.ConditionalManager.class);
        java.util.Iterator<Conditional> itr = cm.getNamedBeanSet().iterator();
        while (itr.hasNext()) {
            String sname = itr.next().getSystemName();
            Conditional c = cm.getBySystemName(sname);
            if (c == null) {
                errorList.add("Conditional null during cancel sensor timers for "  // NOI18N
                        + action.getDeviceName());
                continue; // no more processing of this one
            }

            c.cancelSensorTimer(devName);
            increaseCounter(actionCount);
        }
    }

    void setLight(@Nonnull ConditionalAction action, Light lgt, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (lgt == null) {
            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
        } else {
            int act = action.getActionData();
            if (act == Route.TOGGLE) {
                int state = lgt.getState();
                if (state == Light.ON) {
                    act = Light.OFF;
                } else {
                    act = Light.ON;
                }
            }
            lgt.setState(act);
            increaseCounter(actionCount);
        }
    }

    void setLightIntensity(@Nonnull ConditionalAction action, Light lgt, int intensity, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (lgt == null) {
            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
        } else {
            try {
                if (intensity < 0) {
                    return;
                }
                if (lgt instanceof VariableLight) {
                    ((VariableLight)lgt).setTargetIntensity((intensity) / 100.0);
                } else {
                    lgt.setState(intensity > 0.5 ? Light.ON : Light.OFF);
                }
                increaseCounter(actionCount);
            } catch (IllegalArgumentException e) {
                errorList.add("Exception in set light intensity action - " + action.getDeviceName());  // NOI18N
            }
        }
    }

    void setLightTransitionTime(@Nonnull ConditionalAction action, Light lgt, int time, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (lgt == null) {
            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
        } else {
            try {
                if (time  < 0) {
                    return;
                }
                if (lgt instanceof VariableLight) {
                    ((VariableLight)lgt).setTransitionTime(time );
                }
                increaseCounter(actionCount);
            } catch (IllegalArgumentException e) {
                errorList.add("Exception in set light transition time action - " + action.getDeviceName());  // NOI18N
            }
        }
    }

    void setMemory(@Nonnull ConditionalAction action, Memory m, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (m == null) {
            errorList.add("invalid memory name in action - " + action.getDeviceName());  // NOI18N
        } else {
            m.setValue(action.getActionString());
            increaseCounter(actionCount);
        }
    }

    void copyMemory(@Nonnull ConditionalAction action, Memory mFrom, Memory mTo, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (mFrom == null) {
            errorList.add("invalid memory name in action - " + action.getDeviceName());  // NOI18N
        } else {
            if (mTo == null) {
                errorList.add("invalid memory name in action - " + action.getActionString());  // NOI18N
            } else {
                mTo.setValue(mFrom.getValue());
                increaseCounter(actionCount);
            }
        }
    }

    void enableLogix(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        Logix x = InstanceManager.getDefault(jmri.LogixManager.class).getLogix(devName);
        if (x == null) {
            errorList.add("invalid logix name in action - " + action.getDeviceName());  // NOI18N
        } else {
            x.setEnabled(true);
            increaseCounter(actionCount);
        }
    }

    void disableLogix(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        Logix x = InstanceManager.getDefault(jmri.LogixManager.class).getLogix(devName);
        if (x == null) {
            errorList.add("invalid logix name in action - " + action.getDeviceName());  // NOI18N
        } else {
            x.setEnabled(false);
            increaseCounter(actionCount);
        }
    }

    void playSound(@Nonnull ConditionalAction action, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        String path = actionStr;
        if (!path.equals("")) {
            Sound sound = action.getSound();
            if (sound == null) {
                try {
                    sound = new Sound(path);
                } catch (NullPointerException ex) {
                    errorList.add("invalid path to sound: " + path);  // NOI18N
                }
            }
            if (sound != null) {
                sound.play();
            }
            increaseCounter(actionCount);
        }
    }

    void runScript(@Nonnull ConditionalAction action, String actionStr, @Nonnull Reference<Integer> actionCount) {
        if (!(actionStr.equals(""))) {
            JmriScriptEngineManager.getDefault().runScript(new File(jmri.util.FileUtil.getExternalFilename(actionStr)));
            increaseCounter(actionCount);
        }
    }

    @SuppressWarnings({"deprecation"})  // date.setHours, date.setMinutes, date.setSeconds
    void setFastClockTime(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount) {
        Date date = InstanceManager.getDefault(jmri.Timebase.class).getTime();
        date.setHours(action.getActionData() / 60);
        date.setMinutes(action.getActionData() - ((action.getActionData() / 60) * 60));
        date.setSeconds(0);
        InstanceManager.getDefault(jmri.Timebase.class).userSetTime(date);
        increaseCounter(actionCount);
    }

    void startFastClock(@Nonnull Reference<Integer> actionCount) {
        InstanceManager.getDefault(jmri.Timebase.class).setRun(true);
        increaseCounter(actionCount);
    }

    void stopFastClock(@Nonnull Reference<Integer> actionCount) {
        InstanceManager.getDefault(jmri.Timebase.class).setRun(false);
        increaseCounter(actionCount);
    }

    void controlAudio(@Nonnull ConditionalAction action, String devName) {
        Audio audio = InstanceManager.getDefault(jmri.AudioManager.class).getAudio(devName);
        if (audio == null) {
            return;
        }
        if (audio.getSubType() == Audio.SOURCE) {
            AudioSource audioSource = (AudioSource) audio;
            switch (action.getActionData()) {
                case Audio.CMD_PLAY:
                    audioSource.play();
                    break;
                case Audio.CMD_STOP:
                    audioSource.stop();
                    break;
                case Audio.CMD_PLAY_TOGGLE:
                    audioSource.togglePlay();
                    break;
                case Audio.CMD_PAUSE:
                    audioSource.pause();
                    break;
                case Audio.CMD_RESUME:
                    audioSource.resume();
                    break;
                case Audio.CMD_PAUSE_TOGGLE:
                    audioSource.togglePause();
                    break;
                case Audio.CMD_REWIND:
                    audioSource.rewind();
                    break;
                case Audio.CMD_FADE_IN:
                    audioSource.fadeIn();
                    break;
                case Audio.CMD_FADE_OUT:
                    audioSource.fadeOut();
                    break;
                case Audio.CMD_RESET_POSITION:
                    audioSource.resetCurrentPosition();
                    break;
                default:
                    break;
            }
        } else if (audio.getSubType() == Audio.LISTENER) {
            AudioListener audioListener = (AudioListener) audio;
            switch (action.getActionData()) {
                case Audio.CMD_RESET_POSITION:
                    audioListener.resetCurrentPosition();
                    break;
                default:
                    break; // nothing needed for others
            }
        }
    }

    void jythonCommand(@Nonnull ConditionalAction action, String actionStr, @Nonnull Reference<Integer> actionCount) {
        if (!(actionStr.isEmpty())) {
            // add the text to the output frame
            ScriptOutput.writeScript(actionStr);
            // and execute

            javax.script.ScriptEngine se =  JmriScriptEngineManager.getDefault().getEngine(JmriScriptEngineManager.JYTHON);
            if (se!=null) {
                try {
                    JmriScriptEngineManager.getDefault().eval(actionStr, se);
                } catch (ScriptException ex) {
                    log.error("Error executing script:", ex);  // NOI18N
                }
            } else {
                log.error("Error getting default ScriptEngine");
            }
            increaseCounter(actionCount);
        }
    }

    void allocateWarrantRoute(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            String msg = w.allocateRoute(false, null);
            if (msg != null) {
                log.info("Warrant {} - {}", action.getDeviceName(), msg);  // NOI18N
            }
            increaseCounter(actionCount);
        }
    }

    void deallocateWarrantRoute(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            w.deAllocate();
            increaseCounter(actionCount);
        }
    }

    void setRouteTurnouts(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            String msg = w.setRoute(false, null);
            if (msg != null) {
                log.info("Warrant {} unable to Set Route - {}", action.getDeviceName(), msg);  // NOI18N
            }
            increaseCounter(actionCount);
        }
    }

    void setTrainId(@Nonnull ConditionalAction action, Warrant w, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            if(!w.getSpeedUtil().setAddress(actionStr)) {
                errorList.add("invalid train ID in action - " + action.getDeviceName());  // NOI18N
            }
            increaseCounter(actionCount);
        }
    }

    void setTrainName(@Nonnull ConditionalAction action, Warrant w, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            w.setTrainName(actionStr);
            increaseCounter(actionCount);
        }
    }

    void autoRunWarrant(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            jmri.jmrit.logix.WarrantTableFrame frame = jmri.jmrit.logix.WarrantTableFrame.getDefault();
            String err = frame.runTrain(w, Warrant.MODE_RUN);
            if (err != null) {
                errorList.add("runAutoTrain error - " + err);  // NOI18N
                w.stopWarrant(true, true);
            }
            increaseCounter(actionCount);
        }
    }

    void manualRunWarrant(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            String err = w.setRoute(false, null);
            if (err == null) {
                err = w.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
            }
            if (err != null) {
                errorList.add("runManualTrain error - " + err);  // NOI18N
            }
            increaseCounter(actionCount);
        }
    }

    void controlTrain(@Nonnull ConditionalAction action, Warrant w, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        if (w == null) {
            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
        } else {
            if (!w.controlRunTrain(action.getActionData())) {
                log.info("Train {} not running  - {}", w.getSpeedUtil().getRosterId(), devName);  // NOI18N
            }
            increaseCounter(actionCount);
        }
    }

    void setSignalMastAspect(@Nonnull ConditionalAction action, SignalMast f, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (f == null) {
            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
        } else {
            f.setAspect(actionStr);
            increaseCounter(actionCount);
        }
    }

    void setSignalMastHeld(@Nonnull ConditionalAction action, SignalMast f, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (f == null) {
            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
        } else {
            f.setHeld(true);
            increaseCounter(actionCount);
        }
    }

    void clearSignalMastHeld(@Nonnull ConditionalAction action, SignalMast f, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (f == null) {
            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
        } else {
            f.setHeld(false);
            increaseCounter(actionCount);
        }
    }

    void setSignalMastDark(@Nonnull ConditionalAction action, SignalMast f, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (f == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            f.setLit(false);
            increaseCounter(actionCount);
        }
    }

    void setSignalMastLit(@Nonnull ConditionalAction action, SignalMast f, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (f == null) {
            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
        } else {
            f.setLit(true);
            increaseCounter(actionCount);
        }
    }

    void setBlockValue(@Nonnull ConditionalAction action, OBlock b, String actionStr, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.setValue(actionStr);
            increaseCounter(actionCount);
        }
    }

    void setBlockError(@Nonnull ConditionalAction action, OBlock b, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.setError(true);
            increaseCounter(actionCount);
        }
    }

    void clearBlockError(@Nonnull ConditionalAction action, OBlock b, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.setError(false);
        }
    }

    void deallocateBlock(@Nonnull ConditionalAction action, OBlock b, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.deAllocate(null);
            increaseCounter(actionCount);
        }
    }

    void setBlockOutOfService(@Nonnull ConditionalAction action, OBlock b, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.setOutOfService(true);
            increaseCounter(actionCount);
        }
    }

    void setBlockInService(@Nonnull ConditionalAction action, OBlock b, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList) {
        if (b == null) {
            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
        } else {
            b.setOutOfService(false);
            increaseCounter(actionCount);
        }
    }

    void setNXPairEnabled(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
        if (dp == null) {
            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
        } else {
            dp.setEnabled(true);
            increaseCounter(actionCount);
        }
    }

    void setNXPairDisabled(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
        if (dp == null) {
            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
        } else {
            dp.setEnabled(false);
            increaseCounter(actionCount);
        }
    }

    void setNXPairSegment(@Nonnull ConditionalAction action, @Nonnull Reference<Integer> actionCount, @Nonnull List<String> errorList, String devName) {
        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
        if (dp == null) {
            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
        } else {
            jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                    setSingleSegmentRoute(devName);
            increaseCounter(actionCount);
        }
    }

    private void increaseCounter(@Nonnull Reference<Integer> actionCount) {
        // actionCount.get() is never null, but Spotbugs doesn't know that
        Integer value = actionCount.get();
        actionCount.set(value != null ? value+1 : 0);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalExecute.class);
}
