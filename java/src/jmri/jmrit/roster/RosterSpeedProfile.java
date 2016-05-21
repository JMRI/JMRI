package jmri.jmrit.roster;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import jmri.Block;
import jmri.DccThrottle;
import jmri.NamedBean;
import jmri.Section;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    public void clearCurrentProfile(){
        speeds= new TreeMap<Integer, SpeedStep>();
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
        float spd = 0f;
        if(isForward){
            spd = getForwardSpeed(speedStep);
        } else {
            spd = getReverseSpeed(speedStep);
        }
        return (distance/spd);
    }
    
    /**
    * return the approximate distance travelled in millimeters for a give duration in seconds and speed step.
    */
    public float getDistanceTravelled(boolean isForward, float speedStep, float duration){
        float spd = 0f;
        if(isForward){
            spd = getForwardSpeed(speedStep);
        } else {
            spd = getReverseSpeed(speedStep);
        }
        return Math.abs(spd*duration);
    }
    
    float distanceRemaining = 0;
    float distanceTravelled = 0;
    
    TreeMap<Integer, SpeedStep>  speeds= new TreeMap<Integer, SpeedStep>();
    
    DccThrottle _throttle;
    
    float desiredSpeedStep=-1;
    
    float extraDelay = 0.0f;
    
    NamedBean referenced = null;
    
    javax.swing.Timer stopTimer = null;
    
    long lastTimeTimerStarted = 0l;
    
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
        desiredSpeedStep=-1;
        extraDelay = 0.0f;
        referenced = null;
        synchronized(this){
            distanceTravelled = 0;
            stepQueue = new LinkedList<SpeedSetting>();
        }
    }
    
    public void setExtraInitialDelay(float eDelay){
        extraDelay = eDelay;
    }
    
    /**
     *   Set speed of a throttle to a speeed set by a float, using the block for the length details
     */
    public void changeLocoSpeed(DccThrottle t, Block blk, float speed){
        if(blk==referenced && speed == desiredSpeedStep){
            //if(log.isDebugEnabled()) log.debug("Already setting to desired speed step for this block");
            return;
        }
        float blockLength = blk.getLengthMm();
        if(blk==referenced){
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float)(System.nanoTime()-lastTimeTimerStarted)/1000000000));
            blockLength = distanceRemaining;
            //Not entirely reliable at this stage as the loco could still be running and not completed the calculation of the distance, this could result in an over run
            log.debug("Block passed is the same as we are currently processing");
        } else {
            referenced = blk;
        }
        changeLocoSpeed(t, blockLength, speed);
    
    }
    /**
     *   Set speed of a throttle to a speeed set by a float, using the section for the length details
     */
    //@TODO if a section contains multiple blocks then we could calibrate the change of speed based upon the block status change.
    public void changeLocoSpeed(DccThrottle t, Section sec, float speed){
        if(sec==referenced && speed == desiredSpeedStep){
            if(log.isDebugEnabled()) log.debug("Already setting to desired speed step for this section");
            return;
        }
        float sectionLength = sec.getActualLength();
        if(log.isDebugEnabled()) log.debug("call to change speed via section " + sec.getDisplayName());
        if (sec==referenced){
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float)(System.nanoTime()-lastTimeTimerStarted)/1000000000));
            sectionLength = distanceRemaining;
        } else {
            referenced = sec;
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
        
        if(desiredSpeedStep == speed){
            if(log.isDebugEnabled()) log.debug("Already setting to desired speed step");
            return;
        }
        if(log.isDebugEnabled()) log.debug("public change speed step by float " + speed);
        if(log.isDebugEnabled()) log.debug("Desired Speed Step " + desiredSpeedStep + " asked for " + speed);

        if(stopTimer!=null){
            if(log.isDebugEnabled()) log.debug("stop timer valid so will cancel");
            cancelSpeedChange();
        }
        _throttle=t;
        
        if(log.isDebugEnabled()) log.debug("Desired Speed Step " + desiredSpeedStep + " asked for " + speed);
        desiredSpeedStep = speed;
        
        if(log.isDebugEnabled()) log.debug("calculated current step " + _throttle.getSpeedSetting() + " required " + speed + " current " + _throttle.getSpeedSetting());
        if(_throttle.getSpeedSetting()<speed){
            increaseSpeed = true;
            if(log.isDebugEnabled()) log.debug("Going for acceleration");
        } else {
            increaseSpeed = false;
            if(log.isDebugEnabled()) log.debug("Going for deceleration");
        }
        
        calculateStepDetails(speed, distance);
    }
    
    int extraTime = 0;
    
    void calculateStepDetails(float speedStep, float distance){
    
        float stepIncrement = _throttle.getSpeedIncrement();
        if(log.isDebugEnabled()) log.debug("Desired Speed Step " + desiredSpeedStep + " asked for " + speedStep);
        desiredSpeedStep = speedStep;
        //int step = Math.round(_throttle.getSpeedSetting()*1000);
        if(log.isDebugEnabled()) log.debug("calculated current step " + _throttle.getSpeedSetting() + " required " + speedStep + " current " + _throttle.getSpeedSetting() + " increment " + stepIncrement);
        boolean increaseSpeed = false;
        if(_throttle.getSpeedSetting()<speedStep){
            increaseSpeed = true;
            if(log.isDebugEnabled()) log.debug("Going for acceleration");
        } else {
            if(log.isDebugEnabled()) log.debug("Going for deceleration");
        }
        
        if(distance<=0){
            if(log.isDebugEnabled()) log.debug("Distance is less than 0 " + distance);
            _throttle.setSpeedSetting(speedStep);
            finishChange();
            return;
        }
        
        float calculatedDistance = distance;
        
        if(stopTimer!=null){
            stopTimer.stop();
            distanceRemaining = distance;
        } else {
            calculatedDistance = calculateInitialOverRun(distance);
            distanceRemaining = calculatedDistance;
        }
        
        float calculatingStep = _throttle.getSpeedSetting();
        
        float endspd = 0;
        if(calculatingStep!=0.0 && desiredSpeedStep>0){ // current speed
            if(_throttle.getIsForward()){
                endspd = getForwardSpeed(desiredSpeedStep);
            } else {
                endspd = getReverseSpeed(desiredSpeedStep);
            }
        } else if (desiredSpeedStep!=0.0) {
            if(_throttle.getIsForward()){
                endspd = getForwardSpeed(desiredSpeedStep);
            } else {
                endspd = getReverseSpeed(desiredSpeedStep);
            }
        }
        
        boolean calculated = false;
        
        while(!calculated){
            float spd = 0;
            if(calculatingStep!=0.0){ // current speed
                if(_throttle.getIsForward()){
                    spd = getForwardSpeed(calculatingStep);
                } else {
                    spd = getReverseSpeed(calculatingStep);
                }
            }
            
            if(log.isDebugEnabled()) log.debug("end spd " + endspd + " spd " + spd);
            double avgSpeed = Math.abs((endspd+spd)*0.5);
            if(log.isDebugEnabled()) log.debug("avg Speed " + avgSpeed);
        
            double time = (calculatedDistance/avgSpeed); //in seconds
            time = time *1000; //covert it to milli seconds
            /*if(stopTimer==null){
                if(log.isDebugEnabled()) log.debug("time before remove over run " + time);
                time = calculateInitialOverRun(time);//At the start we will deduct the over run time if configured
                if(log.isDebugEnabled()) log.debug("time after remove over run " + time);
            }*/
            float speeddiff = calculatingStep-desiredSpeedStep;
            float noSteps = speeddiff/stepIncrement;
            if(log.isDebugEnabled()) log.debug("Speed diff " + speeddiff + " number of Steps " + noSteps + " step increment " + stepIncrement);

            int timePerStep = Math.abs((int)(time/noSteps));
            float calculatedStepInc = stepIncrement;
            if(calculatingStep>(stepIncrement*2)){
                //We do not get reliable time results if the duration per speed step is less than 500ms
                //therefore we calculate how many speed steps will fit in to 750ms.
                if(timePerStep<=500  && timePerStep>0){
                    //thing tIncrement should be different not sure about this bit
                    float tmp = (750.0f/timePerStep);
                    calculatedStepInc = stepIncrement*tmp;
                    if(log.isDebugEnabled()) log.debug("time per step was " + timePerStep + " no of increments in 750 ms is " + tmp + " new step increment in " + calculatedStepInc);
                    
                    timePerStep = 750;
                }
            }
            if(log.isDebugEnabled()) log.debug("per interval " + timePerStep);
            
            //Calculate the new speed setting
            if(increaseSpeed){
                calculatingStep = calculatingStep + calculatedStepInc;
                if(calculatingStep > 1.0f){
                    calculatingStep = 1.0f;
                    calculated = true;
                }
                if(calculatingStep>desiredSpeedStep){
                    calculatingStep=desiredSpeedStep;
                    calculated=true;
                }
            } else {
                calculatingStep = calculatingStep - calculatedStepInc;
                if(calculatingStep<_throttle.getSpeedIncrement()){
                    calculatingStep = 0.0f;
                    calculated = true;
                    timePerStep = 0;
                }
                if (calculatingStep<desiredSpeedStep){
                    calculatingStep=desiredSpeedStep;
                    calculated=true;
                }
            }
            if(log.isDebugEnabled()) log.debug("Speed Step current " + _throttle.getSpeedSetting() + " speed to set " + calculatingStep);
            
            SpeedSetting ss = new SpeedSetting(calculatingStep, timePerStep);
            synchronized(this){
                stepQueue.addLast(ss);
            }
            if(stopTimer == null){ //If this is the first time round then kick off the speed change
                setNextStep();
            }
            
            calculatedDistance = calculatedDistance - getDistanceTravelled(_throttle.getIsForward(), calculatingStep, ((float)(timePerStep/1000.0)));
            
            if(calculatedDistance<0 && !calculated){
                log.error("distance remaining is now 0, but we have not reached desired speed setting " + desiredSpeedStep + " v " + calculatingStep);
                ss = new SpeedSetting(desiredSpeedStep, 10);
                synchronized(this){
                    stepQueue.addLast(ss);
                }
                calculated = true;
            }
        }
    }
    
    //The bit with the distance is not used
    float calculateInitialOverRun(float distance){
        if(log.isDebugEnabled()) log.debug("Stop timer not configured so will add overrun " + distance);
        if(_throttle.getIsForward()){
            float extraAsDouble = (getOverRunTimeForward()+extraDelay)/1000;
            if(log.isDebugEnabled()){
                log.debug("Over run time to remove (Forward) {}", getOverRunTimeForward());
                log.debug("{}", extraAsDouble);
            }
            float olddistance = getDistanceTravelled(true, _throttle.getSpeedSetting(), extraAsDouble);
            distance = distance - olddistance;
            //time = time-getOverRunTimeForward();
            //time = time-(extraAsDouble*1000);
        } else {
            float extraAsDouble = (getOverRunTimeReverse()+extraDelay)/1000;
            if(log.isDebugEnabled()){
                log.debug("Over run time to remove (Reverse) {}", getOverRunTimeReverse());
                log.debug("{}", extraAsDouble);
            }
            float olddistance = getDistanceTravelled(false, _throttle.getSpeedSetting(), extraAsDouble);
            distance = distance - olddistance;
            //time = time-getOverRunTimeReverse();
            //time = time-(extraAsDouble*1000);
        }
        if(log.isDebugEnabled()){
            log.debug("Distance remaining " + distance);
            //log.debug("Time after overrun removed " + time);
        }
        return distance;
    
    }
    
    void stopLocoTimeOut(DccThrottle t){
        log.debug("Stopping loco");
        t.setSpeedSetting(0f);
    }
    
    
    /**
    *  This method is called to cancel the existing change in speed.
    */
    public void cancelSpeedChange(){
        if(stopTimer!=null && stopTimer.isRunning()){
            stopTimer.stop();
        }
        finishChange();
    }
    
    synchronized void setNextStep(){
        if(stepQueue.isEmpty()){
            log.debug("No more results");
            finishChange();
            return;
        }
        SpeedSetting ss = stepQueue.getFirst();
        if(ss.getDuration()==0){
            _throttle.setSpeedSetting(0);
            finishChange();
            return;
        }
        if(stopTimer!=null){
            //Reduce the distanceRemaining and calculate the distance travelling
            float distanceTravelledThisStep = getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float)(stopTimer.getDelay()/1000.0)));
            distanceTravelled = distanceTravelled + distanceTravelledThisStep;
            distanceRemaining = distanceRemaining - distanceTravelledThisStep;
        }
        stepQueue.removeFirst();
        _throttle.setSpeedSetting(ss.getSpeedStep());
        stopTimer = new javax.swing.Timer(ss.getDuration(), new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setNextStep();
            }
        });
        stopTimer.setRepeats(false);
        lastTimeTimerStarted = System.nanoTime();
        stopTimer.start();
    
    }
    
    LinkedList<SpeedSetting> stepQueue = new LinkedList<SpeedSetting>();
    
    static class SpeedSetting{
        
        float step = 0.0f;
        int duration = 0;
        
        SpeedSetting(float step, int duration){
            this.step = step;
            this.duration = duration;
        }

        float getSpeedStep(){
            return step;
        }
        
        int getDuration(){
            return duration;
        }
    }
    
    /*
    * The follow deals with the storage and loading of the speed profile for a roster entry.
    */
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
    
    static Logger log = LoggerFactory.getLogger(RosterSpeedProfile.class);
}