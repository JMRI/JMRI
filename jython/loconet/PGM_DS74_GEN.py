# SCRIPT TO PROGRAM DS76 # GEN
# This is a script to help programming the Digitrax DS74.
#
# If you have a Digitrax DT602 throttle with a screen, it will allow you to enter or
# update the settings including routes.
# The DT602 will also allow you to display at the routes.  But without a DT602,
# it is very difficult to correctly enter the routes and to know you didn’t make an error.
# 
# So this script will allow you to create a text file (.TXT) with notepad, Editpad or
# other similar editor.  You then run this script providing the file name and
# it will read the file to update the DS74.
# 
# When it runs, it will pause and tell you to press the buttons on the DS74.
# You press the button (3 seconds) then press the space bar and it will send the
# necessary switch commands then pause again and ask you to press
# a button again then press the space bar.
# 
# The script runs as a script under JMRI.  Because of the way JMRI works, 
# before you start, go to [help] and start “system console”.  The messages
# are displayed here including errors found in the script and directions to
# press buttons on the DS74.  You then go to [scripting] and select "run
# script" and select this script and “open”.
# It will first marginally edit the input file, and display any errors. You can
# stop it and correct your errors and restart. However, once it starts,
# DO NOT do anything on the computer, even clicking on another window
# will interrupt and stop the script.
#  
# Once it has processed the file and starts the update, the first step will be
# to set CV40 to “C” to reset the DS74 to factory.  This way you rerun the
# script as needed to change or correct errors.
# 
# The format of the input file is simple and easy:
# 
# ID 200
# OPS CV 11
# OPS CV 16
# RTS 1 121T 201T 202C
# RTS 2 122C 203T 204C
# RTS 3 123C 205C 206T
# 
# The first record (ID) sets the ID (or actually the address of the first turnout).
# 
# The next records (OPS) set the CVs that need to be set to closed.  One per line.
# 
# The remaining records (RTS) set the routes.  The first parameter (1) indicates
# The route number or slot (1-8).  The next parameter (121T) is the route
# address and whether the route fires on the CLOSE or THROW command.
# The remaining are the addresses of the turnouts to be set and how they
# are set (closed or thrown).  
# 
# Disclaimer:
# This a temporary script.  Sometime not too long from now, the gurus at JMRI 
# will add DS74 support to JMRI.   When that happens, this will be obsolete
# and I and all my beautiful(???) work and great effort will fall back into oblivion.   
# 
# Also, I found that as I get closer to 80 and further from retirement date, that
# 50 yes in IT with procedural languages (COBOL, various assemblers) makes that
# learning a new language along transiting to structured environment makes
# teaching an old dog a new trick more difficult.  So, if you don’t like my
# structure, don’t bitch, download it and fix it!
# 
# Finally, it probably goes without saying,
# but this is public domain.   Feel free to download,
# give it away, modify and improve it, etc.
# Clark Gregory

# This is a script to help programmimg the Digitrax DS74.
# OPEN SYSTEM LOG
# DIRECTIONS ARE DISPLAYED THERE
# EXECUTE SCRIPT OFFLINE FROM LAYOUT
# IT WILL TELL YOU TO PRESS A BUTTON ON THE DS74
# PRESS THE BUTTON 3 SECONDS UNTIL THE LEDS FOR OTHER BUTTONS FLAST ALTERNATELY
# PRESS SPACE BAR ON COMPUTER
# IT WILL SET UP THAT PART THEN TELL YOU TO PRESS THE BUTTON FOR NEXT STEP

import jmri
import java
#import javax.swing.JButton
import javax.swing
#import apps
#import jarray
#import time
#import sys

## CA also import for logging:
import org.slf4j.Logger
import org.slf4j.LoggerFactory
###### CA

log = org.slf4j.LoggerFactory.getLogger(
            "jmri.jmrit.jython.exec.script.PGM_DS74"
        )


#********************************************************
#***  INPUT FILE NAME                                 ***
#***  EVENTUALLY, THIS SHOULD BE READ FROM KEYBOARD   ***
#***  AND SAVED HERE.  NOT SURE HOW SOFOR NOW:        ***
#***  JUST UPDATE SCRIPT HERE WITH THE FILENAME       ***
#***     FOR EACH DS74 RUN                            ***
#********************************************************
global filename
# filename  = "C:\CLARK\DS74217PROG.txt" ## CA
## CA
##  fully qualified hard coded file name is replaced with a partially soft coded filename
##  but only to the extent that the unqualified name, still hard coded, 
##  without knowing the user's Profile location.  
##
##      BUT, and a very serious BUT, the user's DS74 data file MUST reside in his/her currently 
##      active JMRI profile folder at execution time. 
##          As of this time, that file must have the unqualified filename "DS74217PROG.txt" too.

