# define start, stop operations

def setturnout(sensor, turnout, msg) :
    if sensors.getSensor(sensor).state == INACTIVE :
        turnouts.getTurnout(turnout).state = CLOSED
    else :
        warn().display(msg)
        return False     
    return True

def setturnouts() :
    return ( 
        setturnout("LS162", "LT200", "Move train off of front-left turnout") and
        setturnout("LS163", "LT201", "Move train off of front-right turnout") and
        setturnout("LS164", "LT202", "Move train off of back-left turnout") and
        setturnout("LS165", "LT203", "Move train off of back-right turnout") 
    )
    
def setblocks() :
    # train A
    if sensors.getSensor(initSensorA).state == ACTIVE :
        initBlockA.setValue(throttleA)
    else :
        warn().display("Put the "+nameA+" locomotive in its start location")
        return False     

    # train B
    if sensors.getSensor(initSensorB).state == ACTIVE :
        initBlockB.setValue(throttleB)
    else :
        warn().display("Put the "+nameB+" locomotive in its start location")
        return False     

    # check other sensors
    tempsensors = tracksensors[:]
    tempsensors.remove(initSensorA)
    tempsensors.remove(initSensorB)
    for x in tempsensors :
        if sensors.getSensor(x).state == ACTIVE :
            warn().display("Track sensor "+sensors.getSensor(x).systemName+" is active and shouldn't be")
            return False
    
    return True

    
def start() :
    print "Attempt to start layout"
    # set turnouts if possible
    if not setturnouts() : 
        # here if failed
        print "Turnouts are not OK"
        startstopsensor.setState(INACTIVE)
        return  False # end of processing
    print "Turnouts set OK"
    
    # check occupancy & starting locations
    if not setblocks() :
        # here if failed
        print "Locomotives are not in starting positions"
        startstopsensor.setState(INACTIVE)
        return  False # end of processing
    print "Locomotives set OK"
    
    # step blocks to restart
    for b in stopblocks :
        b.setSpeeds(slow, fast)

    # set initial speed, direction, functions
    if throttleA != None :
        throttleA.setIsForward(True)
        throttleA.setSpeedSetting(fast)
        throttleA.setF0(True)
        
    if throttleB != None :
        throttleB.setIsForward(True)
        throttleB.setSpeedSetting(fast)
        throttleB.setF0(True)

    # done, auto running should be happening
    return True

def stop() :
    print "Stopping"
    # set speeds to zero, sounds off
    if throttleA != None :
        throttleA.setSpeedSetting(0)
        throttleA.setF1(False)
        throttleA.setF2(False)
        
    if throttleB != None :
        throttleB.setSpeedSetting(0)
        throttleB.setF1(False)
        throttleB.setF2(False)

    # step blocks to not restart
    for b in stopblocks :
        b.setSpeeds(0, 0)
        
    return True
