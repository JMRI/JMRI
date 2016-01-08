class ADschedule:
    # Encapsulates all info relevant to a schedule

    # CONSTANTS
    
    # Action types
    # Actions that require train stopping
    ERROR = -2
    END_ALTERNATIVE = -1
    STOP = 0
    PAUSE = 1
    START_AT = 2
    CCW = 3
    CW = 4
    WAIT_FOR = 5
    IFE = 6
    IFAT = 7
    IFH = 8
    MANUAL_PRESENT = 9
    # Actions that can be executed while train is running
    GOTO = 10
    SWON = 11
    SWOFF = 12
    HELD = 13
    RELEASE = 14
    SET_F_ON = 15
    SET_F_OFF = 16
    DELAY = 17
    MANUAL_OTHER = 18
    SOUND = 19
    TC = 20
    TT = 21
    
    def __init__(self, text):
        # Save original text
        self.text = text
        self.source = []
        textSpace  = text.replace(",", " ")
        # Break down input text into tokens
        splitted = textSpace.split()
        # Break down tokens containing open brackets "("
        charList = ")[]"
        for s in splitted:
            i = s.find("(")
            while i >= 0 and i < len(s)-1:
                self.__split(s[:i + 1], charList)
                s = s[i + 1:]
                i = s.find("(")
            self.__split(s, charList)
        self.__clearFields()

    def __split(self, s, charList):
        # Internal method
        # Recursively breaks down tokens containing special characters 
        # (open/closed brackets)
        if charList == "":
            self.source.append(s)
        else:
            term = charList[0]
            if len(charList) > 1:
                charList = charList[1:]
            else:
                charList = ""
            i = s.find(term)
            while i >= 0 and len(s) > 1:
                if i > 0:
                    self.__split(s[:i], charList)
                    s = s[i:]
                if len(s) > 1:
                    self.source.append(term)
                    s = s[1:]
                i = s.find(term)
            self.__split(s, charList)

    def __clearFields(self):
        # Set initial status of fields, in order to start schedule scanning
        self.pointer = 0
        self.iteration = 0
        self.iterations = 0
        self.stack = []
        self.error = False
        self.alternative = False
        self.repeating = False
        self.test = False
        self.condition = True
        self.ifStart = False
        self.endAlternative = 0
        self.currentItem = self.__getNextItem()
        self.looping = False

    def __getNextItem(self):
        # Get next item in the schedule (internal method)
        self.firstCall = True
        newItem = ADscheduleItem()
        # Are we at the beginning of a $IF?
        if self.ifStart:
            # Skip commands until condition becomes True
            count = 0
            while not self.condition:
                if self.pointer >= len(self.source):
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "$IF not closed by $END"
                    return newItem
                sl = self.source[self.pointer]
                s = sl.upper()
                self.pointer += 1
                if s.startswith("$IF"):
                    # Nested IF
                    count += 1
                elif s == "$END":
                    # Decrease number of nested IFs
                    count -= 1
                    if count < 0:
                        # End of main IF reached
                        self.pop()
                        self.condition = True
                elif s == "$ELSE":
                    if count == 0:
                        # ELSE of main IF reached
                        self.condition = True
            self.ifStart = False
        # End of schedule?
        if self.pointer >= len(self.source):
            return newItem
        # No, get next token?
        sl = self.source[self.pointer]
        s = sl.upper()
        self.pointer += 1
        if s.endswith("("):
            # Start of repetition
            self.push()
            self.test = False
            self.alternative = False
            self.repeating = True
            self.iteration = 0
            if len(s) > 1:
                s = s[:len(s)-1]
                try:
                    self.iterations = int(s)
                except:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Wrong value \"" + sl + "\""
                    return newItem
            return self.__getNextItem()
        if s == ")":
            # End of repetition
            if not self.repeating:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unbalanced close bracket \")\""
                self.looping = True
                return newItem
            # Should we iterate?
            self.iteration += 1
            # Is this an endless loop?
            self.looping = self.iterations == 0
            # Should we repeat?
            if self.looping or self.iteration < self.iterations:
                # Repeat again
                self.pointer = self.stack[len(self.stack)-1]
                if self.looping:
                    self.iteration = 0
                return self.__getNextItem()
            # Repetition completed
            self.pop()
            return self.__getNextItem()
        if s == "[":
            # Start of alternative
            if self.alternative:
                newItem.action = ADschedule.ERROR
                newItem.message = "Nested square brackets \"[\" are not supported!"
                self.looping = True
                return newItem
            self.push()
            self.alternative = True
            self.test = False
            self.repeating = False
            return self.__getNextItem()
        if s == "]":
            # End of alternative
            if not self.alternative:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unbalanced close bracket \"]\""
                return newItem
            self.endAlternative = self.pointer
            self.pointer = self.stack[len(self.stack)-1]
            newItem.action = ADschedule.END_ALTERNATIVE
            return newItem
        # Test for $ prefixed commands
        if s.startswith("$IF"):
            # Start of test
                self.push()
                self.test = True
                self.alternative = False
                self.repeating = False
                i = s.find(":")
                if s.startswith("$IFH"):
                    newItem.action = ADschedule.IFH
                    if s == "$IFH":
                        newItem.value = None
                        self.condition = True
                        self.ifStart = True
                        return newItem
                    if i < 0:
                        self.error = True
                        newItem.action = ADschedule.ERROR
                        newItem.message = "Wrong format \"" + sl + "\""
                        return newItem
                    self.__getSignal(sl, newItem)
                    if newItem.action == ADschedule.ERROR:
                        self.error = True
                    else:
                        self.condition = True
                        self.ifStart = True
                    return newItem
                if i < 0:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Wrong format \"" + sl + "\""
                    return newItem
                newItem.value = self.__getArgs(sl[i + 1:])
                if len(newItem.value) == 0:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Wrong/missing section name \"" + sl + "\""
                    return newItem
                if s.startswith("$IFAT:"):
                    newItem.action = ADschedule.IFAT
                    self.condition = True
                    self.ifStart = True
                    return newItem
                elif s.startswith("$IFE:"):
                    newItem.action = ADschedule.IFE
                    self.condition = True
                    self.ifStart = True
                    return newItem
                else:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Unknown command \"" + sl + "\""
                    return newItem
        if s == "$ELSE":
            # Third term of test
            if not self.test:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "$ELSE not preceded by $IF"
                return newItem
            self.condition = False
            # Skip tokens untile $END is found
            count = 0
            while not self.condition:
                if self.pointer >= len(self.source):
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "$IF not closed by $END"
                    return newItem
                sl = self.source[self.pointer]
                s = sl.upper()
                self.pointer += 1
                if s.startswith("$IF"):
                    # Nested IF
                    count += 1
                elif s == "$END":
                    # Decrease number of nested IFs
                    count -= 1
                    if count < 0:
                        # End of main IF reached
                        self.pop()
                        self.condition = True
                elif s == "$ELSE":
                    if count == 0:
                        # Too many ELSE
                        self.error = True
                        newItem.action = ADschedule.ERROR
                        newItem.message = "$ELSE not preceded by $IF"
                        return newItem
            return self.__getNextItem()
        if s == "$END":
            # End of test
            if not self.test:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "$END not preceded by $IF"
                return newItem
            self.pop()
            return self.__getNextItem()
        # $Pn - pause n seconds.  $Dn delay n seconds
        if s.startswith("$P") or s.startswith("$D"):
            st = s
            try:
                st = s[2:]
            except:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing value \"" + sl + "\""
                return newItem
            if st.startswith("M"):
                try:
                    st = st[1:]
                except:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Missing value \"" + sl + "\""
                    return newItem
                useFastClock = True        
            else:
                useFastClock = False        
            try:
                newItem.value = float(st)
            except:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong value \"" + sl + "\""
                return newItem
            if useFastClock:
                newItem.value = (newItem.value * 60.
                                 / AutoDispatcher.fastBase.getRate())
            if s.startswith("$P"):         
                newItem.action = ADschedule.PAUSE
            else:
                newItem.action = ADschedule.DELAY
            return newItem
        # Direction change
        if (s == "$CCW" or s == "$EAST" or s == "$NORTH" or s == "$LEFT"
            or s == "$UP"):
            newItem.action = ADschedule.CCW
            return newItem
        if (s == "$CW" or s == "$WEST" or s == "$SOUTH" or s == "RIGHT"
            or s == "DOWN"):
            newItem.action = ADschedule.CW
            return newItem
        # Switching mode
        # Allows train to enter restricted tracks
        # i.e. ONE-WAY and TRANSIT-ONLY
        if s == "$SWON":
            newItem.action = ADschedule.SWON
            return newItem
        if s == "$SWOFF":
            newItem.action = ADschedule.SWOFF
            return newItem
        # Signals control
        newItem.action = ADschedule.ERROR
        if s.startswith("$H:"):
            # $H:signalName sets a signal to "Held" state
            newItem.action = ADschedule.HELD
            self.__getSignal(sl, newItem)
            self.error = newItem.action == ADschedule.ERROR
            return newItem
        elif s.startswith("$R:"):
            # $R:signalName removes the "Held" state
            newItem.action = ADschedule.RELEASE
            self.__getSignal(sl, newItem)
            self.error = newItem.action == ADschedule.ERROR
            return newItem
        # Decoder functions (F0-F28)
        if s.startswith("$ON:F"):
            newItem.action = ADschedule.SET_F_ON
            newItem.value = s[5:]
        elif s.startswith("$OFF:F"):
            newItem.action = ADschedule.SET_F_OFF
            newItem.value = s[6:]
        if newItem.action != ADschedule.ERROR:
            # Retrieve $ON $OFF argument (function number)
            try:
                newItem.value = int(newItem.value)
                if newItem.value < 0 or newItem.value > 28:
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Function number out of range \"" + sl + "\""
            except:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong/missing function number \"" + sl + "\""
            return newItem
        # Wait for empty section
        if s.startswith("$WF:"):
            st = sl
            try:
                st = sl[4:]
            except:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing section name \"" + sl + "\""
                return newItem
            newItem.value = ADsection.getByName(st)
            if newItem.value == None:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown section \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.WAIT_FOR
            return newItem
        # Set present section to manual mode
        if s == "$M":
            newItem.action = ADschedule.MANUAL_PRESENT
            return newItem
        # Set another section to manual mode
        if s.startswith("$M:"):
            try:
                newItem.value = ADsection.getByName(sl[3:])
            except:
                newItem.value = None
            if newItem.value == None:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown/missing section \"" + sl + "\""
                self.looping = True
                return newItem
            newItem.action = ADschedule.MANUAL_OTHER
            return newItem
        if s.startswith("$S:"):
        # Play sound
            try:
                newItem.value = ADsettings.soundDic.get(sl[3:], None)
            except:
                newItem.value = None
            if newItem.value == None:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown/missing sound \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.SOUND
            return newItem
        # Set turnout
        if s.startswith("$TC:") or s.startswith("$TT:"):
            try:
                newItem.value = sl[4:]
            except:
                newItem.value = None
            if newItem.value == None or newItem.value == "":
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing turnout name \"" + sl + "\""
                return newItem
            if s.startswith("$TC:"):
                newItem.action = ADschedule.TC
            else:
                newItem.action = ADschedule.TT
            return newItem
        # Start time (using Fast Clock)
        if s.startswith("$ST:"):
            try:
                minutes = hours = 0
                time = sl[4:]
                i = time.find(":")
                if i < 0:
                    hours = int(time)
                else:
                    hours = time[0:i]
                    hours = int (hours)
                    minutes = time[i + 1:]
                    minutes = int (minutes)
                newItem.value = hours * 60 + minutes
            except:
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong time value \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.START_AT
            return newItem
        #If no command prefixed by $ was found
        # argument should be a section name
        newItem.value = ADsection.getByName(sl)
        if newItem.value == None:
            self.error = True
            newItem.action = ADschedule.ERROR
            if sl.startswith("$"):
                newItem.message = "Unknown command \"" + sl + "\""
            else:
                newItem.message = "Unknown section \"" + sl + "\""
            return newItem
        newItem.action = ADschedule.GOTO
        return newItem

    def __getArgs(self, arg):
        # Get arguments for commands expecting a section name or
        # a list of section names
        argList = []
        if arg != "":
            # Argument is a single section name
            arg = ADsection.getByName(arg)
            if arg != None:
                argList.append(arg)
        else:
            # Argument is a list of section names
            newItem = self.__getNextItem()
            while newItem.action == ADschedule.GOTO:
                argList.append(newItem.value)
                newItem = self.__getNextItem()
            self.next()
            self.pointer -= 1
            self.alternative = False
        return argList

    def __getSignal(self, arg, newItem):
        # Get argument for commands expecting a signal name
            i = arg.find(":")
            try:
                signalName = arg[i + 1:]
            except:
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing signal name \"" + arg + "\""
                return
            # retrieve signal
            newItem.value = ADsignalMast.getByName(signalName)
            if newItem.value == None:
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown signal \"" + arg + "\""
            return

    def getNextAlternative(self):
        # Loop among alternative destinations
        # i.e. list of destinations enclosed in square brackets "[...]"
        outItem = self.currentItem
        if self.error:
            return outItem
        if self.alternative:
            self.currentItem = self.__getNextItem()
            if self.error:
                return self.currentItem
        elif (not self.firstCall) and self.currentItem.action == ADschedule.GOTO:
            outItem = ADscheduleItem()
            outItem.action = ADschedule.END_ALTERNATIVE
            outItem.value = 0
        self.firstCall = not self.firstCall
        return outItem
    
    def next(self):
        # step to next item (or next list of alternatives)
        if self.error:
            self.currentItem.action = ADschedule.ERROR
            return      
        if self.alternative:
            self.pointer = self.endAlternative
            self.pop()
        self.currentItem = self.__getNextItem()
        
    def getFirstAlternative(self):
        # Get the first alternative destination
        # i.e. first destination enclosed in square brackets "[x...]"
        if self.error:
            return self.currentItem
        if self.alternative:
            while self.currentItem.action == ADschedule.GOTO:
                self.currentItem = self.__getNextItem()
                if self.error:
                    return self.currentItem
            self.currentItem = self.__getNextItem()
        else:
            self.firstCall = True
        return self.getNextAlternative()
        
    def testCondition(self, item, section, direction):
        # Set test results for $IFAT and $IFE
        condition = False
        if item.action == ADschedule.IFAT:
            # Test for current section
            for arg in item.value:
                if arg == section:
                    condition = True
                    break
        elif item.action == ADschedule.IFE:
            # Test for empty section
            for arg in item.value:
                if arg.isAvailable():
                    condition = True
                    break
        # Set test results for $IFH
        elif item.action == ADschedule.IFH:
            signal = item.value
            if signal == None:
                signal = section.getSignal(direction)
            condition = signal.isHeld()
        else:
            # No $IF command - return present item
            return item
        # Make sure that we are at the beginning of a test
        if self.ifStart:
            # Apply test results
            self.condition = condition

        # Return next schedule item
        item.action = ADschedule.END_ALTERNATIVE
        # return self.getFirstAlternative()
        return item

    def push(self):
        # Internal method - pushes status into the internal stack
        self.stack.append(self.ifStart)
        self.stack.append(self.condition)
        self.stack.append(self.test)
        self.stack.append(self.endAlternative)
        self.stack.append(self.alternative)
        self.stack.append(self.repeating)
        self.stack.append(self.iterations)
        self.stack.append(self.iteration)
        self.stack.append(self.pointer)

    def pop(self):
        # Internal method - pops status from the internal stack
        # skip pointer (must be restored manually, if needed)
        self.stack.pop()    
        self.iteration = self.stack.pop()
        self.iterations = self.stack.pop()
        self.repeating = self.stack.pop()
        self.alternative = self.stack.pop()
        self.endAlternative = self.stack.pop()
        self.test = self.stack.pop()
        self.condition = self.stack.pop()
        self.ifStart = self.stack.pop()
        
    def match(self, section, direction):
        # Try to find the first occurence of a section in the schedule
        self.__clearFields()
        minDistance = 0
        while True:
            if self.looping:
                break
            item = self.getFirstAlternative()
            item = self.testCondition(item, section, direction)
            while item.action == ADschedule.GOTO:
                if item.value == section:
                    # Move to next destination
                    self.next()
                    return
                # Try and find a route to present destination
                route = ADautoRoute(section, item.value, direction, False)
                # Take note of route length
                routeLength = len(route.step)
                if (routeLength > 0 and (minDistance == 0 or 
                    routeLength < minDistance)):
                    minDistance = routeLength
                item = self.getNextAlternative()
            if item.action == ADschedule.ERROR:
                self.__clearFields()
                self.currentItem.action = ADschedule.ERROR
                self.currentItem.message = item.message
                self.error = True
                return
            if item.action == ADschedule.STOP:
                break
            self.next()
        # No explicit reference to section found
        self.__clearFields()
        # Did we find at least one route?
        if minDistance == 0:
            return
        # At least one route found - synchronize with it
        while True:
            if self.looping:
                # Should not occur
                break
            item = self.getFirstAlternative()
            while item.action  == ADschedule.GOTO:
                # Get the route to present destination
                route = ADautoRoute(section, item.value, direction, False)
                # Has it the required length?
                if len(route.step) == minDistance:
                    return
                item = self.getNextAlternative()
            if item.action == ADschedule.ERROR:
                # Should not occur
                self.__clearFields()
                self.currentItem.action = ADschedule.ERROR
                self.error = True
                return
            if item.action == ADschedule.STOP:
                # Should not occur
                break
            self.next()
        # Just in case (we should never get here)
        self.__clearFields()
        

class ADscheduleItem:
    # Encapsulates info relevant to a schedule item
    def __init__(self):
        self.action = ADschedule.STOP
        self.value = 0
        self.message = ""
        