filename  = jmri.util.FileUtil.getExternalFilename("profile:" + "DS74217PROG.txt") ## CA
log.debug ('filename = "' + filename + '"' ) ## CA

#********************************************************

print "PROGRAM DS74 "
print ("PLEASE READ THE DIRECTIONS THAT CAME WITH YOUR DS74 TO")
print ("   UNDERSTAND WHAT THIS SCRIPT IS DOING")
print ("THIS WILL FIRST READ YOUR INPUT FILE (ENTER FILE ABOVE) ")
print ("    AND DO MINIMAL EDITS, AND SAVING YOUR SETTINGS.")
print ("YOU MAY GET ERRORS WHICH SHOULD BE CORRECTED BEFORE ")
print ("    CONTINUING.  IF OK, THEN CONTINUE, IT WILL TELL YOU")
print ("    TO PRESS THE BUTTONS ON THE DS74 SO IT CAN MAKE ")
print ("    PROPER UPDATES>")
print (" ") 


# These variables will contain the parameters to pass to program section below
global pstep       # USED TO KEEP UP WITH EACH PASS WHILE UPDATING ds74 
global routno      # USED TO KEEP UP WITH EACH ROUTE UPDATING DS745
global prmID       # SAVE THE DS74 ID NOTE IT ACTUALLY THE ADDRESS OF THE FIRST TURNOUT
global prmOPS      # SANE THE CV'S TO BE SET
global prmRTS      # LIST OF ROUTES TO BE SET UP
prmRTS = []
prmOPS = ""

pstep = "0"
routno = 0
erfnd = "N"
errors = 0        # count of errors parsing input file.  If not 0, don't update DS74 

# these are temp holding variables to be reused only within a few lines of code
x1=0              # GENERIC WORK FIELD

#****************************************************************
#***   GET INPUT PARAMETERS:                                  ***
#***  READ INPUT FILE, CHECK FOR ERRORS< MISSING INFO, ETC.   ***
#***  SAVE PARAMETERS IN GLOBAL VARIABLES FOR UPDATING DS74   ***
#****************************************************************

print ("READING INPUT: " + filename ) 

#====================================================
# READ INPUT FILE FOR DS74 SETTINGS
ParmRec = ""    # one record from text file with parameters to configure DS74
Parms = []      # list of all parameters from one record 
Parm1 = ""      # one parameter from one input record being processed

dsfile = open (filename, "r" )

