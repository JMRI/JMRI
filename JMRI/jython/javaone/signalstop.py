# stop on signals

def setStopBlock(block, signal,dir) :
    s = signals.getSignalHead(signal)
    b = jmri.jmrit.tracker.StoppingBlock(block)
    b.addSignal(s,dir)
    return b
def setStopBlock2(block, signal1, signal2, dir) :
    s1 = signals.getSignalHead(signal1)
    s2 = signals.getSignalHead(signal2)
    b = jmri.jmrit.tracker.StoppingBlock(block)
    b.addSignal(s1, s2, dir)
    return b

SB157 = setStopBlock2(IB157,"200 Facing Upper", "200 Facing Lower", CCW)
SB158 = setStopBlock(IB158,"200 Main",CW)
SB159 = setStopBlock(IB159,"200 Siding",CW)

SB150 = setStopBlock2(IB150,"201 Facing Upper", "201 Facing Lower", CW)
SB160 = setStopBlock(IB160,"201 Main",CCW)
SB161 = setStopBlock(IB161,"201 Siding",CCW)

SB156 = setStopBlock2(IB156,"202 Facing Upper", "202 Facing Lower", CW)
SB154 = setStopBlock(IB154,"202 Main",CCW)
SB155 = setStopBlock(IB155,"202 Siding",CCW)

SB151 = setStopBlock2(IB151,"203 Facing Upper", "203 Facing Lower", CCW)
SB152 = setStopBlock(IB152,"203 Main",CW)
SB153 = setStopBlock(IB153,"203 Siding",CW)

stopblocks = [SB157, SB158, SB159, SB150, SB160, SB161, 
              SB156, SB154, SB155, SB151, SB152, SB153]


