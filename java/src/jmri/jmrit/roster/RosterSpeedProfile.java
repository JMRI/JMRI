package jmri.jmrit.roster;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map;

import java.awt.event.ActionEvent;

import org.jdom.Element;
import java.util.List;

import jmri.DccThrottle;
import jmri.Section;
import jmri.Block;

/**
 * A simple class to store a speed profile for a given loco
 * The speed steps against the profile are on a scale of 0 to 1000, this equates to 
 * the float speed x 1000.  This allows a single profile to cover different throttle 
 * speed step settings.  So a profile generate for a loco using 28 steps can be used
 * for a throttle using 126 steps.
**/
public class RosterSpeedProfile {
    
    RosterEntry _re = null;
    
    float overRunTimeReverse = 0.0f;
    float overRunTimeForward = 0.0f;
    
    public RosterSpeedProfile(RosterEntry re){
        _re = re;
    }
    
    public RosterEntry getRosterEntry(){
        return _re;
    }
    
    public float getOverRunTimeForward(){
        return overRunTimeForward;
    }
    
    public void setOverRunTimeForward(float dt){
        overRunTimeForward=dt;
    }
    
    public float getOverRunTimeReverse(){
        return overRunTimeReverse;
    }
    
    public void setOverRunTimeReverse(float dt){
        overRunTimeReverse=dt;
    }
    
    /**
    * forward and reverse values are in meters per second
    */
    public void setSpeed(int speedStep, float forward, float reverse){
        //int iSpeedStep = Math.round(speedStep*1000);
        if(!speeds.containsKey(speedStep)){
            speeds.put(speedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(speedStep);
        ss.setForwardSpeed(forward);
        ss.setReverseSpeed(reverse);
    }
    
    public void setForwardSpeed(float speedStep, float forward){
        int iSpeedStep = Math.round(speedStep*1000);
        if(!speeds.containsKey(iSpeedStep)){
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);
        ss.setForwardSpeed(forward);
    }
    
    public void setReverseSpeed(float speedStep, float reverse){
        int iSpeedStep = Math.round(speedStep*1000);
        if(!speeds.containsKey(iSpeedStep)){
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);
        ss.setReverseSpeed(reverse);
    }
    
    /**
     * return the forward speed in milli-meters per second for a given speed step
     */
    public float getForwardSpeed(float speedStep){
        int iSpeedStep = Math.round(speedStep*1000);
        if(speeds.containsKey(iSpeedStep)){
            return speeds.get(iSpeedStep).getForwardSpeed();
        }
        log.debug("no exact match forward for " + iSpeedStep);
        float lower = 0;
        float higher = 0;
        int highStep = 0;
        int lowStep = 0;
        if(speeds.higherKey(iSpeedStep)!=null){
            highStep = speeds.higherKey(iSpeedStep);
            higher = speeds.get(highStep).getForwardSpeed();
        } else return -1.0f;
        if(speeds.lowerKey(iSpeedStep)!=null) {
            lowStep = speeds.lowerKey(iSpeedStep);
            lower = speeds.get(lowStep).getForwardSpeed();
        }
        
        float valperstep = (higher-lower)/(highStep-lowStep);
        
        float retValue = lower+(valperstep*(iSpeedStep-lowStep));
        return retValue;
    }
    
    /**
     * return the reverse speed in milli-meters per second for a given speed step
     */    
    public float getReverseSpeed(float speedStep){
        int iSpeedStep = Math.round(speedStep*1000);
        if(speeds.containsKey(iSpeedStep)){
            return speeds.get(iSpeedStep).getReverseSpeed();
        }
        log.debug("no exact match reverse for " + iSpeedStep);
        float lower = 0;
        float higher = 0;
        int highStep = 0;
        int lowStep = 0;
        if(speeds.higherKey(iSpeedStep)!=null){
            highStep = speeds.higherKey(iSpeedStep);
            higher = speeds.get(highStep).getForwardSpeed();
        } else return -1.0f;
        if(speeds.lowerKey(iSpeedStep)!=null) {
            lowStep = speeds.lowerKey(iSpeedStep);
            lower = speeds.get(lowStep).getForwardSpeed();
        }
        
        float valperstep = (higher-lower)/(highStep-lowStep);
        
        float retValue = lower+(valperstep*(iSpeedStep-lowStep));
        return retValue;
    }
    
    /**
    * return the approximate duration in seconds that a loco may travel for a given speed step
    */
    public float getDurationOfTravelInSeconds(boolean isForward, float speedStep, int distance){
        float spd = getForwardSpeed(speedStep);
        if(!isForward){
            spd = getReverseSpeed(speedStep);
        }
        return (distance/spd);
    }
    
    /**
    * return the approximate distance travelled in millimeters for a give duration in seconds and speed step.
    */
    public float getDistanceTravelled(boolean isForward, float speedStep, float duration){

        float spd = getForwardSpeed(speedStep);
        
        if(!isForward){
            spd = getReverseSpeed(speedStep);
        }
        
        return Math.abs(spd*duration);
    }
    
    float distanceRemaining = 0;
    
    TreeMap<Integer, SpeedStep>  speeds= new TreeMap<Integer, SpeedStep>();
    
    DccThrottle _throttle;
    
    float stepIncrement = 1;
    float tIncrement = 0.0f;

    float desiredSpeedStep=-1;
    
    float extraDelay = 0.0f;
    
    Section section = null;
    Block block = null;
    
    javax.swing.Timer stopTimer = null;
    
    long lastTimeSpeedFinished = 0l;
    long lastTimeTimerStarted = 0l;
    
    //Add in  a method to record how long through a speed step change we are in.
    //Also  add in a method to determin e how long  (time) it has been that we last finished a change of speed.
    //Called after the stopTimer has finished, calculates distance travelled and then starts on the next change of speed
    void changeSpeedStep(){
        if(_throttle.getSpeedSetting()==desiredSpeedStep){
            log.debug("At desired Speed step");
            finishChange();
            lastTimeSpeedFinished=System.nanoTime();
            return;
        }
        float duration = ((float)(stopTimer.getDelay()+extraTime)/1000);
        //log.info("Cal " + duration);
        //log.info("Distance travelled at step " + _throttle.getSpeedSetting() + " " + getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), duration));
        distanceRemaining = distanceRemaining-getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), duration);
        
        changeLocoSpeedByStep(distanceRemaining, desiredSpeedStep);
    }
    