while True:
    #READ ONE RECORD FROM FILE
    ParmRec = str.upper(dsfile.readline())
    # log.debug ( str(type(ParmRec)) )
    
    if not ParmRec:              # END OF FILE    
        break
        
    log.debug( 'Record from File = "' + ParmRec + '"' )
    x1 = len(ParmRec)
    log.debug( 'Record length = {0}'.format( x1 ) )
    #   Remove trailing spaces or newline from last position of ParmRec
    ##     **********  NOTE: BUG: DOES NOT FIND SPACES ****************
    ## while True:
        ## if (ParmRec[x1-1:x1] == "\n" or ParmRec[x1-1:x1] == " " ) :
            ## ParmRec = ParmRec[0:x1-1]
        ## else:
            ## break
    ParmRec = ParmRec.rstrip()    ## CA
        ## Equivalent to the Java expression:
        ##      ParmRec = ParmRec.trim()    ## CA
    print ("RECORD: " + ParmRec )
    
    # IF POS 1 IS "#", IGNORE AS COMMENT ELSE SEPARATE INTO PARMS
    if ParmRec[0] <> "#":
    
        Parms = ParmRec.split(" ")   # break record into table (list)
        
        # *********************************************************************************
        #    PROCESS  ID  RECORD 
        #    This is loaded as the id of the DS74
        #    or actually the address of the first of four turnouts
        if Parms[0] == "ID":      # process ID record
            log.debug( "processing an ID record" )
            if len(Parms) <> 2:
                print ("  >>>>> ID needs 1 parm")
                errors = errors + 1
            prmID = Parms[1]
            #print (  "REC:" + prmID )
            if (prmID.isdigit):
                if (int(prmID) <= 2000):
                    print ( "  ID:" + prmID)
                else:
                    print ("  >>>>> BAD ID value")
                    errors = errors + 1
            else:
                print ("  >>>>> ID must be numeric" + prmID)
                errors = errors + 1
            
        # *********************************************************************************
        #    PROCESS  OPS  RECORD 
        #    These records contains the CV settings for DS74.
        #    The CVs default to thrown, but user can select that are closed to configure DS74
        #    those are specified here one per record 
        if Parms[0] == "OPS":       # process OPS record
            log.debug( "processing an OPS record" )
            if len(Parms) <> 3:     #  it must have "OPS", "CV",  and 1 (turnout number)
                print ("  >>>>> OPS needs 2 Parms")
                errors = errors + 1 
            if Parms[1] <> "CV":    # the 2ed parm must be "CV"
                print ("  >>>>> OPS invalid, not CV")
                errors = errors + 1
            else:
                if not (Parms[2].isdigit):    # the 3ed must be the CV number >= 99:
                    print ("  >>>>> OPS invalid, CV value")
                    errors = errors + 1
                else: 
                    if prmOPS == "":
                        prmOPS = str(int(Parms[2]) )
                    else:        
                        prmOPS = prmOPS + " " + str(int(Parms[2]))  # All OK, save it
                    #   prmOPS is a string w/ all CVS to close separated by 1 space
                    #print ("  prmOPS:" + prmOPS)
                
                
        # *********************************************************************************
        #    PROCESS  RTS  RECORD
        #    THE DS74 CAN HAVE UPTO 8 ROUTES OF UPTO 8 TURNOUT ADDRESS PER ROUTE
        #    THEY ARE SPRCIFIED HERE AS ONE COMPLETE ROUTE (ITS ADDRESS AND UPTO 8 TURNOUTS) PER RECORD 
        if Parms[0] == "RTS":      # process RTS (routes)
            log.debug( "processing a RTS record" )
            PadrTC = ""            # address of turnout with "T" or "C" from a parameter
            Lp = 0                 # length of one parameter
            PrmCnt = 0                 # count of params on record
            erfnd = "N"               # if any errors found, this will become "Y"
            if (len(Parms) <= 3 or len(Parms) >= 12):
                print ("  >>>>>> RTS must have 1-8 turnouts: " + str(len(Parms) -3 ) )
                errors = errors + 1
                erfnd = "Y"
            
            PrmCnt = len(Parms)    # get number of parameters on input rec
            log.debug( "PrmCnt = {0}".format(PrmCnt) )

            #  EACH PARAMETER LOOKS LIKE 123T OR 123C  
            #       WHERE 123 IS THE SWITCH ADDRESS AND T/C INDICATES CLOSED/THROWN
            RoutSwtchs = Parms[1] + " "
            for ip in range(2,PrmCnt,1):   
                Parm1 = Parms[ip]    #get 1 full parm (adr + T)
                Lp = len(Parm1)      #get length onep inc T or C

                # parks must be numeric and < 2000
                if not ( Parm1[0:Lp-1].isdigit ):
                    # and int(Parm1[0:Lp-1]) <= int(2000)):
                    #####  BUG:   ***** THIS TEST DOESN'T WORK  
                    # The other is looking for numbers followed by “C” or “T” (123T) but 12CT is not caught.
                    print ("  >>>>> Route address need to be numbers: " + Parm1[0:Lp-1] )
                    errors = errors + 1
                    erfnd = "Y"

                tc = Parm1[Lp-1:Lp]         #  [Lp-2:-Lp+1]       #get last chr (TorC)
                if not ( tc == "C" or tc == "T" ) :  
                    print ("  >>>>> Route address need to have T or C: " + tc )
                    errors = errors + 1
                    erfnd = "Y"

                PadrTC = Parm1[0:Lp-1] + ":" + tc    #put oneP bact together w/ $
                #print ("  ==finl:" +  PadrTC ) 

                RoutSwtchs = RoutSwtchs + PadrTC + " "  #now put it all together
            print ("  Route:" + RoutSwtchs  ) 
            prmRTS.append(RoutSwtchs)          

    #print ("1 RECORD DONE")

    
print ("INPUT FILE END SUMMARY OF PARAMETERS")
if erfnd == "Y" :
    print ( ">>>>>> ERRORS  STOP # ERROR EXIT HERE ")
    #return


