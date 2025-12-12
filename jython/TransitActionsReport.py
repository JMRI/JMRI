# TransitActionsReport.py -- Print the transit section actions.
#
# The output will be in the "Script Output" window, or the JMRI system console if the window is not open.
#
# Author:  Dave Sand copyright (c) 2025

import java
import jmri

def getWhen(action):
    code = action.getWhenCode()
    delay = action.getDataWhen()
    data = action.getStringWhen()

    if code == jmri.TransitSectionAction.ENTRY:
        return 'On Entry to this Section' if delay == 0 else '"{}" mSec after entering this Section'.format(delay)

    elif code == jmri.TransitSectionAction.EXIT:
        return 'On Exit from this Section' if delay == 0 else '"{}" mSec after exiting this Section'.format(delay)

    elif code == jmri.TransitSectionAction.BLOCKENTRY:
        return 'On Entry to Block "{}"'.format(data) if delay == 0 else '"{}" mSec after entering Block "{}"'.format(delay, data)

    elif code == jmri.TransitSectionAction.BLOCKEXIT:
        return 'On Exit from Block "{}"'.format(data) if delay == 0 else '"{}" mSec after exiting Block "{}"'.format(delay, data)

    elif code == jmri.TransitSectionAction.TRAINSTOP:
        return 'When train stops moving' if delay == 0 else '"{}" mSec after train stops moving'.format(delay)

    elif code == jmri.TransitSectionAction.TRAINSTART:
        return 'When train starts moving' if delay == 0 else '"{}" mSec after train starts moving'.format(delay)

    elif code == jmri.TransitSectionAction.SENSORACTIVE:
        return 'When Sensor "{}" becomes ACTIVE'.format(data) if delay == 0 else '"{}" mSec after Sensor "{}" becomes ACTIVE'.format(delay, data)

    elif code == jmri.TransitSectionAction.SENSORINACTIVE:
        return 'When Sensor "{}" becomes INACTIVE'.format(data) if delay == 0 else '"{}" mSec after Sensor "{}" becomes INACTIVE'.format(delay, data)

    elif code == jmri.TransitSectionAction.PRESTARTDELAY:
        return 'Pre-start'

    elif code == jmri.TransitSectionAction.PRESTARTACTION:
        return '"{}" mSec after Start of Delay'.format(delay)

    else:
        return code

def getWhat(action):
    code = action.getWhatCode()
    data1 = action.getDataWhat1()
    data2 = action.getDataWhat2()
    text = action.getStringWhat()

    if code == jmri.TransitSectionAction.PAUSE:
        return 'Pause for "{}" fast minutes'.format(data1)

    elif code == jmri.TransitSectionAction.SETMAXSPEED:
        return 'Set maximum train speed to {0} percent'.format(data1)

    elif code == jmri.TransitSectionAction.SETCURRENTSPEED:
        return 'Set train speed to {} percent'.format(data1)

    elif code == jmri.TransitSectionAction.RAMPTRAINSPEED:
        return 'Ramp speed to {} percent'.format(data1)

    elif code == jmri.TransitSectionAction.TOMANUALMODE:
        return 'Change to manual throttle. No done Sensor.' if len(text) == 0 else 'Change to manual throttle. Done Sensor "{}"'.format(text)

    elif code == jmri.TransitSectionAction.SETLIGHT:
        return 'Set locomotive light "{}"'.format(text)

    elif code == jmri.TransitSectionAction.STARTBELL:
        return 'Start bell (if sound decoder)'

    elif code == jmri.TransitSectionAction.STOPBELL:
        return 'Stop bell (if sound decoder)'

    elif code == jmri.TransitSectionAction.SOUNDHORN:
        return 'Sound horn for {} mSec'.format(data1)

    elif code == jmri.TransitSectionAction.SOUNDHORNPATTERN:
        return 'Sound horn pattern "{}" short={} long={}'.format(text, data1, data2)

    elif code == jmri.TransitSectionAction.LOCOFUNCTION:
        return 'Set decoder function "{}" to "{}"'.format(data1, text)

    elif code == jmri.TransitSectionAction.SETSENSORACTIVE:
        return 'Set Sensor "{}" to ACTIVE'.format(text)

    elif code == jmri.TransitSectionAction.SETSENSORINACTIVE:
        return 'Set Sensor "{}" to INACTIVE'.format(text)

    elif code == jmri.TransitSectionAction.HOLDSIGNAL:
        return 'Hold Signal "{}"'.format(text)

    elif code == jmri.TransitSectionAction.RELEASESIGNAL:
        return 'Release Signal "{}"'.format(text)

    elif code == jmri.TransitSectionAction.ESTOP:
        return 'E Stop'

    elif code == jmri.TransitSectionAction.PRESTARTRESUME:
        return 'Delay Start for "{}" mSec'.format(action.getDataWhen())

    elif code == jmri.TransitSectionAction.TERMINATETRAIN:
        return 'Terminate Train'

    elif code == jmri.TransitSectionAction.FORCEALLOCATEPASSSAFESECTION:
        return 'Try to allocate pass next safe section.'

    elif code == jmri.TransitSectionAction.LOADTRAININFO:
        if data2 == jmri.TransitSectionAction.LOCOADDRESSTYPEROSTER:
            return 'Load TrainInfo "{}" with Roster "{}"'.format(text, action.getStringWhat2())

        elif data2 == jmri.TransitSectionAction.LOCOADDRESSTYPENUMBER:
            return 'Load TrainInfo "{}" with Number "{}"'.format(text, action.getStringWhat2())

        elif data2 == jmri.TransitSectionAction.LOCOADDRESSTYPEDEFAULT:
            return 'Load TrainInfo "{}" with Current Number.'.format(text)

        else:
            return 'Load TrainInfo "{}"'.format(text)

    else:
        return code


for transit in transits.getNamedBeanSet():
    transitName = transit.getDisplayName()
    print '---- transit = {}'.format(transitName)

    for transitSection in transit.getTransitSectionList():
        sectionName = transitSection.getSection().getDisplayName()
        sequence = transitSection.getSequenceNumber()
        first = True
        for action in transitSection.getTransitSectionActionList():
            if first:
                print '  section = {} :: {}'.format(sequence, sectionName)
                first = False
            when = getWhen(action)
            what = getWhat(action)

            print '    when :: {}'.format(when)
            print '    what :: {}\n'.format(what)
