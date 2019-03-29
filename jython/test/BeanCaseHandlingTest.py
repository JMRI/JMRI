# Test script-level access to NamedBeans, specifically how Internal
# objects can be named

import jmri

list = []

tn1a = turnouts.provideTurnout("ITname1")
list.append(tn1a)
if (tn1a is None ) : raise AssertionError('tn1a None')
tn1b = turnouts.provideTurnout("ITname1")
list.append(tn1b)
if (tn1b is None ) : raise AssertionError('tn1b None')
if (tn1a != tn1b) : raise AssertionError("tn1a and tn1b didn't match")

# case is checked
tN1 = turnouts.provideTurnout("ITNAME1")
list.append(tN1)
if (tn1a == tN1) : raise AssertionError("tn1a matches tN1, case not handled right")

# spaces fine, kept
tSpaceM  = turnouts.provideTurnout("ITNAME 1")
if (tSpaceM in list) : raise AssertionError("tSPaceM not unique")
list.append(tSpaceM)

tSpaceMM = turnouts.provideTurnout("ITNAME  1")
if (tSpaceMM in list) : raise AssertionError("tSpaceMM not unique")
list.append(tSpaceMM)

tSpaceE  = turnouts.provideTurnout("ITNAME 1 ")
if (tSpaceE in list) : raise AssertionError("tSpaceE not unique")
list.append(tSpaceE)

tSpaceEE  = turnouts.provideTurnout("ITNAME 1  ")
if (tSpaceEE in list) : raise AssertionError("tSpaceEE not unique")
list.append(tSpaceEE)