print ( "prmID:" + prmID )
print ( "prmOPS:" + prmOPS )
for x1 in range(0, len(prmRTS),1 ):
    print ("prmRTS-" + str(x1 + 1) + ":" + prmRTS[x1]  )  
print (" ")
print (" (NOTE: IF YOU HAVEN'T, SCROLL UP AND READ THE TOP) ")
print ("VERIFY ABOVE,  THEN PRESS SPACE BAR TO UPDATE DS74")
print ("    AND FOLLOW DIRECTIONS TO PRESS BUTTONS ON DS74")
print ("    (FOR 3 SECONDS) AND THEN")
print ("    PRESSING SPACE BAR FOR the NEXT STEP")
print ("REMEMBER, BUTTON <OPS> NEEDS TO BE CLEARED")
print ("    BUTTONS <ID> AND <RTS> CLEAR AUTOMATICALLY. ")
print (" ")     

# CREATE THE BUTTON
# b = javax.swing.JLabel("Program DS74" + filename)
b = javax.swing.JLabel("Program DS74 " + filename)

#********************************************************
# NOW PROCESS THE DATA FROM THE INPUT FILE
#  THIS WILL OPEN A SMALL WINDOW TO PROCESS THE DATA
#  WITH THE DS74, THE USER MUST PRESS ONE OF THE BUTTONS.  
#    1) PRESS OPS, ANE WE CLEAR IT TO FACTORY,
#    2) PRESS THE <ID> BUTTON, SO WE CAN SET THE ID
#    3) PRESS THE <OPS> BUTTON AGAIN SO WE CAN SET THE CVS
#    4) PRESS THE <RTS> BUTTON TO SET EACH ROUTE
#               THIS MUST BE DONE FOR EACH ROUTE
#
#  YOU MUST OPEN THE SYSTEM CONSOLE(UNDER HELP) TO CONTINUE
#   IT WILL STOP AND ASK YOU TO PRESS A BUTTON, 
#       PLEASE PRESS THAT BUTTON AND THEN THE SACE BAR
#  CONTINUE THAT UNTIL IT COMPLETES