    boolean increaseSpeed = false;
    
    /**
    * reset everything back to default once the change has finished.
    */
    void finishChange(){
        if(stopTimer!=null)
            stopTimer.stop();
        stopTimer=null;
        _throttle = null;
        distanceRemaining = 0;
        stepIncrement = 0.0f;
        desiredSpeedStep=-1;
        tIncrement = 0.0f;
        cancel = false;
        extraDelay = 0.0f;
        section = null;
        block = null;
    }
    
    public void setExtraInitialDelay(float eDelay){
        extraDelay = eDelay;
    }
    
    /**
     *   Set speed of a throttle to a speeed set by a float, using the block for the length details
     */
    public void changeLocoSpeed(DccThrottle t, Block blk, float speed){
        float blockLength = blk.getLengthMm();
        if(blk==block){
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float)(System.nanoTime()-lastTimeTimerStarted)/1000000000));
            blockLength = distanceRemaining;
            //Not entirely reliable at this stage as the loco could still be running and not completed the calculation of the distance, this could result in an over run
            log.debug("Block passed is the same as we are currently processing");
        } else {
            block = blk;
        }
        changeLocoSpeed(t, blockLength, speed);
    
    }
    /**
     *   Set speed of a throttle to a speeed set by a float, using the section for the length details
     */
    //@TODO if a section contains multiple blocks then we could calibrate the change of speed based upon the block status change.
    public void changeLocoSpeed(DccThrottle t, Section sec, float speed){
        if(sec==section && speed == desiredSpeedStep){
            if(log.isDebugEnabled()) log.debug("Already setting to desired speed step for this section");
            return;
        }
        float sectionLength = sec.getActualLength();
        if(log.isDebugEnabled()) log.debug("call to change speed via section " + sec.getDisplayName());
        if (sec==section){
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float)(System.nanoTime()-lastTimeTimerStarted)/1000000000));
            sectionLength = distanceRemaining;
        } else {
            section = sec;
        }
        changeLocoSpeed(t, sectionLength, speed);
    }
    
    /**
    * Set speed by float increment of a speed step.
    */ 
    public void changeLocoSpeed(DccThrottle t, float distance, float speed){
        if(log.isDebugEnabled()) log.debug("Call to change speed over specific distance float " + speed + " distance " + distance);
        if(speed==t.getSpeedSetting()){ 
            if(log.isDebugEnabled()) log.debug("Throttle and request speed setting are the same " + speed + " " + t.getSpeedSetting() + " so will quit");
            //Already at correct speed setting
            finishChange();
            return;
        }
        //int iSpeedStep = Math.round(speed*1000);
        
        if(desiredSpeedStep == speed){
            if(log.isDebugEnabled()) log.debug("Already setting to desired speed step");
            //finishChange();
            return;
        }
        if(log.isDebugEnabled()) log.debug("public change speed step by float " + speed);
        if(log.isDebugEnabled()) log.debug("Desired Speed Step " + desiredSpeedStep + " asked for " + speed);

        if(stopTimer!=null){
            if(log.isDebugEnabled()) log.debug("stop timer valid so will cancel");
            cancelSpeedChange();
        }
        _throttle=t;
        stepIncrement = _throttle.getSpeedIncrement();
        if(log.isDebugEnabled()) log.debug("Desired Speed Step " + desiredSpeedStep + " asked for " + speed);
        desiredSpeedStep = speed;
        //int step = Math.round(_throttle.getSpeedSetting()*1000);
        if(log.isDebugEnabled()) log.debug("calculated current step " + _throttle.getSpeedSetting() + " required " + speed + " current " + _throttle.getSpeedSetting() + " increment " + stepIncrement);
        /*if(step==iSpeedStep){
            if(log.isDebugEnabled()) log.debug("Already at best speed ");
            finishChange();
            return;
        }else*/ if(_throttle.getSpeedSetting()<speed){
            increaseSpeed = true;
            if(log.isDebugEnabled()) log.debug("Going for acceleration");
        } else {
            increaseSpeed = false;
            if(log.isDebugEnabled()) log.debug("Going for deceleration");
        }
        
        cancel = false;
        changeLocoSpeedByStep(distance, speed);
    }
    
    void changeLocoSpeedByStep(float distance, float speedStep){
        if(log.isDebugEnabled()) log.debug("=========================================================");
        long startTime = System.nanoTime();
        if(cancel)
            return;
        if(distance<=0){
            if(log.isDebugEnabled()) log.debug("Distance is less than 0 " + distance);
            _throttle.setSpeedSetting(speedStep);
            finishChange();
            return;
        }
        
        distanceRemaining = distance;
        if(stopTimer!=null){
            stopTimer.stop();
        }
        
        float tCurrentSpeed = _throttle.getSpeedSetting();

        stepIncrement = _throttle.getSpeedIncrement();
        
        //int step = Math.round(tCurrentSpeed*1000);
        //if(log.isDebugEnabled()) log.debug("calculated step " + step + " required " + speedStep);

        float spd = 0;
        float endspd = 0;
        //This needs to use floor or higher
        if(tCurrentSpeed!=0.0){ // current speed
            if(_throttle.getIsForward()){
                spd = getForwardSpeed(tCurrentSpeed);
                if(speedStep>0)
                    endspd = getForwardSpeed(speedStep);
            } else {
                spd = getReverseSpeed(tCurrentSpeed);
                if(speedStep>0)
                    endspd = getReverseSpeed(speedStep);
            }
        } else if (speedStep!=0.0) {
            if(_throttle.getIsForward()){
                endspd = getForwardSpeed(speedStep);
            } else {
                endspd = getReverseSpeed(speedStep);
            }
        }

        if(log.isDebugEnabled()) log.debug("end spd " + endspd + " spd " + spd);
        double avgSpeed = Math.abs((endspd+spd)*0.5);
        if(log.isDebugEnabled()) log.debug("avg Speed " + avgSpeed);
        
        double time = (distance/avgSpeed); //in seconds
        time = time *1000; //covert it to milli seconds
        if(log.isDebugEnabled()) log.debug("time before remove over run " + time);
        if(stopTimer == null){ //At the start we will deduct the over run time if configured
            if(log.isDebugEnabled()) log.debug("Stop timer not configured so will add overrun " + distanceRemaining);
            if(_throttle.getIsForward()){
                float extraAsDouble = (getOverRunTimeForward()+extraDelay)/1000;
                if(log.isDebugEnabled()){
                    log.debug("Over run time to remove (Reverse) " + getOverRunTimeReverse());
                    log.debug(extraAsDouble);
                }
                float olddistance = getDistanceTravelled(true, tCurrentSpeed, extraAsDouble);
                distanceRemaining = distanceRemaining - olddistance;
                time = time-getOverRunTimeForward();
            } else {
                float extraAsDouble = (getOverRunTimeReverse()+extraDelay)/1000;
                if(log.isDebugEnabled()){
                    log.debug("Over run time to remove (Reverse) " + getOverRunTimeReverse());
                    log.debug(extraAsDouble);
                }
                float olddistance = getDistanceTravelled(false, tCurrentSpeed, extraAsDouble);
                distanceRemaining = distanceRemaining - olddistance;
                time = time-getOverRunTimeReverse();
            }
            if(log.isDebugEnabled()){
                log.debug("Distance remaining " + distanceRemaining);
                log.debug("Time after overrun removed " + time);
            }
        } else {
            if(log.isDebugEnabled()) log.debug("Stop timer configured so will not add overrun");
        }
        
        float speeddiff = _throttle.getSpeedSetting()-desiredSpeedStep;
        float noSteps = speeddiff/stepIncrement;
        if(log.isDebugEnabled()) log.debug("Speed diff " + speeddiff + " number of Steps " + noSteps + " step increment " + stepIncrement);

        int timePerStep = Math.abs((int)(time/noSteps));
        
        if(_throttle.getSpeedSetting()>(_throttle.getSpeedIncrement()*2)){
            //We do not get reliable time results if the duration per speed step is less than 500ms
            //therefore we calculate how many speed steps will fit in to 750ms.
            if(timePerStep<=500  && timePerStep>0){
                //thing tIncrement should be different not sure about this bit
                float tmp = (750.0f/timePerStep);
                stepIncrement = stepIncrement*tmp;
                if(log.isDebugEnabled()) log.debug("time per step was " + timePerStep + " no of increments in 750 ms is " + tmp + " new step increment in " + stepIncrement);
                
                timePerStep = 750;
            }
        }
        if(log.isDebugEnabled()) log.debug("per interval " + timePerStep);
        float speedtoset = _throttle.getSpeedSetting();
        //Calculate the new speed setting
        if(increaseSpeed){
            speedtoset = speedtoset + stepIncrement;
            if(speedtoset > 1.0f)
                speedtoset = 1.0f;
            if(_throttle.getSpeedSetting()>desiredSpeedStep)
                speedtoset=desiredSpeedStep;
        } else {
            speedtoset = speedtoset - stepIncrement;
            if(speedtoset<_throttle.getSpeedIncrement())
                speedtoset = 0.0f;
        }
        if(log.isDebugEnabled()) log.debug("Speed Step current " + _throttle.getSpeedSetting() + " speed to set " + speedtoset);
        
        _throttle.setSpeedSetting(speedtoset);
        long finishTime = System.nanoTime();
        extraTime = ((int)(finishTime-startTime)/1000000);
        //Extra time covers how long it has taken this method to compute and is then taken off of the time we wait before the next speed step.
        //This might be better to remove the the potential distance travelled.
        timePerStep = timePerStep-extraTime;
        stopTimer = new javax.swing.Timer(timePerStep, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                changeSpeedStep();
            }
        });
        stopTimer.setRepeats(false);
        if(cancel){
            finishChange();
            return;
        }
        lastTimeTimerStarted = System.nanoTime();
        stopTimer.start();
        
    }
    int extraTime = 0;
    
    void stopLocoTimeOut(DccThrottle t){
        log.debug("Stopping loco");
        t.setSpeedSetting(0f);
    }
    
    boolean cancel = false;
    
    /**
    *  This method is called to cancel the existing change in speed.
    */
    public void cancelSpeedChange(){
        cancel = true;
        if(stopTimer!=null && stopTimer.isRunning()){
            stopTimer.stop();
        }
        finishChange();
    }
    
    
    public void store(Element e){
        Element d = new Element("speedprofile");
        d.addContent(new Element("overRunTimeForward").addContent(Float.toString(getOverRunTimeForward())));
        d.addContent(new Element("overRunTimeReverse").addContent(Float.toString(getOverRunTimeReverse())));
        Element s = new Element("speeds");
        for(Integer i : speeds.keySet()){
            Element ss = new Element("speed");
            ss.addContent(new Element("step").addContent(""+i));
            ss.addContent(new Element("forward").addContent(Float.toString(speeds.get(i).getForwardSpeed())));
            ss.addContent(new Element("reverse").addContent(Float.toString(speeds.get(i).getReverseSpeed())));
            s.addContent(ss);
        }
        d.addContent(s);
        e.addContent(d);
    }
    
    @SuppressWarnings({ "unchecked" })
    public void load(Element e){
        try{
            setOverRunTimeForward(Float.parseFloat(e.getChild("overRunTimeForward").getText()));
        } catch (Exception ex){
            log.error("Over run Error For " + _re.getId());
        }
        try {
            setOverRunTimeReverse(Float.parseFloat(e.getChild("overRunTimeReverse").getText()));
        } catch (Exception ex){
            log.error("Over Run Error Rev " + _re.getId());
        }        
        Element speeds = e.getChild("speeds");
        List<Element> speedlist = speeds.getChildren("speed");
        for(Element spd:speedlist){
            try{
                String step = spd.getChild("step").getText();
                String forward = spd.getChild("forward").getText();
                String reverse = spd.getChild("reverse").getText();
                setSpeed(Integer.parseInt(step), Float.parseFloat(forward), Float.parseFloat(reverse));
            } catch (Exception ex){
                log.error("Not loaded");
            }
        }
    }
    
    static class SpeedStep {
        
        float forward = 0.0f;
        float reverse = 0.0f;
        
        SpeedStep(){
        }
        
        void setForwardSpeed(float speed){
            forward = speed;
        }
        
        void setReverseSpeed(float speed){
            reverse = speed;
        }
        
        float getForwardSpeed(){
            return forward;
        }
        
        float getReverseSpeed(){
            return reverse;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterSpeedProfile.class.getName());
}