class ListenToKey(java.awt.event.KeyAdapter):
    def keyPressed(self, event):   #event is keyevent
        #sw1 = 0
        global pstep
        global routno
        #print pstep
        xlst = []
        xx1 = ""   # GENERIC WORK FIELD 
        #*****************************************************************
        #*  THE CODE BE LOW WILL BE REPEATED FOR EACH INPUT CARD        **
        #*  THE SMALL WINDOW IS OPEN. BUT JUST SELECTING THE SPACE BAR  **
        #*  WILL START THE NEXT PASS THROUGH THIS "LOOP"                **
        #*****************************************************************
        
        #print (" pstep:" + pstep)
        # SINCE JYTHON RUNS EACH PASS IN A SEPARATE THREAD WE USE GLOBALS
        #    TO SAVE EVERYTHING
        #   PSTEP KEEPS TRACK OF WHERE 
        #   ROUTNO KEPS TRACK OF WHICH ROUTE WE ARE ON
        #
        #*********************************************************
        # POST MESSAGE TO TELL USER TO PRESS SPACE FOR FIRST STEP
        if (pstep == "0") :        
            print ("PRESS <OPS> TO RESET ALL TO FACTORY SETTING" )
            pstep = "R"

        #******************************************************************            
        # *****************  reset DS74 set 40 = C ************************
        elif (pstep == "R") :
            #b = javax.swing.JLabel("Press OPS to reset")
            pstep = "I"
            turnouts.provideTurnout("40").setState(CLOSED)
            print ("   DS74 is RESET CV40=C")
            print ("   CLEAR <OPS>")
            
        #******************************************************************            
        # *****************  PRES ID, SEND DS74 ID / SWITCH ADDR **********
            #print ("   Clear <OPS>   THEN") 
            print ("PRESS <ID> TO SET ID=" + prmID) 
        elif (pstep == "I") :
            #b = javax.swing.JLabel("Press RTS to reset")
            pstep = "C"
            sw1 = prmID
            turnouts.provideTurnout(prmID).setState(CLOSED)
            print ("   ID SET TO " + prmID + " <ID> SHOULD BE CLEAR")
            
        #******************************************************************            
        # *****************  PRES OPS, SEND EACH CV SETTING TO THROWN  ****
            print ("PRESS <OPS> TO SET CV SWITCHES: " + prmOPS ) 
        elif (pstep  == "C") :  # send a close command for each CV requested
            pstep = "X"
            routo = 0
            cvls =[]      #  list of CVs to close
            xi = 0        # index for cvls
            xc = 0        # count of cvls 
            #print ("prmOPS" + prmOPS + "!" )
            cvls = prmOPS.split(" ")       # make cvls a list of cvs requested
            xc = len(cvls)                 # make xc a count of CV in cvls
            
            for xi in range(0,xc,1 )  :    # for each CV in cvls issue a close command 
                #print ("cv:" + cvls[xi] + ":xi" + str(xi) + "xc"+ str(xc) )
                turnouts.provideTurnout(cvls[xi] ).setState(CLOSED)
            print ("   CVs ARE SET, Clear <OPS>")
            
        #******************************************************************            
        # *****************  PRES RTS  SEND EACH CV SETTING TO THROWN  ****
            #=========  routes below here =======================
            #print ("   CLEAR <OPS> "
            routno = 0 
            print ("PRESS <RTS> TO SET EACH ROUTE SLOT: [" + prmRTS[routno] + "]"   ) 
            
        #******************************************************
        #  WE BASICALLY "LOOP" THROUGH HERE FOR EACH ROUTE 
        elif (pstep  == "X") :
            
            ir = 0    # index of routes (1-8) in prmRTS (list)
            cr = 0    # number of routes specified
            it = 0    # index turnouts in route
            xx1 = ""   # generic work field
            
            im = len(prmRTS)
            rteitem = ""    # one item in rtelist
            rtelist = []   # list of items in this route in prmRTS
            
            # look at EACH ROUTE and get each turnout in each route
            ir = routno
            routno = routno +1
            if ir <= len(prmRTS) -1:
                #for ir in range(0, len(prmRTS),1 ): 
                #print ("  ROUTE-" + str(ir) + ":" + prmRTS[ir] + "L:" + str(len(prmRTS) ) )
                
                # this will a list of turnouts in this route,  note 1st is route no (1-8)
                rtelist = prmRTS[ir].split(' ')   
                turnouts.provideTurnout(rtelist[0]).setState(CLOSED)  # set turnout (route) #
                
                #  now set each turnouts in this route 
                for ic in range(1, len(rtelist) -1 ,1 ):  
                    #print (" TO:" + rtelist[ic] + ":" + str(ic) )
                    xx1 = rtelist[ic] [0: len(rtelist[ic])-2]
                    xxc = rtelist[ic] [len(rtelist[ic]) -1 : len(rtelist[ic]) ] 
                    #print ( "xx1:" + xx1 + ":-tc:" + xxc )
                    if (xxc == "C"):
                        print ("   closed:" + xx1 )
                        turnouts.provideTurnout(xx1).setState(CLOSED)
                    else:
                        print ("   throw :"  + xx1  )   #+ "XXC:" + xxc + ":")
                        turnouts.provideTurnout(xx1).setState(THROWN)

            # now send the last turnout again to indicate end of route     
            if (xxc == "C"):
                print ("   Lclosed:" + xx1 )
                turnouts.provideTurnout(xx1).setState(CLOSED)
            else:
                print ("   Lthrow :"  + xx1  )   #+ "XXC:" + xxc + ":")
                turnouts.provideTurnout(xx1).setState(THROWN)


            #  now is this the last route specified, if so message and droup out    
            if int(routno) <= int(len(prmRTS)-1 ) :
                print ("PRESS <RTS> TO SET NEXT ROUTE SLOT:  [" + prmRTS[routno] + "]"    ) 
            else:
                print ("DONE programming:  RTS should be clear: "  )
                #print ("PRESS <RTS> TO SET EACH ROUTE POS :" + prmRTS[routno] ) 
                pstep = "Z"
                print (" END OF JOB - TERMINATE =================================" ) 
                
#DS74prog.frame.dispose()
                

# Create a frame
f = javax.swing.JFrame("DS74prog");
f.setLocation(10,10) ;
f.setSize(1800,1800);
f.addKeyListener( ListenToKey() )
b.addKeyListener( ListenToKey() )
f.contentPane.addKeyListener( ListenToKey())

f.contentPane.add(b)
f.setSize(80,80)
f.pack()
f.show()
###t.stop()
