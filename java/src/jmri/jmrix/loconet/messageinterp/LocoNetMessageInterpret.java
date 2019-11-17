/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.loconet.messageinterp;


import java.time.LocalTime;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents;
import jmri.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for formatting LocoNet packets into human-readable text.
 * <p>
 * Note that the formatted strings end in a \n, and may contain more than one
 * line separated by \n. Someday this should be converted to proper Java line
 * handling.
 * <p>
 * Much of this file is a Java-recoding of the display.c file from the llnmon
 * package of John Jabour. Some of the conversions involve explicit decoding of
 * structs defined in loconet.h in that same package. Those parts are (C)
 * Copyright 2001 Ron W. Auld. Use of these parts is by direct permission of the
 * author.
 * <p>
 * This class is derived from and replaces JMRI's
 * jmri.jmrix.loconet.locomon.Llnmon.java .
 * <p>
 * Many major comment blocks here are quotes from the Digitrax LocoNet(r) OPCODE
 * SUMMARY: found in the LocoNet(r) Personal Edition 1.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * Reverse engineering of OPC_MULTI_SENSE was provided by Al Silverstein, used
 * with permission.
 * <p>
 * Reverse engineering of the Duplex Group/Password/Channel management was
 * provided by Leo Bicknell with help from B. Milhaupt, used with permission.
 * <p>
 * Reverse-engineering of device-specific OpSw messages, throttle text message,
 * and throttle semaphore message was provided by B. Milhaupt, used with
 * permission.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2003
 * @author B. Milhaupt Copyright 2015, 2016, 2018
 * @author Randall Wood Copyright 2016
 */
public class LocoNetMessageInterpret {

    /**
     * Format the message into a text string.
     * <p>
     * Where the code is unable to determine a correct interpretation, the returned
     * string contains a message indicating that the message is not decoded followed
     * by the individual bytes of the message (in hexadecimal).
     *
     * @param l Message to parse
     * @param turnoutPrefix "System Name+ prefix which designates the connection's
     *          Turnouts, such as "LT"
     * @param sensorPrefix "System Name+ prefix which designates the connection's
     *          Turnouts, such as "LS"
     * @param reporterPrefix "System Name+ prefix which designates the connection's
     *          Turnouts, such as "LR"
     * @return String representation of the interpretation of the message
     */
    public static String interpretMessage(LocoNetMessage l, String turnoutPrefix, String sensorPrefix, String reporterPrefix) {

        String result;

        result = "";
        /*
         * 2 Byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<CKSUM>
         * ;
         *
         * 4 byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<CKSUM>
         * :
         *  CODES 0xA8 to 0xAF have responses
         *  CODES 0xB8 to 0xBF have responses
         *
         * 6 byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<ARG3>,<ARG4>,<CKSUM>
         * :
         *  CODES 0xC8 to 0xCF have responses
         *  CODES 0xD8 to 0xDF have responses
         */
        switch (l.getOpCode()) {

            /*
             * OPC_IDLE 0x85 ;FORCE IDLE state, Broadcast emergency STOP
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_IDLE: {
                return Bundle.getMessage("LN_MSG_IDLE");
            }

            /*
             * OPC_GPON 0x83 ;GLOBAL power ON request
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_GPON: {
                return Bundle.getMessage("LN_MSG_GPON");

            }

            /*
             * OPC_GPOFF 0x82 ;GLOBAL power OFF request
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_GPOFF: {
                return Bundle.getMessage("LN_MSG_GPOFF");
            }

            /*
             * OPC_GPBUSY 0x81 ;MASTER busy code, NULL
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_GPBUSY: {
                return Bundle.getMessage("LN_MSG_MASTER_BUSY");
            }

            case LnConstants.OPC_RE_LOCORESET_BUTTON: {
                return Bundle.getMessage("LN_MSG_RE_LOCO_RESET");

            }

            /*
             * OPC_LOCO_ADR     0xBF   ; REQ loco ADR
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR
             *                         ; DATA return <E7>, is SLOT#, DATA that ADR was
             *                         : found in.
             *                         ; IF ADR not found, MASTER puts ADR in FREE slot
             *                         ; and sends DATA/STATUS return <E7>......
             *                         ; IF no FREE slot, Fail LACK,0 is returned
             *                         ; [<B4>,<3F>,<0>,<CHK>]
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LOCO_ADR: {
                String locoAddress = convertToMixed(l.getElement(2), l.getElement(1));
                return Bundle.getMessage("LN_MSG_REQ_SLOT_FOR_ADDR",
                        locoAddress);
            }

            case LnConstants.OPC_EXP_REQ_SLOT: {
                String locoAddress = convertToMixed(l.getElement(2), l.getElement(1));
                return Bundle.getMessage("LN_MSG_REQ_EXP_SLOT_FOR_ADDR",
                        locoAddress);
            }

            /*
             * OPC_SW_ACK       0xBD   ; REQ SWITCH WITH acknowledge function (not DT200)
             *                         ; Follow on message: LACK
             *                         ; <0xBD>,<SW1>,<SW2>,<CHK> REQ SWITCH function
             *                         ;       <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>
             *                         ;               7 ls adr bits.
             *                         ;               A1,A0 select 1 of 4 input pairs
             *                         ;               in a DS54
             *                         ;       <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>
             *                         ;               Control bits and 4 MS adr bits.
             *                         ;               DIR=1 for Closed/GREEN
             *                         ;                  =0 for Thrown/RED
             *                         ;               ON=1 for Output ON
             *                         ;                 =0 FOR output OFF
             *                         ; response is:
             *                         ; <0xB4><3D><00> if DCS100 FIFO is full, rejected.
             *                         ; <0xB4><3D><7F> if DCS100 accepted
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_SW_ACK: {
                result = interpretOpcSwAck(l, turnoutPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_SW_STATE     0xBC   ; REQ state of SWITCH
             *                         ; Follow on message: LACK
             *                         ; <0xBC>,<SW1>,<SW2>,<CHK> REQ state of SWITCH
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_SW_STATE: {
                result = interpretOpcSwState(l, turnoutPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }


            /*
             * OPC_RQ_SL_DATA   0xBB   ; Request SLOT DATA/status block
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBB>,<SLOT>,<0>,<CHK> Request SLOT DATA/status block.
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_RQ_SL_DATA: {
                result = interpretOpcRqSlData(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_MOVE_SLOTS   0xBA   ; MOVE slot SRC to DEST
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBA>,<SRC>,<DEST>,<CHK> Move SRC to DEST if
             *                         ; SRC or LACK etc is NOT IN_USE, clr SRC
             *                         ; SPECIAL CASES:
             *                         ; If SRC=0 ( DISPATCH GET) , DEST=dont care,
             *                         ;    Return SLOT READ DATA of DISPATCH Slot
             *                         ; IF SRC=DEST (NULL move) then SRC=DEST is set to
             *                         ;    IN_USE , if legal move.
             *                         ; If DEST=0, is DISPATCH Put, mark SLOT as DISPATCH
             *                         ;    RETURN slot status <0xE7> of DESTINATION slot
             *                         ;       DEST if move legal
             *                         ;    RETURN Fail LACK code if illegal move
             *                         ;       <B4>,<3A>,<0>,<chk>, illegal to move to/from
             *                         ;       slots 120/127
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_MOVE_SLOTS: {
                result = interpretOpcMoveSlots(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

//            case LnConstants.OPC_EXP_SLOT_MOVE: {
//                result = interpretOpcExpMoveSlots(l);
//                if (result.length() > 0) {
//                    return result;
//                }
//                break;
//            }

            /*
             * OPC_LINK_SLOTS   0xB9   ; LINK slot ARG1 to slot ARG2=
             *                         ; Follow on message: <E7>SLOT READ=
             *                         ; <0xB9>,<SL1>,<SL2>,<CHK> SLAVE slot SL1 to slot SL2
             *                         ; Master LINKER sets the SL_CONUP/DN flags
             *                         ; appropriately. Reply is return of SLOT Status
             *                         ; <0xE7>. Inspect to see result of Link, invalid
             *                         ; Link will return Long Ack Fail <B4>,<39>,<0>,<CHK>
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LINK_SLOTS: {
                int src = l.getElement(1);
                int dest = l.getElement(2);
                return Bundle.getMessage("LN_MSG_LINK_SLOTS", src, dest);
            }

            /*
             * OPC_UNLINK_SLOTS 0xB8   ;UNLINK slot ARG1 from slot ARG2
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xB8>,<SL1>,<SL2>,<CHK> UNLINK slot SL1 from SL2
             *                         ; UNLINKER executes unlink STRATEGY and returns new SLOT#
             *                         ; DATA/STATUS of unlinked LOCO . Inspect data to evaluate UNLINK
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_UNLINK_SLOTS: {
                int src = l.getElement(1);
                int dest = l.getElement(2);
                return Bundle.getMessage("LN_MSG_UNLINK_SLOTS", src, dest);
            } // case LnConstants.OPC_UNLINK_SLOTS

            /*
             * OPC_CONSIST_FUNC 0xB6   ; SET FUNC bits in a CONSIST uplink element
             *                         ; <0xB6>,<SLOT>,<DIRF>,<CHK> UP consist FUNC bits
             *                         ; NOTE this SLOT adr is considered in UPLINKED slot space.
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_CONSIST_FUNC: {
                result = interpretOpcConsistFunc(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_SLOT_STAT1   0xB5   ; WRITE slot stat1
             *                         ; <0xB5>,<SLOT>,<STAT1>,<CHK> WRITE stat1
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_SLOT_STAT1: {
                int slot = l.getElement(1);
                int stat = l.getElement(2);
                return Bundle.getMessage("LN_MSG_SLOT_STAT1", slot, stat,
                        Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                StringUtil.twoHexFromInt(stat)), LnConstants.CONSIST_STAT(stat),
                        LnConstants.LOCO_STAT(stat), LnConstants.DEC_MODE(stat));
            }

            /*
             * OPC_LONG_ACK     0xB4   ; Long acknowledge
             *                         ; <0xB4>,<LOPC>,<ACK1>,<CHK> Long acknowledge
             *                         ; <LOPC> is COPY of OPCODE responding to (msb=0).
             *                         ; LOPC=0 (unused OPC) is also VALID fail code
             *                         ; <ACK1> is appropriate response code for the OPCode
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LONG_ACK: {
                result = interpretLongAck(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_INPUT_REP    0xB2   ; General SENSOR Input codes
             *                         ; <0xB2>, <IN1>, <IN2>, <CHK>
             *                         ;   <IN1> =<0,A6,A5,A4- A3,A2,A1,A0>,
             *                         ;           7 ls adr bits.
             *                         ;           A1,A0 select 1 of 4 inputs pairs in a DS54.
             *                         ;   <IN2> =<0,X,I,L- A10,A9,A8,A7>,
             *                         ;           Report/status bits and 4 MS adr bits.
             *                         ;           "I"=0 for DS54 "aux" inputs
             *                         ;              =1 for "switch" inputs mapped to 4K SENSOR space.
             *                         ;
             *                         ;           (This is effectively a least significant adr bit when
             *                         ;            using DS54 input configuration)
             *                         ;
             *                         ;           "L"=0 for input SENSOR now 0V (LO),
             *                         ;              =1 for Input sensor >=+6V (HI)
             *                         ;           "X"=1, control bit,
             *                         ;              =0 is RESERVED for future!
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_INPUT_REP: {
                result = interpretOpcInputRep(l, sensorPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            } // case LnConstants.OPC_INPUT_REP

            /*
             * OPC_SW_REP       0xB1   ; Turnout SENSOR state REPORT
             *                         ; <0xB1>,<SN1>,<SN2>,<CHK> SENSOR state REPORT
             *                         ;   <SN1> =<0,A6,A5,A4- A3,A2,A1,A0>,
             *                         ;           7 ls adr bits.
             *                         ;           A1,A0 select 1 of 4 input pairs in a DS54
             *                         ;   <SN2> =<0,1,I,L- A10,A9,A8,A7>
             *                         ;           Report/status bits and 4 MS adr bits.
             *                         ;           this <B1> opcode encodes input levels
             *                         ;           for turnout feedback
             *                         ;           "I" =0 for "aux" inputs (normally not feedback),
             *                         ;               =1 for "switch" input used for
             *                         ;                  turnout feedback for DS54
             *                         ;                  ouput/turnout # encoded by A0-A10
             *                         ;           "L" =0 for this input 0V (LO),
             *                         ;               =1 this input > +6V (HI)
             *                         ;
             *                         ;   alternately;
             *                         ;
             *                         ;   <SN2> =<0,0,C,T- A10,A9,A8,A7>
             *                         ;           Report/status bits and 4 MS adr bits.
             *                         ;           this <B1> opcode encodes current OUTPUT levels
             *                         ;           "C" =0 if "Closed" ouput line is OFF,
             *                         ;               =1 "closed" output line is ON
             *                         ;                  (sink current)
             *                         ;           "T" =0 if "Thrown" output line is OFF,
             *                         ;               =1 "thrown" output line is ON
             *                         ;                  (sink I)
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_SW_REP: {
                result = interpretOpcSwRep(l, turnoutPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_SW_REQ       0xB0   ; REQ SWITCH function
             *                         ; <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function
             *                         ;   <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>,
             *                         ;           7 ls adr bits.
             *                         ;           A1,A0 select 1 of 4 input pairs in a DS54
             *                         ;   <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>
             *                         ;           Control bits and 4 MS adr bits.
             *                         ;   DIR  =1 for Closed,/GREEN,
             *                         ;        =0 for Thrown/RED
             *                         ;   ON   =1 for Output ON,
             *                         ;        =0 FOR output OFF
             *                         ;
             *                         ;   Note-Immediate response of <0xB4><30><00> if command failed,
             *                         ;        otherwise no response "A" CLASS codes
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             * Page 12 special form Broadcast.
             * Page 13 special form LocoNet interrogate.
             */
            case LnConstants.OPC_SW_REQ: {
                result = interpretOpcSwReq(l, turnoutPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_LOCO_SND     0xA2   ;SET SLOT sound functions
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LOCO_SND: {
                result = interpretOpcLocoSnd(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            } // case LnConstants.OPC_LOCO_SND

            /*
             * OPC_LOCO_DIRF 0xA1 ;SET SLOT dir, F0-4 state
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LOCO_DIRF: {
                result = interpretOpcLocoDirf(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_LOCO_SPD 0xA0 ;SET SLOT speed e.g. <0xA0><SLOT#><SPD><CHK>
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
            case LnConstants.OPC_LOCO_SPD: {
                result = interpretOpcLocoSpd(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR: {
                result = interpretPocExpLocoSpdDirFunction(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_PANEL_QUERY 0xDF messages used by throttles to discover
             * panels
             *
             * This op code is not documented by Digitrax. Some reverse engineering
             * performed by Leo Bicknell.  The opcode "name" OPC_PANEL_QUERY
             * is not necessarily the name used by Digitrax.
             */
            case LnConstants.OPC_PANEL_QUERY: {
                result = interpretOpcPanelQuery(l);
                if (result.length() > 0) {
                    return result;
                }
                break;

            }

            /*
             * OPC_PANEL_RESPONSE 0xD7 messages used by throttles to discover
             * panels
             *
             * This op code is not documented by Digitrax. Reverse engineering
             * performed by Leo Bicknell.  The opcode "name" OPC_PANEL_RESPONSE
             * is not necessarily the name used by Digitrax.
             */
            case LnConstants.OPC_PANEL_RESPONSE: {
                result = interpretOpcPanelResponse(l);
                if (result.length() > 0) {
                    return result;
                }
                break;

            }

            /*
             * OPC_MULTI_SENSE 0xD0 messages about power management and
             * transponding
             *
             * If byte 1 high nibble is 0x20 or 0x00 this is a transponding
             * message
             *
             * This op code is not documented by Digitrax. Reverse engineering
             * performed by Al Silverstein, and corrections added by B. Milhaupt.
             */
            case LnConstants.OPC_MULTI_SENSE: {
                result = interpretOpcMultiSense(l, reporterPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /**
             * ********************************************************************************************
             * OPC_WR_SL_DATA 0xEF ; WRITE SLOT DATA, 10 bytes * ; Follow on
             * message: LACK * ; <0xEF>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,
             * * ;        <TRK>,<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK> * ; SLOT DATA
             * WRITE, 10 bytes data /14 byte MSG *
             * **********************************************************************************************
             * OPC_SL_RD_DATA 0xE7 ; SLOT DATA return, 10 bytes * ;
             * <0xE7>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>, * ;
             * <TRK>,<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK> * ; SLOT DATA READ, 10
             * bytes data /14 byte MSG * ; * ; NOTE; If STAT2.2=0 EX1/EX2
             * encodes an ID#, * ; [if STAT2.2=1 the STAT.3=0 means EX1/EX2 * ;
             * are ALIAS] * ; * ; ID1/ID2 are two 7 bit values encoding a 14 bit
             * * ; unique DEVICE usage ID. * ; * ; 00/00 - means NO ID being
             * used * ; * ; 01/00 - ID shows PC usage. * ; to Lo nibble is TYP
             * PC# * ; 7F/01 (PC can use hi values) * ; * ; 00/02 -SYSTEM
             * reserved * ; to * ; 7F/03 * ; * ; 00/04 -NORMAL throttle RANGE *
             * ; to * ; 7F/7E *
             * **********************************************************************************************
             * Notes: * The SLOT DATA bytes are, in order of TRANSMISSION for
             * <E7> READ or <EF> WRITE. * NOTE SLOT 0 <E7> read will return
             * MASTER config information bytes. * * 0) SLOT NUMBER: * * ; 0-7FH,
             * 0 is special SLOT, * ; 070H-07FH DIGITRAX reserved: * * 1) SLOT
             * STATUS1: * * D7-SL_SPURGE ; 1=SLOT purge en, * ; ALSO adrSEL
             * (INTERNAL use only) (not seen on NET!) * * D6-SL_CONUP ;
             * CONDN/CONUP: bit encoding-Control double linked Consist List * ;
             * 11=LOGICAL MID CONSIST , Linked up AND down * ; 10=LOGICAL
             * CONSIST TOP, Only linked downwards * ; 01=LOGICAL CONSIST
             * SUB-MEMBER, Only linked upwards * ; 00=FREE locomotive, no
             * CONSIST indirection/linking * ; ALLOWS "CONSISTS of CONSISTS".
             * Uplinked means that * ; Slot SPD number is now SLOT adr of
             * SPD/DIR and STATUS * ; of consist. i.e. is ;an Indirect pointer.
             * This Slot * ; has same BUSY/ACTIVE bits as TOP of Consist. TOP is
             * * ; loco with SPD/DIR for whole consist. (top of list). * ;
             * BUSY/ACTIVE: bit encoding for SLOT activity * * D5-SL_BUSY ;
             * 11=IN_USE loco adr in SLOT -REFRESHED * * D4-SL_ACTIVE ; 10=IDLE
             * loco adr in SLOT -NOT refreshed * ; 01=COMMON loco adr IN SLOT
             * -refreshed * ; 00=FREE SLOT, no valid DATA -not refreshed * *
             * D3-SL_CONDN ; shows other SLOT Consist linked INTO this slot, see
             * SL_CONUP * * D2-SL_SPDEX ; 3 BITS for Decoder TYPE encoding for
             * this SLOT * * D1-SL_SPD14 ; 011=send 128 speed mode packets * *
             * D0-SL_SPD28 ; 010=14 step MODE * ; 001=28 step. Generate Trinary
             * packets for this * ; Mobile ADR * ; 000=28 step. 3 BYTE PKT
             * regular mode * ; 111=128 Step decoder, Allow Advanced DCC
             * consisting * ; 100=28 Step decoder ,Allow Advanced DCC consisting
             * * * 2) SLOT LOCO ADR: * * LOCO adr Low 7 bits (byte sent as ARG2
             * in ADR req opcode <0xBF>) * * 3) SLOT SPEED: * 0x00=SPEED 0 ,STOP
             * inertially * 0x01=SPEED 0 EMERGENCY stop * 0x02->0x7F increasing
             * SPEED,0x7F=MAX speed * (byte also sent as ARG2 in SPD opcode
             * <0xA0> ) * * 4) SLOT DIRF byte: (byte also sent as ARG2 in DIRF
             * opcode <0xA1>) * * D7-0 ; always 0 * D6-SL_XCNT ; reserved , set
             * 0 * D5-SL_DIR ; 1=loco direction FORWARD * D4-SL_F0 ;
             * 1=Directional lighting ON * D3-SL_F4 ; 1=F4 ON * D2-SL_F3 ; 1=F3
             * ON * D1-SL_F2 ; 1=F2 ON * D0-SL_F1 ; 1=F1 ON * * * * * 5) TRK
             * byte: (GLOBAL system /track status) * * D7-D4 Reserved * D3
             * GTRK_PROG_BUSY 1=Programming TRACK in this Master is BUSY. * D2
             * GTRK_MLOK1 1=This Master IMPLEMENTS LocoNet 1.1 capability, *
             * 0=Master is DT200 * D1 GTRK_IDLE 0=TRACK is PAUSED, B'cast EMERG
             * STOP. * D0 GTRK_POWER 1=DCC packets are ON in MASTER, Global
             * POWER up * * 6) SLOT STATUS: * * D3 1=expansion IN ID1/2,
             * 0=ENCODED alias * D2 1=Expansion ID1/2 is NOT ID usage * D0
             * 1=this slot has SUPPRESSED ADV consist-7) * * 7) SLOT LOCO ADR
             * HIGH: * * Locomotive address high 7 bits. If this is 0 then Low
             * address is normal 7 bit NMRA SHORT * address. If this is not zero
             * then the most significant 6 bits of this address are used in *
             * the first LONG address byte ( matching CV17). The second DCC LONG
             * address byte matches CV18 * and includes the Adr Low 7 bit value
             * with the LS bit of ADR high in the MS postion of this * track adr
             * byte. * * Note a DT200 MASTER will always interpret this as 0. *
             * * 8) SLOT SOUND: * * Slot sound/ Accesory Function mode II
             * packets. F5-F8 * (byte also sent as ARG2 in SND opcode) * * D7-D4
             * reserved * D3-SL_SND4/F8 * D2-SL_SND3/F7 * D1-SL_SND2/F6 *
             * D0-SL_SND1/F5 1= SLOT Sound 1 function 1active (accessory 2) * *
             * 9) EXPANSION RESERVED ID1: * * 7 bit ls ID code written by
             * THROTTLE/PC when STAT2.4=1 * * 10) EXPANSION RESERVED ID2: * * 7
             * bit ms ID code written by THROTTLE/PC when STAT2.4=1 *
             * ********************************************************************************************
             * page 10 of LocoNet PE
             */
            case LnConstants.OPC_WR_SL_DATA:
            case LnConstants.OPC_SL_RD_DATA: {
                result = interpretOpcWrSlDataOpcSlRdData(l);
                if (result.length() > 0) {
                    return result;
                }
                break;

            }

            case LnConstants.OPC_ALM_WRITE:
            case LnConstants.OPC_ALM_READ: {
                result = interpretAlm(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            /*
             * OPC_PEER_XFER   0xE5    ; move 8 bytes PEER to PEER, SRC->DST   NO resp
             *                         ; <0xE5>,<10>,<SRC>,<DSTL><DSTH>,<PXCT1>,<D1>,<D2>,<D3>,<D4>,
             *                         ; <PXCT2>,<D5>,<D6>,<D7>,<D8>,<CHK>
             *                         ; SRC/DST are 7 bit args. DSTL/H=0 is BROADCAST msg
             *                         ;     SRC=0 is MASTER
             *                         ;     SRC=0x70-0x7E are reserved
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             *
             * Duplex group management reverse engineered by Leo Bicknell, with input from
             * B. Milhaupt.
             */
            case LnConstants.OPC_PEER_XFER: {
                result = interpretOpcPeerXfer(l, reporterPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.OPC_LISSY_UPDATE: {
                result = interpretOpcLissyUpdate(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.OPC_IMM_PACKET: {
                result = interpretOpcImmPacket(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.RE_OPC_PR3_MODE: {
                result = interpretOpcPr3Mode(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.RE_OPC_IB2_F9_F12: {
                result = interpretIb2F9_to_F12(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

//          TODO: put this back for intelibox cmd station.
//            it conflicts with loconet speed/dire etc.
            case LnConstants.RE_OPC_IB2_SPECIAL: { // 0xD4
                result = interpretIb2Special(l);
                if (result.length() > 0) {
                    return result;
                }
                result = interpretOpcExpMoveSlots(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }//  case LnConstants.RE_OPC_IB2_SPECIAL: { //0xD4

            //$FALL-THROUGH$
            default:
                break;
        } // end switch over opcode type
        return Bundle.getMessage("LN_MSG_UNKNOWN_MESSAGE") +
                Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());
    }


    private static String interpretOpcPeerXfer20_1(LocoNetMessage l) {
        switch (l.getElement(3)) {
            case 0x08: {
                return Bundle.getMessage("LN_MSG_DUPLEX_RECEIVER_QUERY");
            }
            case 0x10: {
                return Bundle.getMessage("LN_MSG_DUPLEX_RECEIVER_RESPONSE");
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_2(LocoNetMessage l) {
        switch (l.getElement(3)) {
            case 0x00: {
                int channel = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

                return Bundle.getMessage("LN_MSG_DUPLEX_CHANNEL_SET",
                        Integer.toString(channel));
            }
            case 0x08: {
                return Bundle.getMessage("LN_MSG_DUPLEX_CHANNEL_QUERY");
            }
            case 0x10: {
                int channel = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

                return Bundle.getMessage("LN_MSG_DUPLEX_CHANNEL_REPORT",
                        Integer.toString(channel));
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_3(LocoNetMessage l) {
        // Characters appear to be 8 bit values, but transmitted over a 7 bit
        // encoding, so high order bits are stashed in element 4 and 9.
        char[] groupNameArray = {(char) (l.getElement(5) | ((l.getElement(4) & 0x01) << 7)),
            (char) (l.getElement(6) | ((l.getElement(4) & 0x02) << 6)),
            (char) (l.getElement(7) | ((l.getElement(4) & 0x04) << 5)),
            (char) (l.getElement(8) | ((l.getElement(4) & 0x08) << 4)),
            (char) (l.getElement(10) | ((l.getElement(9) & 0x01) << 7)),
            (char) (l.getElement(11) | ((l.getElement(9) & 0x02) << 6)),
            (char) (l.getElement(12) | ((l.getElement(9) & 0x04) << 5)),
            (char) (l.getElement(13) | ((l.getElement(9) & 0x08) << 4))};
        String groupName = new String(groupNameArray);

        // The pass code is stuffed in here, each digit in 4 bits.  But again, it's a
        // 7 bit encoding, so the MSB of the "upper" half is stuffed into byte 14.
        int p1 = ((l.getElement(14) & 0x01) << 3) | ((l.getElement(15) & 0x70) >> 4);
        int p2 = l.getElement(15) & 0x0F;
        int p3 = ((l.getElement(14) & 0x02) << 2) | ((l.getElement(16) & 0x70) >> 4);
        int p4 = l.getElement(16) & 0x0F;

        // It's not clear you can set A-F from throttles or Digitrax's tools, but
        // they do take and get returned if you send them on the wire...
        String passcode = StringUtil.twoHexFromInt(p1) + StringUtil.twoHexFromInt(p2)
                + StringUtil.twoHexFromInt(p3) + StringUtil.twoHexFromInt(p4);

        // The MSB is stuffed elsewhere again...
        int channel = l.getElement(17) | ((l.getElement(14) & 0x04) << 5);

        // The MSB is stuffed elsewhere one last time.
        int id = l.getElement(18) | ((l.getElement(14) & 0x08) << 4);

        switch (l.getElement(3)) {
            case 0x00: {
                return Bundle.getMessage("LN_MSG_DUPLEX_NAME_WRITE",
                        groupName);
            }
            case 0x08: {
                return Bundle.getMessage("LN_MSG_DUPLEX_NAME_QUERY");
            }
            case 0x10: {
                return Bundle.getMessage("LN_MSG_DUPLEX_NAME_REPORT",
                        groupName, passcode, channel, id);
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_4(LocoNetMessage l) {
        // The MSB is stuffed elsewhere again...
        int id = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

        switch (l.getElement(3)) {
            case 0x00: {
                return Bundle.getMessage("LN_MSG_DUPLEX_ID_SET", id);
            }
            case 0x08: {
                return Bundle.getMessage("LN_MSG_DUPLEX_ID_QUERY");
            }
            case 0x10: {
                return Bundle.getMessage("LN_MSG_DUPLEX_ID_REPORT", id);
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_7(LocoNetMessage l) {
        if (l.getElement(3) == 0x08) {
            return Bundle.getMessage("LN_MSG_DUPLEX_PASSWORD_QUERY");
        }

        if ((l.getElement(5) < 0x30) || (l.getElement(5) > 0x3c)
                || (l.getElement(6) < 0x30) || (l.getElement(6) > 0x3c)
                || (l.getElement(7) < 0x30) || (l.getElement(7) > 0x3c)
                || (l.getElement(8) < 0x30) || (l.getElement(8) > 0x3c)) {
            return "";
        }
        char[] groupPasswordArray = {(char) l.getElement(5),
            (char) l.getElement(6),
            (char) l.getElement(7),
            (char) l.getElement(8)};
        if ((groupPasswordArray[0] > 0x39) && (groupPasswordArray[0] < 0x3d)) {
            groupPasswordArray[0] += ('A' - '9' - 1);
        }
        if ((groupPasswordArray[1] > 0x39) && (groupPasswordArray[1] < 0x3d)) {
            groupPasswordArray[1] += ('A' - '9' - 1);
        }
        if ((groupPasswordArray[2] > 0x39) && (groupPasswordArray[2] < 0x3d)) {
            groupPasswordArray[2] += ('A' - '9' - 1);
        }
        if ((groupPasswordArray[3] > 0x39) && (groupPasswordArray[3] < 0x3d)) {
            groupPasswordArray[3] += ('A' - '9' - 1);
        }
        String groupPassword = new String(groupPasswordArray);

        switch (l.getElement(3)) {
            case 0x00: {
                return Bundle.getMessage("LN_MSG_DUPLEX_PASSWORD_SET", groupPassword);
            }
            case 0x10: {
                return Bundle.getMessage("LN_MSG_DUPLEX_PASSWORD_REPORT", groupPassword);
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_10(LocoNetMessage l) {
        switch (l.getElement(3)) {
            case 0x08: {
                return Bundle.getMessage("LN_MSG_DUPLEX_CHANNEL_SCAN_QUERY", l.getElement(5));
            }
            case 0x10: {
                // High order bit stashed in another element again.
                int level = (l.getElement(6) & 0x7F) + ((l.getElement(4) & 0x02) << 6);

                return Bundle.getMessage("LN_MSG_DUPLEX_CHANNEL_SCAN_REPORT", l.getElement(5),
                        level);
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_8(LocoNetMessage l) {
        /**
         * **********************************************************************************
         * IPL-capable device ping - OPC_RE_IPL (Device Ping Operations) * The
         * message bytes as assigned as follows:
         * <p>
         * <E5> <14> <08> <GR_OP_T> <DI_F2> <DI_Ss0>
         * <DI_Ss1> ...
         * <p>
         * <DI_Ss2> <DI_Ss3> <DI_U1> <00> <00> <DI_U2>
         * <DI_U3> ...
         * <p>
         * <00> <00><00> <00><00> <CHK> * where:
         * <p>
         * <DI_F2> encodes additional bits for the Slave device serial number. *
         * bits 7-4 always 0000b * bit 3 Bit 31 of Slave Device Serial Number *
         * bit 2 Bit 23 of Slave Device Serial Number * bit 1 Bit 15 of Slave
         * device Serial Number * bit 0 Bit 7 of Slave device Serial Number
         * <p>
         * <DI_Ss0> encodes 7 bits of the 32 bit Host device serial number: *
         * bit 7 always 0 * bits 6-0 Bits 6:0 of Slave device serial number
         * <p>
         * <DI_Ss1> encodes 7 bits of the 32 bit Host device serial number: *
         * bit 7 always 0 * bits 6-0 Bits 14:8 of Slave device serial number
         * <p>
         * <DI_Ss2> encodes 7 bits of the 32 bit Host device serial number: *
         * bit 7 always 0 * bits 6-0 Bits 22:16 of Slave device serial number
         * <p>
         * <DI_Ss3> encodes 7 bits of the 32 bit Host device serial number: *
         * bit 7 always 0 * bits 6-0 Bits 30:24 of Slave device serial number
         * <p>
         * <DI_U1> unknown data * when <GR_OP_T> = 0x08 * is always 0 * when
         * <GR_OP_T> = 0x10 * is not reverse-engineered and may be non-zero.
         * <p>
         * <DI_U2> unknown data * when <GR_OP_T> = 0x08 * is always 0 * when
         * <GR_OP_T> = 0x10 * is not reverse-engineered and may be non-zero.
         * <p>
         * <DI_U3> unknown data * when <GR_OP_T> = 0x08 * is always 0 * when
         * <GR_OP_T> = 0x10 * is not reverse-engineered and may be non-zero. * *
         * Information reverse-engineered by B. Milhaupt and used with
         * permission *
         * **********************************************************************************
         */
        /* OPC_RE_IPL (IPL Ping Operation) */
        // Operations related to DigiIPL Device "Ping" operations
        //
        // "Ping" request issued from DigiIPL ver 1.09 issues this message on LocoNet.
        // The LocoNet request message encodes a serial number but NOT a device type.
        //
        // Depending on which devices are selected in DigiIPL when the "ping"
        // is selected, (and probably the S/Ns of the devices attached to the LocoNet,
        // the response is as follows:
        //     DT402D  LocoNet message includes the serial number from the DT402D's
        //             Slave (RF24) serial number.  If a UR92 is attached to LocoNet,
        //             it will send the message via its RF link to the addressed
        //             DT402D.  (UR92 apparantly assumes that the long 802.15.4
        //             address of the DT402D is based on the serial number embedded
        //             in the LocoNet message, with the MS 32 bits based on the UR92
        //             long address MS 32 bits).  If more than one UR92 is attached
        //             to LocoNet, all will pass the message to the RF interface.
        //     UR92    LocoNet message includes the Slave serial number from the UR92.
        //             These messages are not passed to the RF link by the addressed
        //             UR92.  If more than one UR92 is attached to LocoNet, and the
        //             addressed UR92 hears the RF version of the LocoNet message, it
        //             will respond via the RF interface with an acknowledge packet,
        //             and a UR92 (not sure which one) responds on LocoNet with a
        //             Ping report <e5><14><08><10>.
        //     PR3     LocoNet message includes an effective serial number of all
        //             zeros.  There is no LocoNet message reply generated to a
        //             request to a PR3 S/N, but there will be a reply on the PR3's
        //             computer interface if the ping request was sent via the PR3's
        //             computer interface (i.e. not from some other LocoNet agent).
        //     UT4D    While it has been suggested that the UT4D supports firmware
        //             updates, the UT4D does not respond to the Ping message.
        //     LNRP    While it has been suggested that the LNRP supports firmware
        //             updates, the LNRP does not respond to the Ping message.
        //
        // Ping Report values:
        //     <unkn1> Seems always to be <0C>.  None of the bytes relate to
        //             Duplex Channel Number.
        //     <unkn2> Matches byte 15 of the MAC payload of the reply sent by the
        //             targeted UR92.
        //     <unkn3> Unclear what this byte means.
        //
        // Information reverse-engineered by B. Milhaupt and used with permission
        switch (l.getElement(3)) {
            case 0x08:
                /* OPC_RE_IPL (IPL Ping Query) */
                // Ping Request: <e5><14><08><08><msBits><Sn0><Sn1><Sn2><Sn3><0><0><0><0><0><0><0><0><0><0><0><Chk>

                if ((((l.getElement(4) & 0xF) != 0) || (l.getElement(5) != 0)
                        || (l.getElement(6) != 0) || (l.getElement(7) != 0) || (l.getElement(8) != 0))
                        && (l.getElement(9) == 0) && (l.getElement(10) == 0)
                        && (l.getElement(11) == 0) && (l.getElement(12) == 0)
                        && (l.getElement(13) == 0) && (l.getElement(14) == 0)
                        && (l.getElement(15) == 0) && (l.getElement(16) == 0)
                        && (l.getElement(17) == 0) && (l.getElement(18) == 0)) {

                    int hostSnInt;
                    hostSnInt = (l.getElement(5) + (((l.getElement(4) & 0x1) == 1) ? 128 : 0))
                            + ((l.getElement(6) + (((l.getElement(4) & 0x2) == 2) ? 128 : 0)) * 256)
                            + ((l.getElement(7) + (((l.getElement(4) & 0x4) == 4) ? 128 : 0)) * 256 * 256)
                            + ((l.getElement(8) + (((l.getElement(4) & 0x8) == 8) ? 128 : 0)) * 256 * 256 * 256);
                    return Bundle.getMessage("LN_MSG_DUPLEX_PING_REQUEST",
                            Integer.toHexString(hostSnInt).toUpperCase());
                }
                break;
            case 0x10:
                /* OPC_RE_IPL (IPL Ping Report) */

                // Ping Report:  <e5><14><08><10><msbits><Sn0><Sn1><Sn2><Sn3><unkn1><0><0><Unkn2><Unkn3><0><0><0><0><0><Chk>
                if (((l.getElement(4) & 0xF) != 0) || (l.getElement(5) != 0) || (l.getElement(6) != 0)
                        || (l.getElement(7) != 0) || (l.getElement(8) != 0)) {   // if any serial number bit is non-zero //
                    int hostSnInt = (l.getElement(5) + (((l.getElement(4) & 0x1) == 1) ? 128 : 0))
                            + ((l.getElement(6) + (((l.getElement(4) & 0x2) == 2) ? 128 : 0)) * 256)
                            + ((l.getElement(7) + (((l.getElement(4) & 0x4) == 4) ? 128 : 0)) * 256 * 256)
                            + ((l.getElement(8) + (((l.getElement(4) & 0x8) == 8) ? 128 : 0)) * 256 * 256 * 256);
                    return Bundle.getMessage("LN_MSG_DUPLEX_PING_REPORT",
                            Integer.toHexString(hostSnInt).toUpperCase(),
                            StringUtil.twoHexFromInt(l.getElement(12) + (((l.getElement(9)) & 0x4) == 0x4 ? 128 : 0)).toUpperCase(),
                            StringUtil.twoHexFromInt(l.getElement(13) + (((l.getElement(9)) & 0x8) == 0x8 ? 128 : 0)).toUpperCase()
                    );
                }
                break;
            default:
                break;
        }
        return "";
    }

    private static String interpretOpcPeerXfer20_0f(LocoNetMessage l) {
        String device;

        switch (l.getElement(3)) {
            case 0x08: {
                if ((l.getElement(4) == 0)
                        && (l.getElement(5) == 0) && (l.getElement(6) == 0)
                        && (l.getElement(7) == 0) && (l.getElement(8) == 0)
                        && (l.getElement(9) == 0) && (l.getElement(10) == 0)
                        && (l.getElement(11) == 1) && (l.getElement(12) == 0)
                        && (l.getElement(13) == 0) && (l.getElement(14) == 0)
                        && (l.getElement(15) == 0) && (l.getElement(16) == 0)
                        && (l.getElement(17) == 0) && (l.getElement(18) == 0)) {
                    /**
                     * **********************************************************************************
                     * IPL capable device query - RE_IPL_IDENTITY_OPERATION
                     * (Device Query) * The message bytes are assigned as
                     * follows:
                     * <p>
                     * <E5> <14> <0F> <08> <00> <00>
                     * <00> <00> <00> <00> <00> <01>
                     * <00> <00> ...
                     * <p>
                     * <00> <00> <00> <00> <00> <CHK> * * Information
                     * reverse-engineered by B. Milhaupt and used with
                     * permission *
                     * **********************************************************************************
                     */
                    // Request for all IPL-queryable devices to report their presence
                    //
                    // Information reverse-engineered by B. Milhaupt and used with permission

                    return Bundle.getMessage("LN_MSG_IPL_DISCOVER_ALL_DEVICES");
                } else if (((l.getElement(5) != 0) || (l.getElement(6) != 0))) {
                    /**
                     * **********************************************************************************
                     * IPL device query by type - RE_IPL_IDENTITY_OPERATION
                     * (Device Query) * The message bytes are assigned as
                     * follows:
                     * <p>
                     * <E5> <14> <0F> <08> <DI_Hmf>
                     * <DI_Hst> <DI_Slv> <00> <00> <00>
                     * <00> <01> ...
                     * <p>
                     * <00> <00> <00> <00> <00> <00>
                     * <00> <CHK> * where:
                     * <p>
                     * <DI_Hmf> DigiIPL-capable Host device manufacturer number.
                     * This is not * the same as an NMRA Manufacturer ID. * 0x00
                     * Digitrax * Others No other Host device manufacturer *
                     * numbers have been reverse- * engineered
                     * <p>
                     * <DI_Hst> encodes the DigiIPL-capable Host device type as
                     * follows: * When <DI_Hmf> = 0x00 * 0x00 (0 decimal) No
                     * Host device type reported * 0x04 (4 decimal) UT4D (Note
                     * that UT4D, UT4 and UT4R do * not respond to this DigiIPL
                     * * request) * 0x18 (24 decimal) RF24 - not typically a
                     * Host device * 0x23 (35 decimal) PR3 * 0x2A (42 decimal)
                     * DT402 (or DT402R or DT402D) * 0x33 (51 decimal) DCS51 *
                     * 0x5C (92 decimal) UR92 * Others No other Host device
                     * types have been * reverse-engineered * When
                     * <DI_Hmf> is not 0x00 * All values Not reverse-engineered
                     * <p>
                     * <DI_Slv> encodes the DigiIPL-capable Slave device type as
                     * follows: * When <DI_Smf> = 0x00 * 0x00 (0 decimal) Report
                     * for all Slave device types * 0x18 (24 decimal) RF24 *
                     * Others No other Slave device types have been *
                     * reverse-engineered * * Information reverse-engineered by
                     * B. Milhaupt and used with permission *
                     * **********************************************************************************
                     */
                    // Request for IPL-queryable devices of given manufacturer and type to report
                    // their presence
                    //
                    // Note that standard definitions are provided for UT4D and RF24, even though these
                    // devices do not respond to this query.  Note that UT4D will respond to IPL capable
                    // device query with DI_Hmf = 0, DI_Hst = 0, DI_Slv = 0, and DI_Smf = 0.
                    //
                    // Information reverse-engineered by B. Milhaupt and used with permission

                    device = getDeviceNameFromIPLInfo(l.getElement(4), l.getElement(5));
                    String slave = getSlaveNameFromIPLInfo(l.getElement(4), l.getElement(6));
                    return Bundle.getMessage("LN_MSG_IPL_DISCOVER_SPECIFIC_DEVICES",
                            device, slave);
                }
                break;
            } // end case 0x08, which decodes 0xe5 0x14 0x0f 0x08
            case 0x10: {
                return interpretOpcPeerXfer20Sub10(l);
            } // end case 0x10, which decodes 0xe5 0x14 0x0f 0x10
            default: {
                break;
            }

        } // end of switch (l.getElement(3)), which decodes 0xe5 0x14 0x0f 0x??

        return "";
    }

    private static String interpretOpcPeerXfer20(LocoNetMessage l) {
        // Duplex Radio Management
        // DigiIPL messages
        // LocoIO, LocoServo, LocoBuffer, LocoBooster configuration messages

        switch (l.getElement(2)) {
            case 0x01: {
                // Seems to be a query for just duplex devices.
                String result = interpretOpcPeerXfer20_1(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x02: {
                // Request Duplex Radio Channel
                String result = interpretOpcPeerXfer20_2(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case 0x03: {
                // Duplex Group Name
                String result = interpretOpcPeerXfer20_3(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x04: {
                // Duplex Group ID
                String result = interpretOpcPeerXfer20_4(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x07: {
                // Duplex Group Password
                String result = interpretOpcPeerXfer20_7(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x10: {
                // Radio Channel Noise/Activity
                String result = interpretOpcPeerXfer20_10(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.RE_IPL_PING_OPERATION: { // case 0x08, which decodes 0xe5 0x14 0x08
                String result = interpretOpcPeerXfer20_8(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }

            case LnConstants.RE_IPL_IDENTITY_OPERATION: { // case 0x0f, which decodes 0xe5 0x14 0x0f
                // Operations related to DigiIPL "Ping", "Identify" and "Discover"
                String result = interpretOpcPeerXfer20_0f(l);
                if (result.length() > 0) {
                    return result;
                }
                break;

            }

            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer20Sub10(LocoNetMessage l) {
        /**
         * **********************************************************************************
         * IPL device identity report - RE_IPL_IDENTITY_OPERATION (Device
         * Report) * The message bytes are assigned as follows:
         * <p>
         * <E5> <14> <0F> <08> <DI_Hmf> <DI_Hst>
         * <DI_Slv> <DI_Smf> <DI_Hsw> ...
         * <p>
         * <DI_F1> <DI_Ssw> <DI_Hs0> <DI_Hs1>
         * <DI_Hs2> <DI_F2> <DI_Ss0> ...
         * <p>
         * <DI_Ss1> <DI_Ss2> <DI_Ss3> <CHK> * where:
         * <p>
         * <DI_Hmf> DigiIPL-capable Host device manufacturer number. This is not
         * * the same as an NMRA Manufacturer ID. * 0x00 Digitrax * Others No
         * other Host device manufacturer * numbers have been reverse- *
         * engineered
         * <p>
         * <DI_Hst> encodes the DigiIPL-capable Host device type as follows: *
         * When
         * <DI_Hmf> = 0x00 * 0x00 (0 decimal) No Host device type reported *
         * 0x04 (4 decimal) UT4D * 0x23 (35 decimal) PR3 * 0x2A (42 decimal)
         * DT402 (or DT402R or DT402D) * 0x33 (51 decimal) DCS51 * 0x5C (92
         * decimal) UR92 * Others No other Host device types have been *
         * reverse-engineered * When <DI_Hmf> is not 0x00 * All values Not
         * reverse-engineered
         * <p>
         * <DI_Slv> encodes the DigiIPL-capable Slave device type as follows: *
         * When
         * <DI_Smf> = 0x00 * 0x00 (0 decimal) Report for all Slave device types
         * * 0x18 (24 decimal) RF24 * Others No other Slave device types have
         * been * reverse-engineered
         * <p>
         * <DI_Smf> DigiIPL-capable Slave device manufacturer number. This is
         * not * the same as an NMRA Manufacturer ID. * 0x00 Digitrax * Others
         * No other Slave device manufacturer * numbers have been reverse- *
         * engineered
         * <p>
         * <DI_Hsw> encodes the DigiIPL-capable Host device firmware revision *
         * number as follows: * bit 7 always 0 * bits 6-3 Host device firmware
         * major revision number * bits 2-0 Host device firmware minor revision
         * number
         * <p>
         * <DI_F1> encodes additional bits for the Slave device firmware major *
         * revision number and for the Host device serial number. * bits 7-4
         * always 0000b * bit 3 Bit 23 of Host Device Serial Number * bit 2 Bit
         * 15 of Host Device Serial Number * bit 1 Bit 7 of Host Device Serial
         * Number * bit 0 bit 4 of Slave device firmware Major number
         * <p>
         * <DI_Ssw> encodes the DigiIPL-capable Slave device firmware revision *
         * number as follows: * bit 7 always 0 * bits 6-3 Host device firmware
         * major revision number * bits 6-3 4 least-significant bits of Slave
         * device firmware major * revision number (see also <DI_F1>[0]) * bits
         * 2-0 Slave device firmware minor revision number
         * <p>
         * <DI_Hs0> encodes 7 bits of the 24 bit Host device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 6-0 of Host device serial number
         * <p>
         * <DI_Hs1> encodes 7 bits of the 24 bit Host device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 14-9 of Host device serial number
         * <p>
         * <DI_Hs2> encodes 7 bits of the 24 bit Host device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 22-16 of Host device serial number
         * <p>
         * <DI_F2> encodes additional bits for the Slave device serial number. *
         * bits 7-4 always 0000b * bit 3 Bit 31 of Slave Device Serial Number *
         * bit 2 Bit 23 of Slave Device Serial Number * bit 1 Bit 15 of Slave
         * Device Serial Number * bit 0 Bit 7 of Slave Device Serial Number
         * <p>
         * <DI_Ss0> encodes 7 bits of the 32 bit Slave device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 6-0 of Slave device serial number
         * <p>
         * <DI_Ss1> encodes 7 bits of the 32 bit Slave device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 14-9 of Slave device serial number
         * <p>
         * <DI_Ss2> encodes 7 bits of the 32 bit Slave device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 22-16 of Slave device serial number
         * <p>
         * <DI_Ss3> encodes 7 bits of the 32 bit Slave device serial number: *
         * bit 7 always 0 * bits 6-3 Bits 30-24 of Slave device serial number *
         * * Information reverse-engineered by B. Milhaupt and used with
         * permission *
         * **********************************************************************************
         */
        // Request for one specific IPL-queryable device to return its identity information.
        // Expected response is of type <E5><14><10>...
        //
        // Note that standard definitions are provided for RF24, even though these
        // devices do not generate this report.
        //
        // Information reverse-engineered by B. Milhaupt and used with permission
        String hostType = getDeviceNameFromIPLInfo(l.getElement(4), l.getElement(5));

        String hostVer = ((l.getElement(8) & 0x78) >> 3) + "." + ((l.getElement(8) & 0x7));

        int hostSnInt = ((l.getElement(13) + (((l.getElement(9) & 0x8) == 8) ? 128 : 0)) * 256 * 256)
                + ((l.getElement(12) + (((l.getElement(9) & 0x4) == 4) ? 128 : 0)) * 256)
                + (l.getElement(11) + (((l.getElement(9) & 0x2) == 2) ? 128 : 0));
        String hostSN = Integer.toHexString(hostSnInt).toUpperCase();
        String hostInfo = Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_HOST_DETAILS",
                hostType, hostSN, hostVer);

        String slaveType = getSlaveNameFromIPLInfo(l.getElement(4), l.getElement(6));
        String slaveInfo;
        if (l.getElement(6) != 0) {
            String slaveVer = (((l.getElement(10) & 0x78) >> 3) + ((l.getElement(9) & 1) << 4)) + "." + ((l.getElement(10) & 0x7));
            int slaveSnInt
                    = ((l.getElement(15) + (((l.getElement(14) & 0x1) == 1) ? 128 : 0)))
                    + ((l.getElement(16) + (((l.getElement(14) & 0x2) == 2) ? 128 : 0)) * 256)
                    + ((l.getElement(17) + (((l.getElement(14) & 0x4) == 4) ? 128 : 0)) * 256 * 256)
                    + ((l.getElement(18) + (((l.getElement(14) & 0x8) == 8) ? 128 : 0)) * 256 * 256 * 256);
            slaveInfo = Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_SLAVE_DETAILS", slaveType,
                    Integer.toHexString(slaveSnInt).toUpperCase(),
                    slaveVer);
        } else {
            slaveInfo = Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_SLAVE_NO_SLAVE");
        }
        return Bundle.getMessage("LN_MSG_IPL_DEVICE_IDENTITY_REPORT",
                hostInfo,
                slaveInfo);
    }

    private static String interpretOpcPeerXfer16(LocoNetMessage l) {
        /*
         * SRC=7F is THROTTLE msg xfer
         *  ; <DSTL><DSTH> encode ID#,
         *  ; <0><0> is THROT B'CAST
         *  ; <PXCT1>=<0,XC2,XC1,XC0 - D4.7,D3.7,D2.7,D1.7>
         *  ; XC0-XC2=ADR type CODE-0=7 bit Peer
         * TO Peer adrs *
         *  ; 1=<D1>is SRC HI,<D2>is DST HI
         *  ; <PXCT2>=<0,XC5,XC4,XC3 - D8.7,D7.7,D6.7,D5.7>
         *  ; XC3-XC5=data type CODE- 0=ANSI TEXT string,
         *  ; balance RESERVED *
         * ****************************************************
         * SV programming format 1
         *
         * This is the message format as implemented by the certain
         * existing devices. New designs should not use this format. The
         * message bytes are assigned as follows:
         *   ; <0xE5> <0x10> <SRC> <DST> <0x01> <PXCT1>
         *   ; <D1> <D2> <D3> <D4> <PXCT2>
         *   ; <D5> <D6> <D7> <D8> <CHK>
         *
         * The upper nibble of PXCT1 must be 0,
         * and the upper nibble of PXCT2 must be 1. The meanings of the
         * remaining bytes are as defined in the LocoNet Personal
         * Edition specification.
         * *********************************************
         * SV programming format 2
         *
         * This is the recommended format for new designs.
         * The message bytes as assigned as follows: *
         *  ; <0xE5> <0x10> <SRC> <SV_CMD> <SV_TYPE> <SVX1>
         *  ; <DST_L> <DST_H> <SV_ADRL> <SV_ADRH> <SVX2>
         *  ; <D1> <D2> <D3> <D4> <CHK>
         *
         * The upper nibble of both SVX1 (PXCT1) and SVX2 (PXCT2) must be 1.
         */

        int src = l.getElement(2); // source of transfer
        int dst_l = l.getElement(3); // ls 7 bits of destination
        int dst_h = l.getElement(4); // ms 7 bits of destination
        int pxct1 = l.getElement(5);
        int pxct2 = l.getElement(10);

        int d[] = l.getPeerXfrData();

        if ((src == 0x7F) && (dst_l == 0x7F) && (dst_h == 0x7F)
                && ((pxct1 & 0x70) == 0x40)) {
            // Download (firmware?) messages.
            int sub = pxct2 & 0x70;
            switch (sub) {
                case 0x00: // setup
                    return Bundle.getMessage("LN_MSG_IPL_SETUP",
                            l.getElement(6),
                            l.getElement(8),
                            l.getElement(9),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(7))),
                            l.getElement(11));
                case 0x10: // set address
                    return Bundle.getMessage("LN_MSG_IPL_SET_ADDRESS",
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(d[0])
                                    + StringUtil.twoHexFromInt(d[1])
                                    + StringUtil.twoHexFromInt(d[2])));
                case 0x20: // send data
                case 0x30: // verify
                    return Bundle.getMessage((sub == 0x20) ? "LN_MSG_IPL_SEND_DATA" : "LN_MSG_IPL_VERIFY_REQUEST",
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[0])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[1])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[2])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[3])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[4])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[5])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[6])),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", StringUtil.twoHexFromInt(d[7])));
                case 0x40: // end op
                    return Bundle.getMessage("LN_MSG_IPL_END");
                default: // everything else isn't understood, go to default
                    break;
            }
        }

        if ((src == 0x7F) && (dst_l == 0x0) && (dst_h == 0x0)
                && ((pxct1 & 0x3) == 0x00) && ((pxct2 & 0x70) == 0x70)) {
            // throttle semaphore symbol message
            return Bundle.getMessage("LN_MSG_THROTTLE_SEMAPHORE",
                    ((d[0] * 128) + d[1]),
                    Bundle.getMessage(((d[2] & 0x10) == 0x10)
                            ? "LN_MSG_THROTTLE_SEMAPHORE_HELPER_LIT"
                            : "LN_MSG_THROTTLE_SEMAPHORE_HELPER_UNLIT"),
                    Bundle.getMessage(((d[2] & 0x08) == 0x08)
                            ? "LN_MSG_THROTTLE_SEMAPHORE_HELPER_LIT"
                            : "LN_MSG_THROTTLE_SEMAPHORE_HELPER_UNLIT"),
                    Bundle.getMessage(((d[2] & 0x04) == 0x04)
                            ? "LN_MSG_THROTTLE_SEMAPHORE_HELPER_LIT"
                            : "LN_MSG_THROTTLE_SEMAPHORE_HELPER_UNLIT"),
                    Bundle.getMessage(((d[2] & 0x02) == 0x02)
                            ? "LN_MSG_THROTTLE_SEMAPHORE_HELPER_LIT"
                            : "LN_MSG_THROTTLE_SEMAPHORE_HELPER_UNLIT"),
                    Bundle.getMessage(((d[2] & 0x01) == 0x01)
                            ? "LN_MSG_THROTTLE_SEMAPHORE_HELPER_BLINKING"
                            : "LN_MSG_THROTTLE_SEMAPHORE_HELPER_UNBLINKING")
            );
        }

        if ((src == 0x7F) && ((pxct1 & 0x70) == 0x00)) {

            if ((dst_l == 0x00) && (dst_h == 0x00)) {
                char c[] = new char[]{0, 0, 0, 0, 0, 0, 0, 0};
                c[0] = (char) d[0];
                c[1] = (char) d[1];
                c[2] = (char) d[2];
                c[3] = (char) d[3];
                c[4] = (char) d[4];
                c[5] = (char) d[5];
                c[6] = (char) d[6];
                c[7] = (char) d[7];
                return Bundle.getMessage("LN_MSG_THROTTLE_TEXT_MESSAGE_ALL_THROTTLES",
                        c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
            } else {
                return Bundle.getMessage("LN_MSG_THROTTLE_TEXT_MESSAGE_SPECIFIC_THROTTLE",
                        (char) d[0], (char) d[1],
                        (char) d[2], (char) d[3],
                        (char) d[4], (char) d[5],
                        (char) d[6], (char) d[7],
                        convertToMixed(dst_l, dst_h));
            }
        }

        String result = interpretSV1Message(l);
        if (result.length() > 0) {
            return result;
        }
        result = interpretSV0Message(l);
        if (result.length() > 0) {
            return result;
        }

        // check for a specific type - SV Programming messages format 2
        result = interpretSV2Message(l);
        if (result.length() > 0) {
            return result;
        }

        return "";
    }

    private static String interpretSV1Message(LocoNetMessage l) {
        int d[] = l.getPeerXfrData();
        if ((l.getElement(4) != 1)
                || ((l.getElement(5) & 0x70) != 0)
                || ((l.getElement(10) & 0x70) != 0x10)) {
            // is not an SV1 message
            return "";
        }
        if (l.getElement(2) == 0x50) {
            // Packets from the LocoBuffer
            String dst_subaddrx = (l.getElement(4) != 0x01 ? "" : ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : ""));
            // LocoBuffer to LocoIO
            return "LocoBuffer => LocoIO@"
                    + ((l.getElement(3) == 0) ? "broadcast" : Integer.toHexString(l.getElement(3)) + dst_subaddrx)
                    + " "
                    + (d[0] == 2 ? "Query SV" + d[1] : "Write SV" + d[1] + "=0x" + Integer.toHexString(d[3]))
                    + ((d[2] != 0) ? " Firmware rev " + dotme(d[2]) : "") + ".\n";
        }
        return "";
    }

    private static String interpretSV0Message(LocoNetMessage l) {
        int dst_h = l.getElement(4);
        int pxct1 = l.getElement(5);
        int pxct2 = l.getElement(10);
        if ((dst_h != 0x01) || ((pxct1 & 0xF0) != 0x00)
                || ((pxct2 & 0xF0) != 0x00)) {
            return "";
        }

        // (Jabour/Deloof LocoIO), SV Programming messages format 1
        int dst_l = l.getElement(3);
        int d[] = l.getPeerXfrData();
        int src = l.getElement(2);

        String src_subaddrx = ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : "");
        String dst_subaddrx = ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : "");

        String src_dev = ((src == 0x50) ? "Locobuffer" : "LocoIO@" + "0x" + Integer.toHexString(src) + src_subaddrx);
        String dst_dev = ((dst_l == 0x50) ? "LocoBuffer "
                : ((dst_l == 0x0) ? "broadcast"
                        : "LocoIO@0x" + Integer.toHexString(dst_l) + dst_subaddrx));
        String operation = (src == 0x50)
                ? ((d[0] == 2) ? "Query" : "Write")
                : ((d[0] == 2) ? "Report" : "Write");

        return src_dev + "=> " + dst_dev + " "
                + operation + " SV" + d[1]
                + ((src == 0x50) ? (d[0] != 2 ? ("=0x" + Integer.toHexString(d[3])) : "")
                        : " = " + ((d[0] == 2) ? ((d[2] != 0) ? (d[5] < 10) ? "" + d[5]
                                                : d[5] + " (0x" + Integer.toHexString(d[5]) + ")"
                                        : (d[7] < 10) ? "" + d[7]
                                                : d[7] + " (0x" + Integer.toHexString(d[7]) + ")")
                                : (d[7] < 10) ? "" + d[7]
                                        : d[7] + " (0x" + Integer.toHexString(d[7]) + ")"))
                + ((d[2] != 0) ? " Firmware rev " + dotme(d[2]) : "") + ".\n";
    }

    private static String interpretSV2Message(LocoNetMessage l) {
        // (New Designs)
        String svReply = "";
        LnSv2MessageContents svmc = null;
        try {
            // assume the message is an SV2 message
            svmc = new LnSv2MessageContents(l);
        } catch (IllegalArgumentException e) {
            // message is not an SV2 message.  Ignore the exception.
        }
        if (svmc != null) {
            // the message was indeed an SV2 message
            try {
                // get string representation of the message from an
                // available translation which is best suited to
                // the currently-active "locale"
                svReply = svmc.toString();
            } catch (IllegalArgumentException e) {
                // message is not a properly-formatted SV2 message.  Ignore the exception.
            }
        }
        return svReply;
    }

    private static String interpretOpcPeerXfer10(LocoNetMessage l) {
        // throttle status
        int tcntrl = l.getElement(2);
        String stat;
        switch (tcntrl) {
            case 0x40:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_OK");
                break;
            case 0x7F:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_NO_KEYPRESS");
                break;
            case 0x43:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_PLUS_KEY");
                break;
            case 0x42:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_MINUS_KEY");
                break;
            case 0x41:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_RUNSTOP_KEY");
                break;
            case 0x4e:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_RESP_SEM_DISP_CMD");
                break;
            default:
                stat = Bundle.getMessage("LN_MSG_THROTTLE_STATUS_HELPER_UNKONWN");
                break;
        }

        return Bundle.getMessage("LN_MSG_THROTTLE_STATUS",
                StringUtil.twoHexFromInt(tcntrl),
                stat,
                idString(l.getElement(3), l.getElement(4)),
                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                        StringUtil.twoHexFromInt(l.getElement(7))),
                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                        StringUtil.twoHexFromInt(l.getElement(8))));
    }

    private static String interpretOpcPeerXfer9(LocoNetMessage l, String reporterPrefix) {
        /*
         * Transponding "find" query and report messages.
         * Information reverse-engineered by B. Milhaupt and used with permission */
        switch (l.getElement(2)) {
            case 0x40: {
                /**
                 * **********************************************************************************
                 * Transponding "find" query message * The message bytes are
                 * assigned as follows:
                 * <p>
                 * <0xE5> <0x09> <0x40> <AD_H> <AD_L> <0x00>
                 * <0x00> <0x00> <CHK> * where:
                 * <p>
                 * <AD_H> is encoded as shown below: * When
                 * <AD_H> = 0x7D, * Address is a 7 bit value defined solely by
                 * <AD_L>. * When <AD_H> is not 0x7D, * Address is a 14 bit
                 * value; AD_H{6:0} represent the upper 7 bits * of the 14 bit
                 * address.
                 * <p>
                 * <AD_L> contains the least significant 7 bits of the 14 or 7
                 * bit address. * * Information reverse-engineered by B.
                 * Milhaupt and used with permission *
                 * **********************************************************************************
                 */
                String locoAddr = convertToMixed(l.getElement(4), l.getElement(3));
                return Bundle.getMessage("LN_MSG_TRANSP_FIND_QUERY",
                        locoAddr);
            }
            case 0x00: {
                /**
                 * **********************************************************************************
                 * Transponding "find" report message * The message bytes are
                 * assigned as follows:
                 * <p>
                 * <0xE5> <0x09> <0x00> <AD_H> <AD_L> <TR_ST>
                 * <TR_ZS> <0x00> <CHK> * where:
                 * <p>
                 * <AD_H> is encoded as shown below: * When
                 * <AD_H> = 0x7D, * Address is a 7 bit value defined solely by
                 * <AD_L>. * When <AD_H> is not 0x7D, * Address is a 14 bit
                 * value; AD_H{6:0} represent the upper 7 bits * of the 14 bit
                 * address.
                 * <p>
                 * <AD_L> contains the least significant 7 bits of the 14 or 7
                 * bit address.
                 * <p>
                 * <TR_ST> contains the transponding status for the addressed
                 * equipment, * encoded as: * bits 7-6 always 00b * bit 5
                 * encodes transponding presence * 0 = Addressed equipment is
                 * absent * 1 = Addressed equipment is present * bits 4-0 encode
                 * bits 7-3 of the Detection Section
                 * <p>
                 * <TR_ZS> contains the zone number and detection section,
                 * encoded as: * bit 7 always 0 * bits 6-4 encode bits 2-0 of
                 * the Detection Section * bits 3-1 encode the Transponding Zone
                 * as shown below * 000b Zone A * 001b Zone B * 010b Zone C *
                 * 011b Zone D * 100b Zone E * 101b Zone F * 110b Zone G * 111b
                 * Zone H * bit 0 always 0 * * Information reverse-engineered by
                 * B. Milhaupt and used with permission *
                 * **********************************************************************************
                 */

                int section = ((l.getElement(5) & 0x1F) << 3) + ((l.getElement(6) & 0x70) >> 4) + 1;
                String zone;
                String locoAddr = convertToMixed(l.getElement(4), l.getElement(3));

                switch (l.getElement(6) & 0x0F) {
                    case 0x00:
                        zone = "A";
                        break;
                    case 0x02:
                        zone = "B";
                        break;
                    case 0x04:
                        zone = "C";
                        break;
                    case 0x06:
                        zone = "D";
                        break;
                    case 0x08:
                        zone = "E";
                        break;
                    case 0x0A:
                        zone = "F";
                        break;
                    case 0x0C:
                        zone = "G";
                        break;
                    case 0x0E:
                        zone = "H";
                        break;
                    default:
                        zone = Bundle.getMessage("LN_MSG_TRANSP_HELPER_UNKNOWN_ZONE",
                                l.getElement(6) & 0x0F);
                        break;
                }

                // get system and user names
                String reporterSystemName = reporterPrefix
                        + ((l.getElement(5) & 0x1F) * 128 + l.getElement(6) + 1);

                Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterSystemName);

                String uname = "";
                if (reporter != null) {
                    uname = reporter.getUserName();
                }

                if ((uname != null) && (!uname.isEmpty())) {
                    return Bundle.getMessage("LN_MSG_TRANSP_REPORT_KNOWN_REPORTER_USERNAME",
                            locoAddr,
                            reporterSystemName,
                            uname,
                            section,
                            zone);
                }
                return Bundle.getMessage("LN_MSG_TRANSP_REPORT_KNOWN_REPORTER_UNKNOWN_USERNAME",
                        locoAddr,
                        reporterSystemName,
                        section,
                        zone);
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer7(LocoNetMessage l) {
        // This might be Uhlenbrock IB-COM start/stop programming track
        if (l.getElement(2) == 0x01 && l.getElement(3) == 0x49 && l.getElement(4) == 0x42) {
            switch (l.getElement(5)) {
                case 0x40: {
                    return Bundle.getMessage("LN_MSG_UHLENBROCK_STOP_PROGRAMMING_TRACK");
                }
                case 0x41: {
                    return Bundle.getMessage("LN_MSG_UHLENBROCK_START_PROGRAMMING_TRACK");
                }
                default:
                    break;
            }
        }
        return "";
    }

    private static String interpretOpcPeerXfer(LocoNetMessage l, String reporterPrefix) {
        String result = "";
        // The first byte seems to determine the type of message.
        switch (l.getElement(1)) {
            case 0x10: { //l.getZElement(1)
                result = interpretOpcPeerXfer16(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x0A: {
                result = interpretOpcPeerXfer10(l);
                if (result.length() > 0) {
                    return result;
                }
                break;

            }
            case 0x14: {
                result = interpretOpcPeerXfer20(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x09: { // l.getZElement(1)
                result = interpretOpcPeerXfer9(l, reporterPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            case 0x07: {
                result = interpretOpcPeerXfer7(l);
                if (result.length() > 0) {
                    return result;
                }
                break;
            }
            default: {
                break;
            }
        }
        return "";

    }

    private static String interpretLongAck(LocoNetMessage l) {
        int opcode = l.getElement(1);
        int ack1 = l.getElement(2);

        switch (opcode | 0x80) {
            case (LnConstants.OPC_LOCO_ADR):
                // response for OPC_LOCO_ADR
                return Bundle.getMessage("LN_MSG_LONG_ACK_LOCO_ADR");

            case (LnConstants.OPC_LINK_SLOTS):
                // response for OPC_LINK_SLOTS
                return Bundle.getMessage("LN_MSG_LONG_ACK_LINK_SLOTS");

            case (LnConstants.OPC_SW_ACK):
                // response for OPC_SW_ACK
                switch (ack1) {
                    case 0:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_SW_ACK_FULL");
                    case 0x7f:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_SW_ACK_ACCEPT");
                    default:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_SW_ACK_UNKNOWN",
                                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                        StringUtil.twoHexFromInt(ack1)))+
                                        Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());

                }
            case (LnConstants.OPC_SW_REQ):
                // response for OPC_SW_REQ
                return Bundle.getMessage("LN_MSG_LONG_ACK_SW_REQ_FAIL");

            case (LnConstants.OPC_WR_SL_DATA):
                // response for OPC_WR_SL_DATA
                switch (ack1) {
                    case 0:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_FAIL");
                    case 0x01:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_OK");
                    case 0x23:
                    case 0x2b:
                    case 0x6B:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_PROG_DCS51_OK");
                    case 0x40:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_BLIND");
                    case 0x7f:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_NOT_IMPL");
                    default:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_WR_SL_UNKNOWN",
                                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                        StringUtil.twoHexFromInt(ack1)))+
                                Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());

                }

            case (LnConstants.OPC_SW_STATE):
                // response for OPC_SW_STATE
                return Bundle.getMessage("LN_MSG_LONG_ACK_SW_STATE",
                        Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                StringUtil.twoHexFromInt(ack1)),
                        Bundle.getMessage((((ack1 & 0x20) != 0)
                                ? "LN_MSG_SWITCH_STATE_CLOSED"
                                : "LN_MSG_SWITCH_STATE_THROWN")));

            case (LnConstants.OPC_MOVE_SLOTS):
                // response for OPC_MOVE_SLOTS
                switch (ack1) {
                    case 0:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_MOVE_SL_REJECT");
                    case 0x7f:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_MOVE_SL_ACCEPT");
                    default:
                        return Bundle.getMessage("LN_MSG_LONG_ACK_MOVE_SL_UNKNOWN",
                                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                        StringUtil.twoHexFromInt(ack1)))+
                                Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());

                }

            case LnConstants.OPC_IMM_PACKET:
                // response for OPC_IMM_PACKET
                if (ack1 == 0) {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_REJECT");
                } else if (ack1 == 0x7f) {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_ACCEPT");
                } else if (l.getElement(1) == 0x6D && l.getElement(2) == 0x01) {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_UHL_PROG");

                } else {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_UNKNOWN",
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(ack1)))+
                                Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());

                }

            case LnConstants.OPC_IMM_PACKET_2:
                // response for OPC_IMM_PACKET
                return Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_LIM_MASTER",
                        ack1, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                StringUtil.twoHexFromInt(ack1)));

            case (LnConstants.RE_LACK_SPEC_CASE1 | 0x80): // 0x50 plus opcode bit so can match the switch'd value:
            case (LnConstants.RE_LACK_SPEC_CASE2 | 0x80): //0x00 plus opcode bit so can match the switch'd value:
                // OpSwitch read response reverse-engineered by B. Milhaupt and
                // used with permission
                int responseValue = l.getElement(2);
                if (responseValue == 0x7f) {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_SPEC_CASE1_2_ACCEPTED");
                } else {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_SPEC_CASE1_2_REPORT",
                            (((responseValue & 0x20) == 0x20) ? 1 : 0),
                            (((responseValue & 0x20) == 0x20)
                                    ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_CLOSED")
                                    : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_THROWN")));
                }
            case LnConstants.OPC_ALM_READ:
                if (l.getElement(2) == 0) {
                    return Bundle.getMessage("LN_MSG_LONG_ACK_SLOT_NOT_SUPPORTED",
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(opcode)));
                }
                break;
            default:
                break;
        }
        return "";
    }

    private static String interpretPm4xPowerEvent(LocoNetMessage l) {
        int pCMD = (l.getElement(3) & 0xF0);

        if ((pCMD == 0x30) || (pCMD == 0x10)) {
            // autoreverse
            int cm1 = l.getElement(3);
            int cm2 = l.getElement(4);
            String sect1Mode, sect1State;
            String sect2Mode, sect2State;
            String sect3Mode, sect3State;
            String sect4Mode, sect4State;

            if ((cm1 & 1) != 0) {
                sect1Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_AUTOREV");
                sect1State = ((cm2 & 1) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_REV")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NORM");
            } else {
                sect1Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_PROTECT");
                sect1State = ((cm2 & 1) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_SHORT")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NONSHORT");
            }

            if ((cm1 & 2) != 0) {
                sect2Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_AUTOREV");
                sect2State = ((cm2 & 2) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_REV")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NORM");
            } else {
                sect2Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_PROTECT");
                sect2State = ((cm2 & 2) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_SHORT")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NONSHORT");
            }

            if ((cm1 & 4) != 0) {
                sect3Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_AUTOREV");
                sect3State = ((cm2 & 4) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_REV")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NORM");
            } else {
                sect3Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_PROTECT");
                sect3State = ((cm2 & 4) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_SHORT")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NONSHORT");
            }

            if ((cm1 & 8) != 0) {
                sect4Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_AUTOREV");
                sect4State = ((cm2 & 8) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_REV")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NORM");
            } else {
                sect4Mode = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_PROTECT");
                sect4State = ((cm2 & 8) != 0)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_SHORT")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X_HELPER_MODE_NONSHORT");
            }
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_PM4X",
                    (l.getElement(2) + 1) + ((l.getElement(1) & 0x1) << 7),
                    sect1Mode, sect1State, sect2Mode, sect2State,
                    sect3Mode, sect3State, sect4Mode, sect4State);
        }
        if ((pCMD == 0x20) ) { //BXP88
            int cm1 = l.getElement(3);
            int cm2 = l.getElement(4);
            ArrayList<Integer> sectsShorted = new ArrayList<>();
            ArrayList<Integer> sectsUnshorted = new ArrayList<>();
            if ((cm2 & 0x01) != 0) {
                sectsShorted.add(1);
            } else {
                sectsUnshorted.add(1);
            }
            if ((cm2 & 0x02) != 0) {
                sectsShorted.add(2);
            } else {
                sectsUnshorted.add(2);
            }
            if ((cm2 & 0x04) != 0) {
                sectsShorted.add(3);
            } else {
                sectsUnshorted.add(3);
            }
            if ((cm2 & 0x08) != 0) {
                sectsShorted.add(4);
            } else {
                sectsUnshorted.add(4);
            }
            if ((cm1 & 0x01) != 0) {
                sectsShorted.add(5);
            } else {
                sectsUnshorted.add(5);
            }
            if ((cm1 & 0x02) != 0) {
                sectsShorted.add(6);
            } else {
                sectsUnshorted.add(6);
            }
            if ((cm1 & 0x04) != 0) {
                sectsShorted.add(7);
            } else {
                sectsUnshorted.add(7);
            }
            if ((cm1 & 0x08) != 0) {
                sectsShorted.add(8);
            } else {
                sectsUnshorted.add(8);
            }
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXP88",
                    (l.getElement(2) + 1) + ((l.getElement(1) & 0x1) << 7),
                    StringUtils.join(sectsShorted, ','), StringUtils.join(sectsUnshorted, ','));
        }
        if ( (pCMD == 0x50) || (pCMD == 0x40)) { //BXPA1
            int cm1 = l.getElement(3);
            String RevState = "";
            String BreakState = "";
            if ((cm1 & 0x10) != 0) { // reversing state
                if ((cm1 & 0x08) != 0) {
                    RevState = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXPA1_HELPER_MODE_REV");
                } else {
                    RevState = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXPA1_HELPER_MODE_NORM");
                }
            } else {
                // breaker state
                if ((cm1 & 0x08) != 0) {
                    BreakState = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXPA1_HELPER_MODE_SHORT");
                } else {
                    BreakState = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXPA1_HELPER_MODE_NONSHORT");
                }
            }
            int bxpa1_Id = ((l.getElement(2) << 3 ) + (l.getElement(3) & 0x07 ) + 1);
            // Due to a problem with the firmware messages from x and x+4 are identical
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_POWER_BXPA1",
                    bxpa1_Id, bxpa1_Id +4,
                    RevState, BreakState);
        }
        return "";
    }

    private static String interpretOpSws(LocoNetMessage l) {
        int pCMD = (l.getElement(3) & 0xF0);
        if (pCMD == 0x70) {
            // programming
            int deviceType = l.getElement(3) & 0x7;
            String device;
            switch (deviceType) {
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_PM4X:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_PM4X");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_BDL16X:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_BDL16X");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_SE8:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_SE8C");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_DS64:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_DS64");
                    break;
                default:
                    return "";
            }

            int val = (l.getElement(4) & 0x01);
            int opsw = (l.getElement(4) & 0x7E) / 2 + 1;
            int bdaddr = l.getElement(2) + 1;
            if ((l.getElement(1) & 0x1) != 0) {
                bdaddr += 128;
            }

            if ((deviceType == 0) && (bdaddr == 1) && (l.getElement(4) == 0)) {
                return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_ACCESS_QUERY_ALL");
            }

            if ((l.getElement(1) & 0x10) != 0) {
                // write
                String valType = (val == 1)
                        ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_CLOSED")
                        : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_THROWN");
                return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_WRITE_ACCESS",
                        device, bdaddr, opsw, val, valType);
            } else {
                // query
                return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_QUERY_ACCESS",
                        device, bdaddr, opsw);
            }
        }
        return "";
    }

    private static String interpretDeviceType(LocoNetMessage l) {
        int pCMD = (l.getElement(3) & 0xF0);
        if (pCMD == 0x00) {
            /**
             * **************************************************
             * Device type report * The message bytes as assigned as follows:
             * <p>
             * <0xD0> <DQT_REQ> <DQT_BRD> <DQT_B3> <DQT_B4>
             * <CHK> * * where:
             * <p>
             * <DQT_REQ> contains the device query request, * encoded as: * bits
             * 7-4 always 0110b * bits 3-1 always 001b * bit 0 (BoardID-1)<7>
             * <p>
             * <DQT_BRD> contains most the device board ID number, * encoded as:
             * * bit 7 always 0b * bits 6-0 (BoardID-1)<6:0>
             * <p>
             * <DQT_B3> contains the board type identification, * encoded as: *
             * bits 7-4 always 0000b * bits 3-0 contain the encoded device type,
             * * encoded as: * 0000b PM4x device * 0001b BDL16x device * 0010b
             * SE8C device * 0011b DS64 device * others Unknown device type
             * <p>
             * <DQT_B4> contains device version number: * bit 7 always 0b * bits
             * 6-0 VersionNumber(6:0) * * Information reverse-engineered by B.
             * Milhaupt and used with permission *
             * **************************************************
             */
            // This message is a report which is sent by a LocoNet device
            // in response to a query of attached devices
            // Note - this scheme is supported by only some Digitrax devices.
            //
            // A VersionNumber of 0 implies the hardware does not report
            // a valid version number.
            //
            // Device type report reverse-engineered by B. Milhaupt and
            // used with permission
            int deviceType = l.getElement(3) & 0x7;
            String device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_UNKNOWN");
            switch (deviceType) {
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_PM4X:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_PM4X");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_BDL16X:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_BDL16X");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_SE8:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_SE8C");
                    break;
                case LnConstants.RE_MULTI_SENSE_DEV_TYPE_DS64:
                    device = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_DS64");
                    break;
                default:
                    log.warn("Unhandled device type: {}", deviceType);
                    break;
            }

            int bdaddr = l.getElement(2) + 1;
            if ((l.getElement(1) & 0x1) != 0) {
                bdaddr += 128;
            }
            String versionNumber = Integer.toString(l.getElement(4));
            if (l.getElement(4) == 0) {
                versionNumber = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_DEV_RPT_HELPER_VER_UNKNOWN");
            }
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_DEV_TYPE_RPT",
                    device, bdaddr, versionNumber);
        }
        return "";
    }

    private static String interpretOpcMultiSense(LocoNetMessage l, String reporterPrefix) {
        int type = l.getElement(1) & LnConstants.OPC_MULTI_SENSE_MSG;
        switch (type) {
            case LnConstants.OPC_MULTI_SENSE_POWER:
                // This is a PM42 power event.
                String result = interpretPm4xPowerEvent(l);
                if (result.length() > 0) {
                    return result;
                }
                result = interpretOpSws(l);
                if (result.length() > 0) {
                    return result;
                }
                result = interpretDeviceType(l);
                if (result.length() > 0) {
                    return result;
                } else {
                    break;
                }

            case LnConstants.OPC_MULTI_SENSE_PRESENT:
            case LnConstants.OPC_MULTI_SENSE_ABSENT:
                result = interpretOpcMultiSenseTranspPresence(l, reporterPrefix);
                if (result.length() > 0) {
                    return result;
                }
                break;
            default:
                break;
        }
        return "";
    }

    private static String interpretOpcMultiSenseTranspPresence(LocoNetMessage l, String reporterPrefix) {
        // Transponding Event
        // get system and user names
        String reporterSystemName;
        String reporterUserName;
        String zone;
        int bxp88Zone = 1 + (l.getElement(2) & 0x07);
        switch (l.getElement(2) & 0x0f) { // ignore bit 0 which seems to provide some unknown info from the BXP88
            case 0x00:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEA");
                break;
            case 0x02:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEB");
                break;
            case 0x04:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEC");
                break;
            case 0x06:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONED");
                break;
            case 0x08:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEE");
                break;
            case 0x0A:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEF");
                break;
            case 0x0C:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEG");
                break;
            case 0x0E:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONEH");
                break;
            default:
                zone = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_ZONE_UNKNOWN",
                        (l.getElement(2) & 0x0F));
                break;
        }
        int type = l.getElement(1) & LnConstants.OPC_MULTI_SENSE_MSG;

        reporterSystemName = reporterPrefix
                + ((l.getElement(1) & 0x1F) * 128 + l.getElement(2) + 1);

        Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterSystemName);
        reporterUserName = "";
        if (reporter != null) {
            String uname = reporter.getUserName();
            if ((uname != null) && (!uname.isEmpty())) {
                reporterUserName = uname;
            }
        }
        int bxpa1Number = 1 + l.getElement(2) + (l.getElement(1) & 0x1F) * 128;
        int bxp88Number = 1 + (l.getElement(2)/8) + (l.getElement(1) & 0x1F) * 16;
        int section = 1 + (l.getElement(2) / 16) + (l.getElement(1) & 0x1F) * 8;

        String locoAddr = convertToMixed(l.getElement(4), l.getElement(3));
        String transpActivity = (type == LnConstants.OPC_MULTI_SENSE_PRESENT)
                ? Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_HELPER_IS_PRESENT")
                : Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_HELPER_IS_ABSENT");

        if ((l.getElement(2) & 0x1) == 0) {
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_REPORT_WITH_BXP88",
                    locoAddr, transpActivity, reporterSystemName,
                    reporterUserName, section, zone, bxp88Number, bxp88Zone, bxpa1Number);
        } else {
            return Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_TRANSP_REPORT_NOT_BDL16X",
                    locoAddr, transpActivity, reporterSystemName,
                    reporterUserName, bxp88Number, bxp88Zone, bxpa1Number);
        }
    }

    private static String interpretOpcWrSlDataOpcSlRdData(LocoNetMessage l) {
        int slot = l.getElement(2); // slot number for this request
        String mode;
        int command = l.getOpCode();
        int id1 = l.getElement(11); // ls 7 bits of ID code
        int id2 = l.getElement(12); // ms 7 bits of ID code
        /*
         * These messages share a common data format with the only difference being
         * whether we are reading or writing the slot data.
         */
        if (command == LnConstants.OPC_WR_SL_DATA) {
            mode = Bundle.getMessage("LN_MSG_SLOT_HELPER_ACCESS_TYPE_REQUEST");
        } else {
            mode = Bundle.getMessage("LN_MSG_SLOT_HELPER_ACCESS_TYPE_RESPONSE");
        }

        switch (slot) {
            case LnConstants.FC_SLOT:
                String result;
                result = interpretFastClockSlot(l, mode, id1, id2);
                if (result.length() > 0) {
                    return result;
                }
                break;
            case LnConstants.PRG_SLOT:
                result = interpretProgSlot(l, mode, id1, id2, command);
                if (result.length() > 0) {
                    return result;
                }
                break;

            case 0x79:
            case 0x7a:
            case 0x7D:
                return "";
            case LnConstants.CFG_EXT_SLOT:
                result = interpretCmdStnExtCfgSlotRdWr(l, command);
                if (result.length() > 0) {
                    return result;
                }
                break;

            // end programming track block
            case LnConstants.CFG_SLOT:
                result = interpretCmdStnCfgSlotRdWr(l, command);
                if (result.length() > 0) {
                    return result;
                }
                break;

            default:
                result = interpretStandardSlotRdWr(l, id1, id2, command, slot);
                if (result.length() > 0) {
                    return result;
                }
                break;
        }

        return "";
    }

    private static String interpretOpcInputRep(LocoNetMessage l, String sensorPrefix) {
        int in1 = l.getElement(1);
        int in2 = l.getElement(2);
        int contactNum = ((SENSOR_ADR(in1, in2) - 1) * 2 + ((in2 & LnConstants.OPC_INPUT_REP_SW) != 0 ? 2 : 1));
        // get system and user names
        String sensorSystemName = sensorPrefix + contactNum;
        String sensorUserName = "";
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorSystemName);
        sensorUserName = "";
        if (sensor != null) {
            String uname = sensor.getUserName();
            if ((uname != null) && (!uname.isEmpty())) {
                sensorUserName = uname;
            }
        }

        int sensorid = (SENSOR_ADR(in1, in2) - 1) * 2
                + ((in2 & LnConstants.OPC_INPUT_REP_SW) != 0 ? 2 : 1);

        int bdlid = ((sensorid - 1) / 16) + 1;
        int bdlin = ((sensorid - 1) % 16) + 1;
        String bdl = Bundle.getMessage("LN_MSG_OPC_INPUT_REP_BDL_INFO",
                bdlid, bdlin);

        int boardid = ((sensorid - 1) / 8) + 1;
        int boardindex = ((sensorid - 1) % 8);
        String otherBoardsNames;
        String otherBoardsInputs;
        if (sensorid < 289) {
            otherBoardsNames = Bundle.getMessage("LN_MSG_OPC_INPUT_REP_ALL_EQUIV_BOARDS", boardid);
            otherBoardsInputs = Bundle.getMessage("LN_MSG_OPC_INPUT_REPORT_INPUT_NAMES_ALL_EQUIV_BOARDS",
                    ds54sensors[boardindex], ds64sensors[boardindex],
                    se8csensors[boardindex]);
        } else {
            otherBoardsNames = Bundle.getMessage("LN_MSG_OPC_INPUT_REP_NO_SE8C", boardid);
            otherBoardsInputs = Bundle.getMessage("LN_MSG_OPC_INPUT_REPORT_INPUT_NAMES_NO_SE8C",
                    ds54sensors[boardindex], ds64sensors[boardindex]);
        }

        // There is no way to tell what kind of a board sent the message.
        // To be user friendly, we just print all the known combos.
        return Bundle.getMessage("LN_MSG_OPC_INPUT_REP",
                sensorSystemName, sensorUserName,
                Bundle.getMessage((in2 & LnConstants.OPC_INPUT_REP_HI) != 0
                        ? "LN_MSG_SENSOR_STATE_HIGH" : "LN_MSG_SENSOR_STATE_LOW"),
                bdl,
                otherBoardsNames, otherBoardsInputs);
    }

    private static String interpretOpcSwRep(LocoNetMessage l, String turnoutPrefix) {
        int sn1 = l.getElement(1);
        int sn2 = l.getElement(2);
        // get system and user names
        String turnoutUserName = "";

        String turnoutSystemName = turnoutPrefix
                + SENSOR_ADR(sn1, sn2);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutSystemName);

        String uname = "";
        if (turnout != null) {
            uname = turnout.getUserName();
            if ((uname != null) && (!uname.isEmpty())) {
                turnoutUserName = uname;
            } else {
                turnoutUserName = "";
            }
        }

        if ((sn2 & LnConstants.OPC_SW_REP_INPUTS) != 0) {
            return Bundle.getMessage("LN_MSG_OPC_SW_REP_INPUTS_STATE",
                    turnoutSystemName, turnoutUserName,
                    Bundle.getMessage(((sn2 & LnConstants.OPC_SW_REP_SW) != 0
                            ? "LN_MSG_SENSOR_SW_INPUT_TYPE_HI"
                            : "LN_MSG_SENSOR_SW_INPUT_TYPE_LO")),
                    Bundle.getMessage((((sn2 & LnConstants.OPC_SW_REP_HI) != 0)
                            ? "LN_MSG_SENSOR_SW_INPUT_STATE_HI"
                            : "LN_MSG_SENSOR_SW_INPUT_STATE_LO")));
        }
        return Bundle.getMessage("LN_MSG_OPC_SW_REP_OUTPUT_STATE",
                turnoutSystemName, turnoutUserName,
                Bundle.getMessage((((sn2 & LnConstants.OPC_SW_REP_CLOSED) != 0)
                        ? "LN_MSG_SENSOR_SW_OUTPUT_STATE_ON"
                        : "LN_MSG_SENSOR_SW_OUTPUT_STATE_OFF")),
                Bundle.getMessage((((sn2 & LnConstants.OPC_SW_REP_THROWN) != 0)
                        ? "LN_MSG_SENSOR_SW_OUTPUT_STATE_ON"
                        : "LN_MSG_SENSOR_SW_OUTPUT_STATE_OFF")));
    }

    private static String interpretOpcSwAck(LocoNetMessage l, String turnoutPrefix) {
        int sw2 = l.getElement(2);
        if ((sw2 & 0x40) == 0x40) {
            return "";
        }
        // get system and user names
        String turnoutUserName = "";

        String turnoutSystemName = turnoutPrefix
                + SENSOR_ADR(l.getElement(1), l.getElement(2));
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutSystemName);

        String uname = "";
        if (turnout != null) {
            uname = turnout.getUserName();
            if ((uname != null) && (!uname.isEmpty())) {
                turnoutUserName = uname;
            } else {
                turnoutUserName = "";
            }
        }

        String pointsDirection = ((sw2 & LnConstants.OPC_SW_ACK_CLOSED) != 0
                ? Bundle.getMessage("LN_MSG_SW_POS_CLOSED")
                : Bundle.getMessage("LN_MSG_SW_POS_THROWN"));
        String outputState = (((sw2 & LnConstants.OPC_SW_ACK_OUTPUT) != 0)
                ? Bundle.getMessage("LN_MSG_SENSOR_SW_OUTPUT_STATE_ON")
                : Bundle.getMessage("LN_MSG_SENSOR_SW_OUTPUT_STATE_OFF"));
        return Bundle.getMessage("LN_MSG_REQ_SWITCH", turnoutSystemName,
                turnoutUserName, pointsDirection, outputState);
    }

    private static String interpretOpcSwState(LocoNetMessage l, String turnoutPrefix) {
        // get system and user names
        if ((l.getElement(2) & 0x40) != 0x00) {
            return "";
        }
        String turnoutUserName = "";
        String turnoutSystemName = turnoutPrefix
                + SENSOR_ADR(l.getElement(1), l.getElement(2));
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutSystemName);

        String uname = "";
        if (turnout != null) {
            uname = turnout.getUserName();
            if ((uname != null) && (!uname.isEmpty())) {
                turnoutUserName = uname;
            } else {
                turnoutUserName = "";
            }
        }


        return Bundle.getMessage("LN_MSG_SW_STATE", turnoutSystemName,
                turnoutUserName);
    }

    private static String interpretOpcRqSlData(LocoNetMessage l) {
        int slot = l.getElement(1) + 128 * (l.getElement(2) & 0x07);
        boolean expSlotRequ = (l.getElement(2) & 0x40) == 0X40 ? true : false;
        switch (slot) {
         // Slots > 120 & < 128 are all special, but these are the only ones we know to decode.
         // Extended System Slots 248 thru 251 delt with seperately, not here
            case LnConstants.FC_SLOT:
                return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_FC_SLOT");
            case LnConstants.CFG_SLOT:
                return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_CFG_SLOT");
            case LnConstants.CFG_EXT_SLOT:
                return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_EXT_CFG_SLOT");
            case LnConstants.PRG_SLOT:
                return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_PRG_SLOT");
            case 0x79:
            case 0x7a:
            case 0x7d:
                break;
            default:
                if (expSlotRequ) {
                    return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_LOCO_EXP_SLOT", slot);
                } else {
                    return Bundle.getMessage("LN_MSG_SLOT_REQ_SLOT_LOCO_SLOT", slot);
                }
        }
        return "";
    }

    private static String interpretOpcMoveSlots(LocoNetMessage l) {
        int src = l.getElement(1);
        int dest = l.getElement(2);
        if ((src >= 0x79) && (src <= 0x7f)) {
            return "";
        }
        if ((dest >= 0x79) && (dest <= 0x7f)) {
            return "";
        }

        /* check special cases */
        if (src == 0) {
            /* DISPATCH GET */

            return Bundle.getMessage("LN_MSG_MOVE_SL_GET_DISP");
        } else if (src == dest) {
            /* IN USE */

            return Bundle.getMessage("LN_MSG_MOVE_SL_NULL_MOVE", src);
        } else if (dest == 0) {
            /* DISPATCH PUT */

            return Bundle.getMessage("LN_MSG_MOVE_SL_DISPATCH_PUT", src);
        } else {
            /* general move */

            return Bundle.getMessage("LN_MSG_MOVE_SL_MOVE", src, dest);
        }
    }

    private static String interpretOpcConsistFunc(LocoNetMessage l) {
        int slot = l.getElement(1);
        int dirf = l.getElement(2);
        if ((dirf & 0x40) == 0x40) {
            return "";
        }
        return Bundle.getMessage("LN_MSG_CONSIST_FUNC",
                slot,
                interpretDIRF(dirf));
    }

    private static String interpretOpcLocoSnd(LocoNetMessage l) {
        int slot = l.getElement(1);
        int snd = l.getElement(2);
        return Bundle.getMessage("LN_MSG_OPC_LOCO_SND",
                slot,
                Bundle.getMessage((snd & LnConstants.SND_F5) != 0
                        ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF"),
                Bundle.getMessage((snd & LnConstants.SND_F6) != 0
                        ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF"),
                Bundle.getMessage((snd & LnConstants.SND_F7) != 0
                        ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF"),
                Bundle.getMessage((snd & LnConstants.SND_F8) != 0
                        ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF"));

    }

    protected static String interpretDIRF(int dirf) {
        if ((dirf & 0x40) == 0x40) {
            return "";
        }
        String dirf0_4[] = interpretF0_F4toStrings(dirf);
        return Bundle.getMessage("LN_MSG_HELPER_DIRF",
                Bundle.getMessage((dirf & LnConstants.DIRF_DIR) != 0
                        ? "LN_MSG_DIRECTION_REV" : "LN_MSG_DIRECTION_FWD"),
                dirf0_4[0], dirf0_4[1], dirf0_4[2], dirf0_4[3], dirf0_4[4]);

    }

    private static String interpretOpcLocoDirf(LocoNetMessage l) {
        int slot = l.getElement(1);
        int dirf = l.getElement(2);

        String dirFinfo = interpretDIRF(dirf);
        if (dirFinfo.length() == 0) {
            return "";
        }

        return Bundle.getMessage("LN_MSG_OPC_LOCO_DIRF",
                slot, dirFinfo);
    }

    private static String interpretOpcLocoSpd(LocoNetMessage l) {
        int slot = l.getElement(1);
        int spd = l.getElement(2);

        if (spd == LnConstants.OPC_LOCO_SPD_ESTOP) {
            return Bundle.getMessage("LN_MSG_OPC_LOCO_SPD_ESTOP", slot);
        } else {
            return Bundle.getMessage("LN_MSG_OPC_LOCO_SPD_NORMAL", slot, spd);
        }

    }

    private static String interpretOpcPanelQuery(LocoNetMessage l) {
        switch (l.getElement(1)) {
            case 0x00: {
                return Bundle.getMessage("LN_MSG_OPC_DF_TETHERLESS_QUERY");
            }
            case 0x40: {
                if (l.getElement(2) == 0x1F) {
                    // Some UR devices treat this operation as a set plus query, others
                    // treat this only as a set.
                    return Bundle.getMessage("LN_MSG_OPC_DF_SET_LOCONETID", l.getElement(3));
                }
                break;
            }
            default: {
                break;
            }
        }
        return "";
    }

    private static String interpretOpcSwReq(LocoNetMessage l, String turnoutPrefix) {
        int sw1 = l.getElement(1);
        int sw2 = l.getElement(2);
        if ((sw2 & 0x40) == 0x40) {
            return "";
        }

        if ((!(((sw2 & 0xCF) == 0x0F) && ((sw1 & 0xFC) == 0x78)))
                && (!(((sw2 & 0xCF) == 0x07) && ((sw1 & 0xFC) == 0x78)))) {
            // ordinary form, LPU V1.0 page 9
            // handle cases which are not "stationary decoder interrogate" messages
            // get system and user names
            String turnoutUserName = "";

            String turnoutSystemName = turnoutPrefix
                    + SENSOR_ADR(l.getElement(1), l.getElement(2));
            Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutSystemName);

            String uname = "";
            if (turnout != null) {
                uname = turnout.getUserName();
                if ((uname != null) && (!uname.isEmpty())) {
                    turnoutUserName = uname;
                } else {
                    turnoutUserName = "";
                }
            }

            String pointsDirection = ((sw2 & LnConstants.OPC_SW_ACK_CLOSED) != 0
                    ? Bundle.getMessage("LN_MSG_SW_POS_CLOSED")
                    : Bundle.getMessage("LN_MSG_SW_POS_THROWN"));
            String outputState = ((sw2 & LnConstants.OPC_SW_ACK_OUTPUT) != 0
                    ? Bundle.getMessage("LN_MSG_SW_OUTPUT_STATE_ON")
                    : Bundle.getMessage("LN_MSG_SW_OUTPUT_STATE_OFF"));
            if (turnoutUserName.length() == 0) {
                return Bundle.getMessage("LN_MSG_OPC_SW_REQ_NORMAL_WITHOUT_USERNAME",
                        turnoutSystemName,
                        pointsDirection, outputState);
            } else {
                return Bundle.getMessage("LN_MSG_OPC_SW_REQ_NORMAL_WITH_USERNAME",
                        turnoutSystemName, turnoutUserName,
                        pointsDirection, outputState);
            }
        }

        /*
        Handle cases which are "stationary decoder interrogate" messages.
         */

        /*
         * Decodes a/c/b bits to allow proper creation of a list of addresses
         * which ought to reply to the "stationary decoder interrogate" message.
         */
        int a = (sw2 & 0x20) >> 5;
        int c = (sw1 & 0x02) >> 1;
        int b = (sw1 & 0x01);

        /*
         * All this blob does is loop through the ranges indicated by the
         * a/c/b bits, they are mask bits in the midde of the range. The
         * idea is to get 8 sensors at a time, since that is generally what
         * units have, and to query units 1, 9, 17... then 2, 10, 18... and
         * so on such that if they are all in a row they don't get hit at
         * the same time.
         */
        int topbits = 0;
        int midbits = (a << 2) + (c << 1) + b;
        int count = 0;
        StringBuilder addrListB = new StringBuilder();
        for (topbits = 0; topbits < 32; topbits++) {
            // The extra "+1" adjusts for the fact that we show 1-2048,
            // rather than 0-2047 on the wire.
            int lval = (topbits << 6) + (midbits << 3) + 1;
            int hval = lval + 7;

            if ((count % 8) != 0) {
                addrListB.append(", "); // NOI18N
            } else {
                if (count == 0) {
                    addrListB.append("\t"); // NOI18N
                } else {
                    addrListB.append(",\n\t");  // NOI18N
                }
            }
            addrListB.append("").append(lval);  // NOI18N
            addrListB.append("-").append(hval); // NOI18N
            count++;
        }

        String addrList = addrListB.toString();

        if (((sw2 & 0xCF) == 0x0F) && ((sw1 & 0xFC) == 0x78)) {
            // broadcast address LPU V1.0 page 12
            return Bundle.getMessage("LN_MSG_OPC_SW_REQ_INTERROGATE_TURNOUTS",
                    a, c, b, addrList);
        } else {
            // broadcast address LPU V1.0 page 13
            return Bundle.getMessage("LN_MSG_OPC_SW_REQ_INTERROGATE_SENSORS_TURNOUTS",
                    a, c, b, addrList);
        }
    }

    private static String interpretFastClockSlot(LocoNetMessage l, String mode, int id1, int id2) {
        /**
         * FAST Clock: The system FAST clock and parameters are implemented in
         * Slot#123 <7B>. Use <EF> to write new clock information, Slot read of
         * 0x7B,<BB><7B>.., will return current System clock information, and
         * other throttles will update to this SYNC. Note that all attached
         * display devices keep a current clock calculation based on this SYNC
         * read value, i.e. devices MUST not continuously poll the clock SLOT to
         * generate time, but use this merely to restore SYNC and follow current
         * RATE etc. This clock slot is typically "pinged" * or read SYNC'd
         * every 70 to 100 seconds, by a single user, so all attached devices
         * can synchronise any phase drifts. Upon seeing a SYNC read, all
         * devices should reset their local sub-minute phase counter and
         * invalidate the SYNC update ping generator.
         * <p>
         * Clock Slot Format:
         * <p>
         * <0xEF>,<0E>,<7B>,<CLK_RATE>,<FRAC_MINSL>,<FRAC_MINSH>,<256-MINS_60>,
         * <TRK><256-HRS_24>,<DAYS>,<CLK_CNTRL>,<ID1>,<1D2>,<CHK>
         * <p>
         * where:
         * <p>
         * <CLK_RATE> 0=Freeze clock, * 1=normal 1:1 rate, 10=10:1 etc, max
         * VALUE is 7F/128 to 1
         * <p>
         * <FRAC_MINSL> FRAC mins hi/lo are a sub-minute counter, depending on
         * the CLOCK generator
         * <p>
         * <FRAC_MINSH> Not for ext. usage. This counter is reset when valid
         * <E6><7B>
         * SYNC message is seen
         * <p>
         * <256-MINS_60> This is FAST clock MINUTES subtracted from 256. Modulo
         * 0-59
         * <p>
         * <256-HRS_24> This is FAST clock HOURS subtracted from 256. Modulo
         * 0-23
         * <p>
         * <DAYS> number of 24 Hr clock rolls, positive count
         * <p>
         * <CLK_CNTRL> Clock Control Byte D6- 1=This is valid Clock information,
         * 0=ignore this <E6><7B>, SYNC reply
         * <p>
         * <ID1>,<1D2> This is device ID last setting the clock.
         * <p>
         * <00><00> shows no set has happened
         * <p>
         * <7F><7x> are reserved for PC access *
         */

        int minutes; // temporary time values
        int hours;
        int clk_rate = l.getElement(3); // 0 = Freeze clock, 1 = normal,
        // 10 = 10:1 etc. Max is 0x7f
        int mins_60 = l.getElement(6); // 256 - minutes
        int track_stat = l.getElement(7); // track status
        int hours_24 = l.getElement(8); // 256 - hours
        int days = l.getElement(9); // clock rollovers
        int clk_cntrl = l.getElement(10); // bit 6 = 1; data is valid
        // clock info
        // "  " 0; ignore this reply
        // id1/id2 is device id of last device to set the clock
        // "   " = zero shows not set has happened

        /* recover hours and minutes values */
        minutes = ((255 - mins_60) & 0x7f) % 60;
        hours = ((256 - hours_24) & 0x7f) % 24;
        hours = (24 - hours) % 24;
        minutes = (60 - minutes) % 60;

        return Bundle.getMessage("LN_MSG_SLOT_ACCESS_FAST_CLOCK",
                mode,
                ((clk_cntrl & 0x20) != 0 ? "" : Bundle.getMessage("LN_MSG_SLOT_HELPER_FC_SYNC")),
                (clk_rate != 0 ? Bundle.getMessage("LN_MSG_SLOT_HELPER_FC_RUNNING")
                        : Bundle.getMessage("LN_MSG_SLOT_HELPER_FC_FROZEN")),
                clk_rate,
                days,
                fcTimeToString(hours, minutes),
                idString(id1, id2),
                trackStatusByteToString(track_stat));
    }

    private static String interpretProgSlot(LocoNetMessage l, String mode, int id1, int id2, int command) {
        /**
         * ********************************************************************************************
         * Programmer track: * ================= * The programmer track is
         * accessed as Special slot #124 ( $7C, 0x7C). It is a full *
         * asynchronous shared system resource. * * To start Programmer task,
         * write to slot 124. There will be an immediate LACK acknowledge * that
         * indicates what programming will be allowed. If a valid programming
         * task is started, * then at the final (asynchronous) programming
         * completion, a Slot read <E7> from slot 124 * will be sent. This is
         * the final task status reply. * * Programmer Task Start: *
         * ----------------------
         * <p>
         * <0xEF>,<0E>,<7C>,<PCMD>,<0>,<HOPSA>,<LOPSA>,<TRK>;<CVH>,<CVL>,
         * <p>
         * <DATA7>,<0>,<0>,<CHK> * * This OPC leads to immediate LACK codes:
         * <p>
         * <B4>,<7F>,<7F>,<chk> Function NOT implemented, no reply.
         * <p>
         * <B4>,<7F>,<0>,<chk> Programmer BUSY , task aborted, no reply.
         * <p>
         * <B4>,<7F>,<1>,<chk> Task accepted , <E7> reply at completion.
         * <p>
         * <B4>,<7F>,<0x40>,<chk> Task accepted blind NO <E7>
         * reply at completion. * * Note that the <7F> code will occur in
         * Operations Mode Read requests if the System is not * configured for
         * and has no Advanced Acknowlegement detection installed.. Operations
         * Mode * requests can be made and executed whilst a current Service
         * Mode programming task is keeping * the Programming track BUSY. If a
         * Programming request is rejected, delay and resend the * complete
         * request later. Some readback operations can keep the Programming
         * track busy for up * to a minute. Multiple devices, throttles/PC's
         * etc, can share and sequentially use the * Programming track as long
         * as they correctly interpret the response messages. Any Slot RD * from
         * the master will also contain the Programmer Busy status in bit 3 of
         * the <TRK> byte. * * A <PCMD> value of
         * <00> will abort current SERVICE mode programming task and will echo
         * with * an <E6> RD the command string that was aborted. * * <PCMD>
         * Programmer Command: * -------------------------- * Defined as * D7 -0
         * * D6 -Write/Read 1= Write, * 0=Read * D5 -Byte Mode 1= Byte
         * operation, * 0=Bit operation (if possible) * D4 -TY1 Programming Type
         * select bit * D3 -TY0 Prog type select bit * D2 -Ops Mode 1=Ops Mode
         * on Mainlines, * 0=Service Mode on Programming Track * D1 -0 reserved
         * * D0 -0-reserved * * Type codes: * ----------- * Byte Mode Ops Mode
         * TY1 TY0 Meaning * 1 0 0 0 Paged mode byte Read/Write on Service Track
         * * 1 0 0 0 Paged mode byte Read/Write on Service Track * 1 0 0 1
         * Direct mode byteRead/Write on Service Track * 0 0 0 1 Direct mode bit
         * Read/Write on Service Track * x 0 1 0 Physical Register byte
         * Read/Write on Service Track * x 0 1 1 Service Track- reserved
         * function * 1 1 0 0 Ops mode Byte program, no feedback * 1 1 0 1 Ops
         * mode Byte program, feedback * 0 1 0 0 Ops mode Bit program, no
         * feedback * 0 1 0 1 Ops mode Bit program, feedback * *
         * <HOPSA>Operations Mode Programming * 7 High address bits of Loco to
         * program, 0 if Service Mode
         * <p>
         * <LOPSA>Operations Mode Programming * 7 Low address bits of Loco to
         * program, 0 if Service Mode
         * <p>
         * <TRK> Normal Global Track status for this Master, * Bit 3 also is 1
         * WHEN Service Mode track is BUSY
         * <p>
         * <CVH> High 3 BITS of CV#, and ms bit of DATA.7
         * <p>
         * <0,0,CV9,CV8 - 0,0, D7,CV7>
         * <p>
         * <CVL> Low 7 bits of 10 bit CV address.
         * <p>
         * <0,CV6,CV5,CV4-CV3,CV2,CV1,CV0>
         * <p>
         * <DATA7>Low 7 BITS OF data to WR or RD COMPARE
         * <p>
         * <0,D6,D5,D4 - D3,D2,D1,D0> * ms bit is at CVH bit 1 position. * *
         * Programmer Task Final Reply: * ---------------------------- * (if saw
         * LACK
         * <B4>,<7F>,<1>,<chk> code reply at task start)
         * <p>
         * <0xE7>,<0E>,<7C>,<PCMD>,<PSTAT>,<HOPSA>,<LOPSA>,<TRK>;<CVH>,<CVL>,
         * <p>
         * <DATA7>,<0>,<0>,<CHK> * * <PSTAT> Programmer Status error flags.
         * Reply codes resulting from * completed task in PCMD * D7-D4 -reserved
         * * D3 -1= User Aborted this command * D2 -1= Failed to detect READ
         * Compare acknowledge response * from decoder * D1 -1= No Write
         * acknowledge response from decoder * D0 -1= Service Mode programming
         * track empty- No decoder detected * * This <E7> response is issued
         * whenever a Programming task is completed. It echos most of the *
         * request information and returns the PSTAT status code to indicate how
         * the task completed. * If a READ was requested <DATA7> and <CVH>
         * contain the returned data, if the PSTAT indicates * a successful
         * readback (typically =0). Note that if a Paged Read fails to detect a
         * * successful Page write acknowledge when first setting the Page
         * register, the read will be * aborted, showing no Write acknowledge
         * flag D1=1. *
         * ********************************************************************************************
         */
        int cvData;
        int cvNumber;

        // progTask = (progTaskMsg *) msgBuf;
        // slot - slot number for this request - slot 124 is programmer
        int pcmd = l.getElement(3); // programmer command
        int pstat = l.getElement(4); // programmer status error flags in
        // reply message
        int hopsa = l.getElement(5); // Ops mode - 7 high address bits
        // of loco to program
        int lopsa = l.getElement(6); // Ops mode - 7 low address bits of
        // loco to program
        /* trk - track status. Note: bit 3 shows if prog track is busy */
        int cvh = l.getElement(8); // hi 3 bits of CV# and msb of data7
        int cvl = l.getElement(9); // lo 7 bits of CV#
        int data7 = l.getElement(10); // 7 bits of data to program, msb
        // is in cvh above

        cvData = (((cvh & LnConstants.CVH_D7) << 6) | (data7 & 0x7f)); // was
        // PROG_DATA
        cvNumber = (((((cvh & LnConstants.CVH_CV8_CV9) >> 3) | (cvh & LnConstants.CVH_CV7)) * 128) + (cvl & 0x7f)) + 1; // was
        // PROG_CV_NUM(progTask)

        if (command == LnConstants.OPC_WR_SL_DATA) {
            /* interpret the programming mode request (to programmer) */
            switch ((pcmd & (LnConstants.PCMD_MODE_MASK | LnConstants.PCMD_RW))) {
                case LnConstants.PAGED_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_PAGED_RD",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.PAGED_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_PAGED_WR",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.DIR_BYTE_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_DIR_BYTE_RD",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.DIR_BYTE_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_DIR_BYTE_WR",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.DIR_BIT_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_DIR_BIT_RD",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber), cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true));
                case LnConstants.DIR_BIT_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_DIR_BIT_WR",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK:
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_BYTE_MODE:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_REG_BYTE_RD",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_RW:
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_BYTE_MODE | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_REG_BYTE_WR",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.SRVC_TRK_RESERVED:
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_BYTE_MODE:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_RD_RESERVED",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_RW:
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_BYTE_MODE | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_SRVC_TRK_WR_RESERVED",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.OPS_BYTE_NO_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BYTE_RD_NO_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.OPS_BYTE_NO_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BYTE_WR_NO_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.OPS_BYTE_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BYTE_RD_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.OPS_BYTE_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BYTE_WR_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.OPS_BIT_NO_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BIT_RD_NO_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.OPS_BIT_NO_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BIT_WR_NO_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case LnConstants.OPS_BIT_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BIT_RD_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.OPS_BIT_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_OPS_BIT_WR_FEEDBACK",
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                case 0:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_UHLENBROCK_RD",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_READ_REQ",
                                    cvNumber));
                case LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_UHLENBROCK_WR",
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
                default:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_REQUEST_UNKNOWN",
                            pcmd,
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(pcmd)),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_WRITE_REQ",
                                    cvNumber, cvData, Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)), StringUtil.to8Bits(cvData, true)));
            }
        } else {
            /* interpret the  programming mode response (from programmer) */
            /* if we're reading the slot back, check the status
             * this is supposed to be the Programming task final reply
             * and will have the resulting status byte.
             */
            String responseMessage = "(ODD BEHAVIOR - Default value not overwritten - report to developers!"; // NOI18N
            String hexMessage = "";
            if (pstat != 0) {
                if ((pstat & LnConstants.PSTAT_USER_ABORTED) != 0) {
                    responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_USER_ABORT");
                } else if ((pstat & LnConstants.PSTAT_READ_FAIL) != 0) {
                    responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_NO_READ_COMPARE_ACK_DETECT");
                } else if ((pstat & LnConstants.PSTAT_WRITE_FAIL) != 0) {
                    responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_NO_WRITE_ACK_DETECT");
                } else if ((pstat & LnConstants.PSTAT_NO_DECODER) != 0) {
                    responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_NO_LOCO_ON_PROGRAMMING_TRACK");
                } else if ((pstat & 0xF0) != 0) {
                    if ((pstat & 0xF0) == 0x10) {
                        // response from transponding decoder
                        responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_SUCCESS_VIA_RX4_BDL16X");

                    } else {
                        responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_UNDECODED",
                                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                        StringUtil.twoHexFromInt(pstat)));
                        hexMessage = Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());
                    }
                }
            } else {
                responseMessage = Bundle.getMessage("LN_MSG_SLOT_PROG_HELPER_RESPONSE_SUCCEEDED");
            }

            switch ((pcmd & (LnConstants.PCMD_MODE_MASK | LnConstants.PCMD_RW))) {
                case LnConstants.PAGED_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_PAGED_RD",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.PAGED_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_PAGED_WR",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.DIR_BYTE_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_DIR_BYTE_RD",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.DIR_BYTE_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_DIR_BYTE_WR",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber,
                                    cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.DIR_BIT_ON_SRVC_TRK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_DIR_BIT_RD",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.DIR_BIT_ON_SRVC_TRK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_DIR_BIT_WR",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK:
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_BYTE_MODE:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_REG_BYTE_RD",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_RW:
                case LnConstants.REG_BYTE_RW_ON_SRVC_TRK | LnConstants.PCMD_BYTE_MODE | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_REG_BYTE_WR",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.SRVC_TRK_RESERVED:
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_BYTE_MODE:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_RD_RESERVED",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_RW:
                case LnConstants.SRVC_TRK_RESERVED | LnConstants.PCMD_BYTE_MODE | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_SRVC_TRK_WR_RESERVED",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BYTE_NO_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BYTE_RD_NO_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BYTE_NO_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BYTE_WR_NO_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BYTE_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BYTE_RD_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BYTE_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BYTE_WR_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BIT_NO_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BIT_RD_NO_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BIT_NO_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BIT_WR_NO_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BIT_FEEDBACK:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BIT_RD_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.OPS_BIT_FEEDBACK | LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_OPS_BIT_WR_FEEDBACK",
                            responseMessage,
                            convertToMixed(lopsa, hopsa),
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case 0:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_UHLENBROCK_RD",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                case LnConstants.PCMD_RW:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_UHLENBROCK_WR",
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
                default:
                    return Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_RESPONSE_UNKNOWN",
                            pcmd,
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(pcmd)),
                            responseMessage,
                            Bundle.getMessage("LN_MSG_SLOT_PROG_MODE_CV_INFO_HELPER_REPLY",
                                    cvNumber, cvData,
                                    Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                            StringUtil.twoHexFromInt(cvData)),
                                    StringUtil.to8Bits(cvData, true)))+hexMessage;
            }
        }
    }

    private static String interpretCmdStnCfgSlotRdWr(LocoNetMessage l, int command) {

        /**
         * ************************************************
         * Configuration slot, holding op switches
         * ************************************************
         * <p>
         * NOTE: previously, this message provided specific text about the
         * meaning of each OpSw when it was closed. With the advent of newer
         * Digitrax command stations, the specific information was no longer
         * completely accurate. As such, this information now only shows bits as
         * "closed" or "thrown".
         */
        String thrown = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_THROWN");
        String closed = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_CLOSED");

        String opswGroup1, opswGroup2, opswGroup3, opswGroup4,
                opswGroup5, opswGroup6, opswGroup7, opswGroup8;
        opswGroup1 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                1, ((l.getElement(3) & 0x01) != 0 ? closed : thrown),
                2, ((l.getElement(3) & 0x02) != 0 ? closed : thrown),
                3, ((l.getElement(3) & 0x04) != 0 ? closed : thrown),
                4, ((l.getElement(3) & 0x08) != 0 ? closed : thrown),
                5, ((l.getElement(3) & 0x10) != 0 ? closed : thrown),
                6, ((l.getElement(3) & 0x20) != 0 ? closed : thrown),
                7, ((l.getElement(3) & 0x40) != 0 ? closed : thrown),
                8, thrown);
        opswGroup2 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                9, ((l.getElement(4) & 0x01) != 0 ? closed : thrown),
                10, ((l.getElement(4) & 0x02) != 0 ? closed : thrown),
                11, ((l.getElement(4) & 0x04) != 0 ? closed : thrown),
                12, ((l.getElement(4) & 0x08) != 0 ? closed : thrown),
                13, ((l.getElement(4) & 0x10) != 0 ? closed : thrown),
                14, ((l.getElement(4) & 0x20) != 0 ? closed : thrown),
                15, ((l.getElement(4) & 0x40) != 0 ? closed : thrown),
                16, thrown);
        opswGroup3 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                17, ((l.getElement(5) & 0x01) != 0 ? closed : thrown),
                18, ((l.getElement(5) & 0x02) != 0 ? closed : thrown),
                19, ((l.getElement(5) & 0x04) != 0 ? closed : thrown),
                20, ((l.getElement(5) & 0x08) != 0 ? closed : thrown),
                21, ((l.getElement(5) & 0x10) != 0 ? closed : thrown),
                22, ((l.getElement(5) & 0x20) != 0 ? closed : thrown),
                23, ((l.getElement(5) & 0x40) != 0 ? closed : thrown),
                24, thrown);
        opswGroup4 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                25, ((l.getElement(6) & 0x01) != 0 ? closed : thrown),
                26, ((l.getElement(6) & 0x02) != 0 ? closed : thrown),
                27, ((l.getElement(6) & 0x04) != 0 ? closed : thrown),
                28, ((l.getElement(6) & 0x08) != 0 ? closed : thrown),
                29, ((l.getElement(6) & 0x10) != 0 ? closed : thrown),
                30, ((l.getElement(6) & 0x20) != 0 ? closed : thrown),
                31, ((l.getElement(6) & 0x40) != 0 ? closed : thrown),
                32, thrown);
        opswGroup5 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                33, ((l.getElement(8) & 0x01) != 0 ? closed : thrown),
                34, ((l.getElement(8) & 0x02) != 0 ? closed : thrown),
                35, ((l.getElement(8) & 0x04) != 0 ? closed : thrown),
                36, ((l.getElement(8) & 0x08) != 0 ? closed : thrown),
                37, ((l.getElement(8) & 0x10) != 0 ? closed : thrown),
                38, ((l.getElement(8) & 0x20) != 0 ? closed : thrown),
                39, ((l.getElement(8) & 0x40) != 0 ? closed : thrown),
                40, thrown);
        opswGroup6 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                41, ((l.getElement(9) & 0x01) != 0 ? closed : thrown),
                42, ((l.getElement(9) & 0x02) != 0 ? closed : thrown),
                43, ((l.getElement(9) & 0x04) != 0 ? closed : thrown),
                44, ((l.getElement(9) & 0x08) != 0 ? closed : thrown),
                45, ((l.getElement(9) & 0x10) != 0 ? closed : thrown),
                46, ((l.getElement(9) & 0x20) != 0 ? closed : thrown),
                47, ((l.getElement(9) & 0x40) != 0 ? closed : thrown),
                48, thrown);
        opswGroup7 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                49, ((l.getElement(10) & 0x01) != 0 ? closed : thrown),
                50, ((l.getElement(10) & 0x02) != 0 ? closed : thrown),
                51, ((l.getElement(10) & 0x04) != 0 ? closed : thrown),
                52, ((l.getElement(10) & 0x08) != 0 ? closed : thrown),
                53, ((l.getElement(10) & 0x10) != 0 ? closed : thrown),
                54, ((l.getElement(10) & 0x20) != 0 ? closed : thrown),
                55, ((l.getElement(10) & 0x40) != 0 ? closed : thrown),
                56, thrown);
        opswGroup8 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                57, ((l.getElement(11) & 0x01) != 0 ? closed : thrown),
                58, ((l.getElement(11) & 0x02) != 0 ? closed : thrown),
                59, ((l.getElement(11) & 0x04) != 0 ? closed : thrown),
                60, ((l.getElement(11) & 0x08) != 0 ? closed : thrown),
                61, ((l.getElement(11) & 0x10) != 0 ? closed : thrown),
                62, ((l.getElement(11) & 0x20) != 0 ? closed : thrown),
                63, ((l.getElement(11) & 0x40) != 0 ? closed : thrown),
                64, thrown);
        return Bundle.getMessage(((command == LnConstants.OPC_WR_SL_DATA)
                ? "LN_MSG_SLOT_CMD_STN_CFG_WRITE_REQ"
                : "LN_MSG_SLOT_CMD_STN_CFG_READ_REPORT"),
                opswGroup1, opswGroup2, opswGroup3, opswGroup4,
                opswGroup5, opswGroup6, opswGroup7, opswGroup8);

    }

    private static String interpretCmdStnExtCfgSlotRdWr(LocoNetMessage l, int command) {
    /*
     * ************************************************
     * Extended Configuration slot, holding op switches
     * ************************************************
     */
        String thrown = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_THROWN");
        String closed = Bundle.getMessage("LN_MSG_OPC_MULTI_SENSE_OPSW_HELPER_CLOSED");

        String opswGroup1, opswGroup2, opswGroup3, opswGroup4,
                opswGroup5, opswGroup6, opswGroup7, opswGroup8;
        opswGroup1 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                65, ((l.getElement(3) & 0x01) != 0 ? closed : thrown),
                66, ((l.getElement(3) & 0x02) != 0 ? closed : thrown),
                67, ((l.getElement(3) & 0x04) != 0 ? closed : thrown),
                68, ((l.getElement(3) & 0x08) != 0 ? closed : thrown),
                69, ((l.getElement(3) & 0x10) != 0 ? closed : thrown),
                70, ((l.getElement(3) & 0x20) != 0 ? closed : thrown),
                71, ((l.getElement(3) & 0x40) != 0 ? closed : thrown),
                72, thrown);
        opswGroup2 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                73, ((l.getElement(4) & 0x01) != 0 ? closed : thrown),
                74, ((l.getElement(4) & 0x02) != 0 ? closed : thrown),
                75, ((l.getElement(4) & 0x04) != 0 ? closed : thrown),
                76, ((l.getElement(4) & 0x08) != 0 ? closed : thrown),
                77, ((l.getElement(4) & 0x10) != 0 ? closed : thrown),
                78, ((l.getElement(4) & 0x20) != 0 ? closed : thrown),
                79, ((l.getElement(4) & 0x40) != 0 ? closed : thrown),
                80, thrown);
        opswGroup3 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                81, ((l.getElement(5) & 0x01) != 0 ? closed : thrown),
                82, ((l.getElement(5) & 0x02) != 0 ? closed : thrown),
                83, ((l.getElement(5) & 0x04) != 0 ? closed : thrown),
                84, ((l.getElement(5) & 0x08) != 0 ? closed : thrown),
                85, ((l.getElement(5) & 0x10) != 0 ? closed : thrown),
                86, ((l.getElement(5) & 0x20) != 0 ? closed : thrown),
                87, ((l.getElement(5) & 0x40) != 0 ? closed : thrown),
                88, thrown);
        opswGroup4 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                89, ((l.getElement(6) & 0x01) != 0 ? closed : thrown),
                90, ((l.getElement(6) & 0x02) != 0 ? closed : thrown),
                91, ((l.getElement(6) & 0x04) != 0 ? closed : thrown),
                92, ((l.getElement(6) & 0x08) != 0 ? closed : thrown),
                93, ((l.getElement(6) & 0x10) != 0 ? closed : thrown),
                94, ((l.getElement(6) & 0x20) != 0 ? closed : thrown),
                95, ((l.getElement(6) & 0x40) != 0 ? closed : thrown),
                96, thrown);
        opswGroup5 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                97, ((l.getElement(8) & 0x01) != 0 ? closed : thrown),
                98, ((l.getElement(8) & 0x02) != 0 ? closed : thrown),
                99, ((l.getElement(8) & 0x04) != 0 ? closed : thrown),
                100, ((l.getElement(8) & 0x08) != 0 ? closed : thrown),
                101, ((l.getElement(8) & 0x10) != 0 ? closed : thrown),
                102, ((l.getElement(8) & 0x20) != 0 ? closed : thrown),
                103, ((l.getElement(8) & 0x40) != 0 ? closed : thrown),
                104, thrown);
        opswGroup6 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                105, ((l.getElement(9) & 0x01) != 0 ? closed : thrown),
                106, ((l.getElement(9) & 0x02) != 0 ? closed : thrown),
                107, ((l.getElement(9) & 0x04) != 0 ? closed : thrown),
                108, ((l.getElement(9) & 0x08) != 0 ? closed : thrown),
                109, ((l.getElement(9) & 0x10) != 0 ? closed : thrown),
                110, ((l.getElement(9) & 0x20) != 0 ? closed : thrown),
                111, ((l.getElement(9) & 0x40) != 0 ? closed : thrown),
                112, thrown);
        opswGroup7 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                113, ((l.getElement(10) & 0x01) != 0 ? closed : thrown),
                114, ((l.getElement(10) & 0x02) != 0 ? closed : thrown),
                115, ((l.getElement(10) & 0x04) != 0 ? closed : thrown),
                116, ((l.getElement(10) & 0x08) != 0 ? closed : thrown),
                117, ((l.getElement(10) & 0x10) != 0 ? closed : thrown),
                118, ((l.getElement(10) & 0x20) != 0 ? closed : thrown),
                119, ((l.getElement(10) & 0x40) != 0 ? closed : thrown),
                120, thrown);
        opswGroup8 = Bundle.getMessage("LN_MSG_SLOT_CMD_STN_CFG_HELPER_EIGHT_OPSWS",
                121, ((l.getElement(11) & 0x01) != 0 ? closed : thrown),
                122, ((l.getElement(11) & 0x02) != 0 ? closed : thrown),
                123, ((l.getElement(11) & 0x04) != 0 ? closed : thrown),
                124, ((l.getElement(11) & 0x08) != 0 ? closed : thrown),
                125, ((l.getElement(11) & 0x10) != 0 ? closed : thrown),
                126, ((l.getElement(11) & 0x20) != 0 ? closed : thrown),
                127, ((l.getElement(11) & 0x40) != 0 ? closed : thrown),
                128, thrown);
        return Bundle.getMessage(((command == LnConstants.OPC_WR_SL_DATA)
                ? "LN_MSG_SLOT_CMD_STN_EXT_CFG_WRITE_REQ"
                : "LN_MSG_SLOT_CMD_STN_EXT_CFG_READ_REPORT"),
                opswGroup1, opswGroup2, opswGroup3, opswGroup4,
                opswGroup5, opswGroup6, opswGroup7, opswGroup8);
    }

    private static String interpretStandardSlotRdWr(LocoNetMessage l, int id1, int id2, int command, int slot) {

        /**
         * ************************************************
         * normal slot read/write message - see info above *
         * ************************************************
         */
        int trackStatus = l.getElement(7); // track status
        int stat = l.getElement(3); // slot status
        int adr = l.getElement(4); // loco address
        int spd = l.getElement(5); // command speed
        int dirf = l.getElement(6); // direction and F0-F4 bits
        String[] dirf0_4 = interpretF0_F4toStrings(dirf);
        int ss2 = l.getElement(8); // slot status 2 (tells how to use
        // ID1/ID2 & ADV Consist)
        int adr2 = l.getElement(9); // loco address high
        int snd = l.getElement(10); // Sound 1-4 / F5-F8
        String[] sndf5_8 = interpretF5_F8toStrings(snd);

        String locoAdrStr = figureAddressIncludingAliasing(adr, adr2, ss2, id1, id2);
        return Bundle.getMessage(((command == LnConstants.OPC_WR_SL_DATA)
                ? "LN_MSG_SLOT_LOCO_INFO_WRITE"
                : "LN_MSG_SLOT_LOCO_INFO_READ"),
                slot,
                locoAdrStr,
                LnConstants.CONSIST_STAT(stat),
                LnConstants.LOCO_STAT(stat),
                LnConstants.DEC_MODE(stat),
                directionOfTravelString((dirf & LnConstants.DIRF_DIR) == 0),
                spd, // needs re-interpretation for some cases of slot consisting state
                dirf0_4[0],
                dirf0_4[1],
                dirf0_4[2],
                dirf0_4[3],
                dirf0_4[4],
                sndf5_8[0],
                sndf5_8[1],
                sndf5_8[2],
                sndf5_8[3],
                trackStatusByteToString(trackStatus),
                Bundle.getMessage("LN_MSG_SLOT_HELPER_SS2_SIMPLE",
                        Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                StringUtil.twoHexFromInt(ss2))),
                Bundle.getMessage("LN_MSG_SLOT_HELPER_ID1_ID2_AS_THROTTLE_ID",
                        idString(id1, id2)));
    }

    private static String interpretOpcPanelResponse(LocoNetMessage l) {
        switch (l.getElement(1)) {
            case 0x12: {
                // Bit 3 (0x08 in hex) is set by every UR-92 we've ever captured.
                // The hypothesis is this indicates duplex enabled, but this has
                // not been confirmed with Digitrax.
                return Bundle.getMessage("LN_MSG_OPC_D7_TETHERLESS_REPORT_UR92",
                        l.getElement(3) & 0x07,
                        ((l.getElement(3) & 0x08) == 0x08
                        ? Bundle.getMessage("LN_MSG_HELPER_D7_UR92_DUPLEX")
                        : ""));
            }
            case 0x17: {
                return Bundle.getMessage("LN_MSG_OPC_D7_TETHERLESS_REPORT_UR90",
                        l.getElement(3) & 0x07);
            }
            case 0x1F: {
                return Bundle.getMessage("LN_MSG_OPC_D7_TETHERLESS_REPORT_UR91",
                        l.getElement(3) & 0x07);
            }
            default: {
                return "";
            }
        }
    }

    private static String interpretOpcLissyUpdate(LocoNetMessage l) {
        /*
         * OPC_LISSY_UPDATE   0xE4
         *
         * LISSY is an automatic train detection system made by Uhlenbrock.
         * All documentation appears to be in German.
         */
        switch (l.getElement(1)) {
            case 0x08: // Format LISSY message
                int unit = (l.getElement(4) & 0x7F);
                int address = (l.getElement(6) & 0x7F) + 128 * (l.getElement(5) & 0x7F);
                switch (l.getElement(2)) {
                    case 0x00:
                        // Reverse-engineering note: interpretation of element 2 per wiki.rocrail.net
                        // OPC_LISSY_REP
                        return Bundle.getMessage("LN_MSG_LISSY_IR_REPORT_LOCO_MOVEMENT",
                                unit,
                                Integer.toString(address),
                                ((l.getElement(3) & 0x20) == 0
                                ? Bundle.getMessage("LN_MSG_LISSY_IR_REPORT_HELPER_DIRECTION_NORTH")
                                : Bundle.getMessage("LN_MSG_LISSY_IR_REPORT_HELPER_DIRECTION_SOUTH")));
                    case 0x01:
                        // Reverse-engineering note: interpretation of element 2 per wiki.rocrail.net
                        // OPC_WHEELCNT_REP
                        int wheelCount = (l.getElement(6) & 0x7F) + 128 * (l.getElement(5) & 0x7F);
                        return Bundle.getMessage("LN_MSG_LISSY_WHEEL_REPORT_LOCO_MOVEMENT",
                                unit, Integer.toString(wheelCount),
                                ((l.getElement(3) & 0x20) == 0
                                ? Bundle.getMessage("LN_MSG_LISSY_IR_REPORT_HELPER_DIRECTION_NORTH")
                                : Bundle.getMessage("LN_MSG_LISSY_IR_REPORT_HELPER_DIRECTION_SOUTH")));
                    default:
                        break;
                }
                break;

            case 0x0A: // Format special message
                int element = l.getElement(2) * 128 + l.getElement(3);
                int stat1 = l.getElement(5);
                int stat2 = l.getElement(6);
                String status;
                switch (stat1 & 0x30) {
                    case 0x30:
                        status = Bundle.getMessage("LN_MSG_SE_REPORT_HELPER_BOTH_RES");
                        break;
                    case 0x10:
                        status = Bundle.getMessage("LN_MSG_SE_REPORT_HELPER_AX_RES");
                        break;
                    case 0x20:
                        status = Bundle.getMessage("LN_MSG_SE_REPORT_HELPER_XA_RES");
                        break;
                    default:
                        status = Bundle.getMessage("LN_MSG_SE_REPORT_HELPER_NO_RES");
                        break;
                }

                return Bundle.getMessage("LN_MSG_SE_REPORT",
                        (element + 1), element,
                        l.getElement(7), l.getElement(8),
                        status,
                        Bundle.getMessage(((stat2 & 0x01) != 0)
                                ? "LN_MSG_SWITCH_STATE_THROWN"
                                : "LN_MSG_SWITCH_STATE_CLOSED"),
                        Bundle.getMessage(((stat1 & 0x01) != 0)
                                ? "LN_MSG_SE_REPORT_HELPER_OCCUPIED"
                                : "LN_MSG_SE_REPORT_HELPER_UNOCCUPIED"));
            case 0x09:
                if (l.getElement(4) == 0x00) {
                    return Bundle.getMessage("LN_MSG_UNRECOGNIZED_SIG_STATE_REPORT_MAY_BE_FROM_CML_HW")+
                                Bundle.getMessage("LN_MONITOR_MESSGAGE_RAW_HEX_INFO", l.toString());
                }
                break;
            default:
                break;
        }
        return "";
    }

    private static String interpretOpcImmPacket(LocoNetMessage l) {
        /*
         * OPC_IMM_PACKET   0xED   ;SEND n-byte packet immediate LACK
         *                         ; Follow on message: LACK
         *                         ; <0xED>,<0B>,<7F>,<REPS>,<DHI>,<IM1>,<IM2>,
         *                         ;        <IM3>,<IM4>,<IM5>,<CHK>
         *                         ;   <DHI>=<0,0,1,IM5.7-IM4.7,IM3.7,IM2.7,IM1.7>
         *                         ;   <REPS>  D4,5,6=#IM bytes,
         *                         ;           D3=0(reserved);
         *                         ;           D2,1,0=repeat CNT
         *                         ; IF Not limited MASTER then
         *                         ;   LACK=<B4>,<7D>,<7F>,<chk> if CMD ok
         *                         ; IF limited MASTER then Lim Masters respond
         *                         ;   with <B4>,<7E>,<lim adr>,<chk>
         *                         ; IF internal buffer BUSY/full respond
         *                         ;   with <B4>,<7D>,<0>,<chk>
         *                         ;   (NOT IMPLEMENTED IN DT200)
         *
         * This sends a raw NMRA packet across the LocoNet.
         *
         * Page 11 of LocoNet Personal Edition v1.0.
         *
         * Decodes for the F9-F28 functions taken from the NMRA standards and
         * coded by Leo Bicknell.
         */
        // sendPkt = (sendPktMsg *) msgBuf;
        int val7f = l.getElement(2);
        /* fixed value of 0x7f */

        int reps = l.getElement(3);
        /* repeat count */

        int dhi = l.getElement(4);
        /* high bits of data bytes */

        int im1 = l.getElement(5);
        int im2 = l.getElement(6);
        int im3 = l.getElement(7);
        int im4 = l.getElement(8);
        int im5 = l.getElement(9);
        int mobileDecoderAddress = -999;
        int nmraInstructionType = -999;
        int nmraSubInstructionType = -999;
        int playableWhistleLevel = -999;

        // see if it really is a 'Send Packet' as defined in LocoNet PE
        if ((val7f == 0x7f) && (l.getElement(1) == 0x0B)) {
            int len = ((reps & 0x70) >> 4);
            if (len < 2) {
                return ""; // no valid NMRA packets of less than 2 bytes.
            }
            // duplication of packet data as packetInt was deemed necessary
            // due to issues with msBit loss when converting from "byte" to
            // integral forms
            byte[] packet = new byte[len];
            int[] packetInt = new int[len];
            packet[0] = (byte) (im1 + ((dhi & 0x01) != 0 ? 0x80 : 0));
            packetInt[0] = (im1 + ((dhi & 0x01) != 0 ? 0x80 : 0));

            // len >= 2 always true at this point
            packet[1] = (byte) (im2 + ((dhi & 0x02) != 0 ? 0x80 : 0));
            packetInt[1] = (im2 + ((dhi & 0x02) != 0 ? 0x80 : 0));

            if (len >= 3) {
                packet[2] = (byte) (im3 + ((dhi & 0x04) != 0 ? 0x80 : 0));
                packetInt[2] = (im3 + ((dhi & 0x04) != 0 ? 0x80 : 0));
            }
            if (len >= 4) {
                packet[3] = (byte) (im4 + ((dhi & 0x08) != 0 ? 0x80 : 0));
                packetInt[3] = (im4 + ((dhi & 0x08) != 0 ? 0x80 : 0));
            }
            if (len >= 5) {
                packet[4] = (byte) (im5 + ((dhi & 0x10) != 0 ? 0x80 : 0));
                packetInt[4] = (im5 + ((dhi & 0x10) != 0 ? 0x80 : 0));
            }

            int address;
            // compute some information which is useful for decoding
            // the "Playable" whistle message
            // Information reverse-engineered by B. Milhaupt and used with permission
            if ((packetInt[0] & 0x80) == 0x0) {
                // immediate packet addresses a 7-bit multi-function (mobile) decoder
                mobileDecoderAddress = packetInt[0];
                nmraInstructionType = (packetInt[1] & 0xE) >> 5;
                nmraSubInstructionType = (packetInt[1] & 0x1f);
                if ((nmraSubInstructionType == 0x1d) && (packetInt[2] == 0x7f)) {
                    playableWhistleLevel = packetInt[3];
                }
            } else if ((packetInt[0] & 0xC0) == 0xC0) {
                // immediate packet addresses a 14-bit multi-function (mobile) decoder
                mobileDecoderAddress = ((packetInt[0] & 0x3F) << 8) + packetInt[1];
                nmraInstructionType = (packetInt[2] & 0xE0) >> 5;
                nmraSubInstructionType = (packetInt[2] & 0x1f);
                if ((nmraSubInstructionType == 0x1d) && (packetInt[3] == 0x7f)) {
                    playableWhistleLevel = packetInt[4];
                }
            } else {
                // immediate packet not addressed to a multi-function (mobile) decoder
                log.debug("got Here 1.");
            }
            if ((mobileDecoderAddress >= 0)
                    && (nmraInstructionType == 1)
                    && (nmraSubInstructionType == 0x1D)) {
                // the "Playable" whistle message
                // Information reverse-engineered by B. Milhaupt and used with permission
                return Bundle.getMessage("LN_MSG_PLAYABLE_WHISTLE_CONTROL",
                        Integer.toString(mobileDecoderAddress),
                        playableWhistleLevel,
                        (reps & 0x7));
            }

            // F9-F28 w/a long address.
            if ((packetInt[0] & 0xC0) == 0xC0) {
                address = ((packetInt[0] & 0x3F) << 8) + packetInt[1];

                if ((packetInt[2] & 0xFF) == 0xDF) {
                    // Functions 21-28
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F21_TO_F28",
                            Integer.toString(address),
                            Bundle.getMessage(((packetInt[3] & 0x01) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x02) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x04) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x08) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x10) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x20) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x40) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[3] & 0x80) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
                } else if ((packetInt[2] & 0xFF) == 0xDE) {
                    // Functions 13-20
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F13_TO_F20",
                            Integer.toString(address),
                            Bundle.getMessage((((packetInt[3] & 0x01) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x02) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x04) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x08) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x10) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x20) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x40) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[3] & 0x80) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
                } else if ((packetInt[2] & 0xF0) == 0xA0) {
                    // Functions 9-12
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F9_TO_F12",
                            Integer.toString(address),
                            Bundle.getMessage((((packetInt[2] & 0x01) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[2] & 0x02) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[2] & 0x04) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage((((packetInt[2] & 0x08) != 0) ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
                } else {
                    return Bundle.getMessage("LN_MSG_OPC_IMM_PKT_GENERIC",
                            ((reps & 0x70) >> 4),
                            (reps & 0x07),
                            reps,
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(dhi)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im1)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im2)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im3)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im4)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im5)),
                            NmraPacket.format(packet));
                }
            } else { // F9-F28 w/a short address.
                address = packetInt[0];
                if ((packetInt[1] & 0xFF) == 0xDF) {
                    // Functions 21-28
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F21_TO_F28",
                            address,
                            Bundle.getMessage(((packetInt[2] & 0x01) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x02) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x04) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x08) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x10) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x20) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x40) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x80) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));

                } else if ((packetInt[1] & 0xFF) == 0xDE) {
                    // Functions 13-20
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F13_TO_F20",
                            address,
                            Bundle.getMessage(((packetInt[2] & 0x01) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x02) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x04) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x08) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x10) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x20) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x40) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[2] & 0x80) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
                } else if ((packetInt[1] & 0xF0) == 0xA0) {
                    // Functions 9-12
                    return Bundle.getMessage("LN_MSG_SEND_PACKET_IMM_SET_F9_TO_F12",
                            address,
                            Bundle.getMessage(((packetInt[1] & 0x01) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[1] & 0x02) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[1] & 0x04) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                            Bundle.getMessage(((packetInt[1] & 0x08) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
                } else {
                    // Unknown
                    return Bundle.getMessage("LN_MSG_OPC_IMM_PKT_GENERIC",
                            ((reps & 0x70) >> 4),
                            (reps & 0x07),
                            reps,
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(dhi)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im1)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im2)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im3)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im4)),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(im5)),
                            NmraPacket.format(packet));
                }
            } // else { // F9-F28 w/a short address.
        } else if (l.getElement(1) == 0x1F) {
            if (l.getElement(2) == 0x01 && l.getElement(3) == 0x49 && l.getElement(4) == 0x42
                    && l.getElement(6) != 0x5E && l.getElement(10) == 0x70 && l.getElement(11) == 0x00 && l.getElement(15) == 0x10) {
                // Uhlenbrock IB-COM / Intellibox I and II read or write CV value on programming track
                String cv = Integer.toString(l.getElement(8) * 256 + ((l.getElement(5) & 0x02) * 64) + l.getElement(7));
                int val = l.getElement(9) + 16 * (l.getElement(5) & 0x08);
                switch (l.getElement(6)) {
                    case 0x6C:
                        return Bundle.getMessage("LN_MSG_UHLEN_READ_CV_REG_MODE_FROM_PT", cv);
                    case 0x6D:
                        return Bundle.getMessage("LN_MSG_UHLEN_WRITE_CV_REG_MODE_FROM_PT", cv);
                    case 0x6E:
                        return Bundle.getMessage("LN_MSG_UHLEN_READ_CV_PAGED_MODE_FROM_PT", cv);
                    case 0x6F:
                        return Bundle.getMessage("LN_MSG_UHLEN_WRITE_CV_PAGED_MODE_FROM_PT", cv);
                    case 0x71:
                        return Bundle.getMessage("LN_MSG_UHLEN_WRITE_CV_DIRECT_BYTE_MODE_FROM_PT",
                                cv, val);
                    case 0x70: // observed on Intellibox II, even though it does not work on IB-COM
                    case 0x72:
                        return Bundle.getMessage("LN_MSG_UHLEN_READ_CV_DIRECT_BYTE_MODE_FROM_PT", cv);
                    default:
                        break;
                }
                return "";
            } else if (l.getElement(2) == 0x01 && l.getElement(3) == 0x49 && l.getElement(4) == 0x42
                    && l.getElement(6) == 0x5E) {
                // Uhlenbrock IB-COM / Intellibox I and II write CV value on main track
                int addr = l.getElement(8) * 256 + ((l.getElement(5) & 0x02) * 64) + l.getElement(7);
                String cv = Integer.toString(l.getElement(11) * 256 + ((l.getElement(5) & 0x08) << 4) + l.getElement(9));
                int val = ((l.getElement(10) & 0x02) << 6) + l.getElement(12);
                return Bundle.getMessage("LN_MSG_UHLEN_CV_OPS_MODE_WRITE",
                        addr, cv, val);
            }
        }
        return ""; // not an understood message.
    }

    private static String interpretOpcPr3Mode(LocoNetMessage l) {
        /*
         * Sets the operating mode of the PR3 device, if present.
         *
         * Information reverse-engineered by B. Milhaupt and used with permission
         */

        if ((l.getElement(1) == 0x10) && ((l.getElement(2) & 0x7c) == 0)
                && (l.getElement(3) == 0) && (l.getElement(4) == 0)) {
            // set PR3 mode of operation, where LS 2 bits of byte 2 are encoded as:
            // 0x00 Set the PR3 mode to MS100 interface mode with PR3 LocoNet termination disabled
            // 0x01 Set the PR3 to decoder programming track mode
            // 0x03 Set the PR3 to MS100 interface mode with PR3 LocoNet termination enabled

            switch (l.getElement(2) & 0x3) {
                case 0x00: {
                    return Bundle.getMessage("LN_MSG_SET_PR3_MODE_LOCONET_IF_WITHOUT_TERM");
                }
                case 0x02: {
                    return Bundle.getMessage("LN_MSG_SET_PR3_MODE_PR3_PROGRAMMING_TRACK_ONLY");
                }
                case 0x03: {
                    return Bundle.getMessage("LN_MSG_SET_PR3_MODE_LN_MSG_SET_PR3_MODE_LOCONET_IF_WITH_TERM");
                }
                default: {
                    break;
                }
            }
        }
        return "";
    }

    private static String interpretIb2Special(LocoNetMessage l) {
        // Intellibox function control message for mobile decoder F0-F28 (IB-I) and F13-F28 (IB-II)
        if ((l.getElement(1) == LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN)
                && ((l.getElement(3) == LnConstants.RE_IB1_SPECIAL_F5_F11_TOKEN)
                || (l.getElement(3) == LnConstants.RE_IB2_SPECIAL_F13_F19_TOKEN)
                || (l.getElement(3) == LnConstants.RE_IB2_SPECIAL_F21_F27_TOKEN))) {
            // Intellibox-I function control message for mobile decoder F5 thru F27 except F12 and F20
            // Intellibox-II function control message for mobile decoder F13 thru F27 except F20
            // Note: Intellibox-II documentation implies capability to control
            // MANY more functions.  This capability may be extended by
            // additional tokens in element 3, including the special-case encoding
            // for the "eighth bit" as handled in the following case, below,
            // for F12, F20 & F28
            int funcOffset = 5 + 8 * (l.getElement(3) - LnConstants.RE_IB1_SPECIAL_F5_F11_TOKEN);
            String encodingType;
            if (l.getElement(3) == LnConstants.RE_IB1_SPECIAL_F5_F11_TOKEN) {
                encodingType = Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_HELPER_IB1");
            } else {
                encodingType = Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_HELPER_IB2");
            }
            String funcInfo[] = new String[7];
            int mask = 1;
            for (int i = 0; i < 7; i++) {
                // handle 7 bits of data
                funcInfo[i] = Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_HELPER_INDIV_FUNC",
                        funcOffset + i,
                        Bundle.getMessage(((l.getElement(4) & mask) != 0)
                                ? "LN_MSG_FUNC_ON"
                                : "LN_MSG_FUNC_OFF"));
                mask *= 2;
            }
            return Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL",
                    encodingType, l.getElement(2), funcInfo[0],
                    funcInfo[1], funcInfo[2], funcInfo[3],
                    funcInfo[4], funcInfo[5], funcInfo[6]);
        } else if ((l.getElement(1) == LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN)
                && (l.getElement(3) == LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN)) {
            // Special-case for F12, F20 and F28, since the tokens from the previous case
            // can only encode 7 bits of data in element(4).
            return Bundle.getMessage("LN_MSG_INTELLIBOX_SPECIAL_FUNC_CTL",
                    l.getElement(2),
                    Bundle.getMessage(((l.getElement(4) & LnConstants.RE_IB2_SPECIAL_F12_MASK) != 0)
                            ? "LN_MSG_FUNC_ON"
                            : "LN_MSG_FUNC_OFF"),
                    Bundle.getMessage(((l.getElement(4) & LnConstants.RE_IB2_SPECIAL_F20_MASK) != 0)
                            ? "LN_MSG_FUNC_ON"
                            : "LN_MSG_FUNC_OFF"),
                    Bundle.getMessage(((l.getElement(4) & LnConstants.RE_IB2_SPECIAL_F28_MASK) != 0)
                            ? "LN_MSG_FUNC_ON"
                            : "LN_MSG_FUNC_OFF"));
        } else if ((l.getElement(1) == LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN)
                && (l.getElement(3) == LnConstants.RE_IB1_SPECIAL_F0_F4_TOKEN)) {
            // For Intellibox-I "one" with SW version 2.x - Special-case for F0 to F4
            String funcInfo[] = new String[7];
            funcInfo[0] = Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_HELPER_INDIV_FUNC",
                    0,
                    (l.getElement(4) & LnConstants.RE_IB1_F0_MASK) == 0 ? Bundle.getMessage("LN_MSG_FUNC_ON")
                    : Bundle.getMessage("LN_MSG_FUNC_OFF"));
            int mask = 1;
            for (int i = 0; i < 4; i++) {
                // handle 7 bits of data
                funcInfo[i + 1] = Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_HELPER_INDIV_FUNC",
                        i + 1,
                        Bundle.getMessage(((l.getElement(4) & mask) != 0)
                                ? "LN_MSG_FUNC_ON"
                                : "LN_MSG_FUNC_OFF"));
                mask *= 2;
            }
            return Bundle.getMessage("LN_MSG_INTELLIBOX_FUNC_CTL_F0_TO_F4",
                    l.getElement(2),
                    funcInfo[0], funcInfo[1], funcInfo[2], funcInfo[3],
                    funcInfo[4]);
        }
        // Because the usage of other tokens in message element(3) are not yet
        // understood, let execution fall thru to the "default" case
        return "";
    }

    private static String interpretIb2F9_to_F12(LocoNetMessage l) {
        // Intellibox-II function control message for mobile decoder F9 thru F12.
        int slot = l.getElement(1);
        int funcs = l.getElement(2);
        return Bundle.getMessage("LN_MSG_INTELLIBOX_SLOT_SET_F9_TO_F12",
                slot,
                Bundle.getMessage(((funcs & LnConstants.RE_IB2_F9_MASK) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                Bundle.getMessage(((funcs & LnConstants.RE_IB2_F10_MASK) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                Bundle.getMessage(((funcs & LnConstants.RE_IB2_F11_MASK) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")),
                Bundle.getMessage(((funcs & LnConstants.RE_IB2_F12_MASK) != 0 ? "LN_MSG_FUNC_ON" : "LN_MSG_FUNC_OFF")));
    }

    /**
     * Convert bytes from LocoNet packet into a locomotive address.
     *
     * @param a1 Byte containing the upper bits.
     * @param a2 Byte containing the lower bits.
     * @return a locomotive address in the range of 0-16383
     */
    static private int LOCO_ADR(int a1, int a2) {
        return (((a1 & 0x7f) * 128) + (a2 & 0x7f));
    }

    /**
     * Convert bytes from LocoNet packet into a 1-based address for a sensor or
     * turnout.
     *
     * @param a1 Byte containing the upper bits
     * @param a2 Byte containing the lower bits
     * @return 1-4096 address
     */
    static private int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    /*
     * Take an int and convert it to a dotted version number
     * as used by the LocoIO protocol.
     * Example:  123 => 1.2.3
     */
    /**
     * Take the LocoIO version number and convert to human friendly format, like
     * "1.4.8" or "9.1".
     *
     * @param val The LocoIO version.
     * @return String with human readable format
     */
    public static String dotme(int val) {
        if ((val >= 0) && (val < 10)) {
            return Bundle.getMessage("LN_MSG_LOCOIO_HELPER_FIRMWARE_REV_DOTTED_ONE_DIGIT", val);
        } else if ((val >= 10) && (val < 100)) {
            return Bundle.getMessage("LN_MSG_LOCOIO_HELPER_FIRMWARE_REV_DOTTED_TWO_DIGITS", val / 10, val % 10);
        } else if ((val >= 100) && (val < 1000)) {
            int hundreds = val / 100;
            int tens = (val - (hundreds * 100)) / 10;
            int ones = val % 10;
            return Bundle.getMessage("LN_MSG_LOCOIO_HELPER_FIRMWARE_REV_DOTTED_THREE_DIGITS", hundreds, tens, ones);
        }
        return Bundle.getMessage("LN_MSG_LOCOIO_HELPER_FIRMWARE_REV_OUT_OF_RANGE", val);
    }

    /**
     * Convert throttle ID to a human friendly format.
     *
     * @param id1 Byte #1 of the ID
     * @param id2 Byte #2 of the ID
     * @return String with human friendly format, without the influence of
     *         Locale
     */
    private static String idString(int id1, int id2) {
        /* the decimalIdValueWithoutLocale_SpecificFormatting variable
        is used to generate a string representation of the ID value
        without any local-specific formatting.  In other words, in a
        us_EN locale, we want "14385", not "14,385".
         */
        String decimalIdValueWithoutLocale_SpecificFormatting
                = Integer.toString(((id2 & 0x7F) * 128 + (id1 & 0x7F)));

        String s = Bundle.getMessage("LN_MSG_THROTTLE_ID",
                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                        StringUtil.twoHexFromInt(id2 & 0x7F)),
                Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                        StringUtil.twoHexFromInt(id1 & 0x7F)),
                decimalIdValueWithoutLocale_SpecificFormatting);
        return s;
    }

    /**
     * Create a string representation of the loco address in
     * addressLow & addressHigh in a form appropriate for the type of address (2
     * or 4 digit) using the Digitrax 'mixed mode' if necessary.
     * <p>
     * "Mixed mode" is used by DT100 and DT200 throttles to display loco
     * addresses between 100 and 127 as a two-digit displayable value, where the
     * left digit is either 'a', 'b', or 'c', (for addresses in the 10x, 11x,
     * and 12x ranges, respectively), and the right digit is the "x" from the
     * ranges above.
     *
     * @param addressLow  the least-significant 7 bits of the loco address
     * @param addressHigh the most-significant 7 bits of the loco address
     * @return a String containing the address, using Digitrax 'mixed mode'
     *         representation of the loco address, if appropriate
     */
    private static String convertToMixed(int addressLow, int addressHigh) {
        // if we have a 2 digit decoder address, proceed accordingly
        switch (addressHigh) {
            case 0x7d:
                log.debug("addressLow / 10 = {}", addressLow / 10);
                switch (addressLow) {
                    case 100: case 101: case 102: case 103: case 104: case 105:
                    case 106: case 107: case 108: case 109:
                        // N (short, alternately 'An') (or long address NN)
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_AND_LONG_ADDRESS_Ax",
                                addressLow,
                                addressLow-100,
                                String.valueOf(LOCO_ADR(addressHigh, addressLow)));
                                // Note: .toString intentionally used here to remove the "internationalized"
                                // presentation of integers, which, in US English, adds a "," between
                                // the thousands digit and the hundreds digit.  This comma is undesired
                                // in this application.
                    case 110: case 111: case 112: case 113: case 114: case 115:
                    case 116: case 117: case 118: case 119:
                        // N (short, alternately 'Bn') (or long address NN)
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_AND_LONG_ADDRESS_Bx",
                                addressLow,
                                addressLow-110,
                                String.valueOf(LOCO_ADR(addressHigh, addressLow)));
                                // Note: .toString intentionally used here to remove the "internationalized"
                                // presentation of integers, which, in US English, adds a "," between
                                // the thousands digit and the hundreds digit.  This comma is undesired
                                // in this application.
                    case 120: case 121: case 122: case 123: case 124: case 125:
                    case 126: case 127:
                        // N (short, alternately 'Cn') (or long address NN)
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_AND_LONG_ADDRESS_Cx",
                                addressLow,
                                addressLow-120,
                                String.valueOf(LOCO_ADR(addressHigh, addressLow)));
                                // Note: .toString intentionally used here to remove the "internationalized"
                                // presentation of integers, which, in US English, adds a "," between
                                // the thousands digit and the hundreds digit.  This comma is undesired
                                // in this application.
                    default:
                        // N (short) (or long address NN)
                        return Bundle.getMessage("LN_MSG_HELPER_IS_SHORT_AND_LONG_ADDRESS",
                                addressLow,
                                String.valueOf(LOCO_ADR(addressHigh, addressLow)));
                                // Note: .toString intentionally used here to remove the "internationalized"
                                // presentation of integers, which, in US English, adds a "," between
                                // the thousands digit and the hundreds digit.  This comma is undesired
                                // in this application.
                }

            case 0x00:
            case 0x7f:
                switch (addressLow) {
                    case 100: case 101: case 102: case 103: case 104: case 105:
                    case 106: case 107: case 108: case 109:
                        // N (short, alternately 'An')
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_ADDRESS_Ax",
                                addressLow,
                                addressLow-100);
                    case 110: case 111: case 112: case 113: case 114: case 115:
                    case 116: case 117: case 118: case 119:
                        // N (short, alternately 'Bn')
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_ADDRESS_Bx",
                                addressLow,
                                addressLow-110);
                    case 120: case 121: case 122: case 123: case 124: case 125:
                    case 126: case 127:
                        // N (short, alternately 'Cn')
                        return Bundle.getMessage("LN_MSG_HELPER_IS_ALTERNATE_SHORT_ADDRESS_Cx",
                                addressLow,
                                addressLow-120);
                    default:
                        // N (short)
                        return Bundle.getMessage("LN_MSG_HELPER_IS_SHORT_ADDRESS",
                                addressLow);
                }
            default:
                // return the full 4 digit address
                return String.valueOf(LOCO_ADR(addressHigh, addressLow));
                // Note: .toString intentionally used here to remove the "internationalized"
                // presentation of integers, which, in US English, adds a "," between
                // the thousands digit and the hundreds digit.  This comma is undesired
                // in this application.
        }
    }

    private static String trackStatusByteToString(int trackStatusByte) {
        return Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STAT",
                (((trackStatusByte & LnConstants.GTRK_MLOK1) != 0)
                        ? Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_LN1_1")
                        : Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_DT200")),
                (((trackStatusByte & LnConstants.GTRK_POWER) != 0)
                        ? Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_TRK_PWR_ON")
                        : Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_TRK_PWR_OFF")),
                (((trackStatusByte & LnConstants.GTRK_IDLE) != 0)
                        ? Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_TRK_PWR_RUNNING")
                        : Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_TRK_PWR_PAUSED")),
                (((trackStatusByte & LnConstants.GTRK_PROG_BUSY) != 0)
                        ? Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_PRG_BUSY")
                        : Bundle.getMessage("LN_MSG_SLOT_HELPER_TRK_STATUS_PRG_AVAILABLE"))
        );
    }

    /**
     * Return a string which is formatted by a bundle Resource Name.
     *
     * @param hour    fast-clock hour
     * @param minute  fast-clock minute
     * @return a formatted string containing the time
     */
    private static String fcTimeToString(int hour, int minute) {
        return Bundle.getMessage("LN_MSG_SLOT_HELPER_FC_TIME",
                LocalTime.of(hour, minute).toString());
    }

    protected static String[] interpretF0_F4toStrings(int dirf) {
        String[] s = new String[5];

        s[0] = (((dirf & LnConstants.DIRF_F0) == LnConstants.DIRF_F0)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));
        s[1] = (((dirf & LnConstants.DIRF_F1) == LnConstants.DIRF_F1)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));
        s[2] = (((dirf & LnConstants.DIRF_F2) == LnConstants.DIRF_F2)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));
        s[3] = (((dirf & LnConstants.DIRF_F3) == LnConstants.DIRF_F3)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));
        s[4] = (((dirf & LnConstants.DIRF_F4) == LnConstants.DIRF_F4)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));
        return s;
    }

    protected static String directionOfTravelString(boolean isForward) {
        return Bundle.getMessage(isForward ? "LN_MSG_DIRECTION_FWD"
                : "LN_MSG_DIRECTION_REV");
    }

    protected static String[] interpretF5_F8toStrings(int snd) {
        String[] s = new String[4];

        s[0] = (((snd & LnConstants.SND_F5) == LnConstants.SND_F5)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));

        s[1] = (((snd & LnConstants.SND_F6) == LnConstants.SND_F6)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));

        s[2] = (((snd & LnConstants.SND_F7) == LnConstants.SND_F7)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));

        s[3] = (((snd & LnConstants.SND_F8) == LnConstants.SND_F8)
                ? Bundle.getMessage("LN_MSG_FUNC_ON")
                : Bundle.getMessage("LN_MSG_FUNC_OFF"));

        return s;
    }

    private static String figureAddressIncludingAliasing(int adr, int adr2, int ss2, int id1, int id2) {

        /*
         * Build loco address string. String will be a simple
         * number, unless the address is between 100 and 127
         * (inclusive), where a Digitrax "mixed mode" version
         * of the address will be appended.
         */
        String mixedAdrStr = convertToMixed(adr, adr2);

        /*
         * If the address is a command station "alias" condition,
         * then note it in the string.
         */
        if (adr2 == 0x7f) {
            if ((ss2 & LnConstants.STAT2_ALIAS_MASK) == LnConstants.STAT2_ID_IS_ALIAS) {
                /* this is an aliased address and we have the alias */
                return Bundle.getMessage("LN_MSG_LOCO_ADDR_HELPER_ALIAS_2_DIGIT_WITH_KNOWN_4_DIGIT",
                        Integer.toString(LOCO_ADR(id2, id1)), mixedAdrStr);
            } else {
                /* this is an aliased address and we don't have the alias */
                return Bundle.getMessage("LN_MSG_LOCO_ADDR_HELPER_ALIAS_2_DIGIT_WITH_UNKNOWN_4_DIGIT",
                        mixedAdrStr);
            }
        } else {
            /* a regular address which is not an alias */
            return mixedAdrStr;
        }
    }

    private static String getAlmTaskType(int taskTypeByte) {
        if (taskTypeByte == 2) {
            return Bundle.getMessage("LN_MSG_ALM_HELPER_TASK_TYPE_RD");
        } else if (taskTypeByte == 3) {
            return Bundle.getMessage("LN_MSG_ALM_HELPER_TASK_TYPE_WR");
        } else if (taskTypeByte == 0) {
            return Bundle.getMessage("LN_MSG_ALM_HELPER_TASK_TYPE_ID");
        } else {
            return Bundle.getMessage("LN_MSG_ALM_HELPER_TASK_TYPE_UNKONWN",
                    taskTypeByte);
        }
    }

    public static String getDeviceNameFromIPLInfo(int manuf, int type) {
        if (manuf != LnConstants.RE_IPL_MFR_DIGITRAX) {
            return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_UNDEFINED_MFG_PROD",
                    manuf, type);
        }
        switch (type) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_ALL:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_ALLDEVICES");
            case LnConstants.RE_IPL_DIGITRAX_HOST_LNRP:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_LNRP");
            case LnConstants.RE_IPL_DIGITRAX_HOST_UT4:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_UT4");
            case LnConstants.RE_IPL_DIGITRAX_HOST_WTL12:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_WTL12");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS210:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DCS210");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS240:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DCS240");
            case LnConstants.RE_IPL_DIGITRAX_HOST_PR3:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_PR3");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DT402:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DT402");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DT500:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DT500");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS51:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DCS51");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS52:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DCS52");
            case LnConstants.RE_IPL_DIGITRAX_HOST_UR92:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_UR92");
            case LnConstants.RE_IPL_DIGITRAX_HOST_PR4:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_PR4");
            case LnConstants.RE_IPL_DIGITRAX_HOST_LNWI:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_LNWI");
            case LnConstants.RE_IPL_DIGITRAX_HOST_BXP88:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_BXP88");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DB210:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DB210");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DB210OPTO:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DB210OPTO");
            case LnConstants.RE_IPL_DIGITRAX_HOST_DB220:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_DB220");

            default:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_HOST_UNKNOWN", type);
        }
    }

    public static String getSlaveNameFromIPLInfo(int manuf, int slaveNum) {
        if (manuf != LnConstants.RE_IPL_MFR_DIGITRAX) {
            return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_UNDEFINED_MFG_PROD",
                    manuf, slaveNum);
        }
        switch (slaveNum) {
            case LnConstants.RE_IPL_DIGITRAX_SLAVE_ALL:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_SLAVE_ALLDEVICES");
            case LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_SLAVE_RF24");
            default:
                return Bundle.getMessage("LN_MSG_IPL_DEVICE_HELPER_DIGITRAX_SLAVE_UNKNOWN", slaveNum);
        }
    }

    /**
     * Interpret messages with Opcode of OPC_ALM_READ, OPC_ALM_WRITE
     *
     * @param l LocoNet Message to interpret
     * @return String containing interpreted message or empty string if
     *      message is not interpretable.
     */
    public static String interpretAlm(LocoNetMessage l) {
        if ((l.getOpCode() != LnConstants.OPC_ALM_READ) &&
                (l.getOpCode() != LnConstants.OPC_ALM_WRITE)) {
            return "";
        }
        if (l.getElement(1) == 0x10) {
            switch (l.getElement(2)) {
                case 0:
                    if ((l.getElement(3) == 0)
                            && (l.getElement(6) == 0)) {
                        return Bundle.getMessage("LN_MSG_QUERY_ALIAS_INFO");
                    } else if ((l.getElement(3) == 0)
                            && (l.getElement(6) == 0x0b)) {
                        return Bundle.getMessage("LN_MSG_ALIAS_INFO_REPORT", l.getElement(4) * 2);
                    } else if ((l.getElement(6) == 0xf)
                            && (l.getElement(14) == 0)) {
                        // Alias read and write messages
                        String message;
                        if (l.getElement(3) == 0x2) {
                            if (l.getOpCode() == LnConstants.OPC_ALM_WRITE) {
                                return Bundle.getMessage("LN_MSG_QUERY_ALIAS", l.getElement(4));
                            } else {
                                message = "LN_MSG_REPORT_ALIAS_2_ALIASES";
                            }
                        } else {
                            break;
                        }
                        String longAddr = convertToMixed(l.getElement(7), l.getElement(8));
                        int shortAddr = l.getElement(9);
                        String longAddr2 = convertToMixed(l.getElement(11), l.getElement(12));
                        int shortAddr2 = l.getElement(13);
                        int pair = l.getElement(4);

                        return Bundle.getMessage(message, pair,
                                longAddr, shortAddr, longAddr2, shortAddr2);
                    } else if ((l.getElement(3) == 0x43)) {
                        String longAddr = convertToMixed(l.getElement(7), l.getElement(8));
                        int shortAddr = l.getElement(9);
                        String longAddr2 = convertToMixed(l.getElement(11), l.getElement(12));
                        int shortAddr2 = l.getElement(13);
                        int pair = l.getElement(4);
                        return Bundle.getMessage("LN_MSG_SET_ALIAS_2_ALIASES",
                                pair, longAddr, shortAddr, longAddr2, shortAddr2);
                    } else if ((l.getElement(6) == 0)
                            && (l.getElement(14) == 0)) {
                        return Bundle.getMessage("LN_MSG_QUERY_ALIAS", l.getElement(4));
                    }
                    break;
                case 1:
                    if ((l.getElement(2) == 1) &&
                            ((l.getElement(3) & 0x7E) == 0x2)
                            // ((l.getElement(6) & 0x7E) == 0x2) &&  // sometimes 0x00, sometimes 0x0F, not sure why
                            ) {
                            // appears to be related to command-station routes
                            int turnoutGroup;
                            int altTurnoutGroup;
                            int routeNum;
                            int altRouteNum;
                            routeNum = 1 + (((l.getElement(4) + l.getElement(5)*128)/2) & 0x1f);
                            turnoutGroup = 1 + ((l.getElement(4) & 0x1)<< 2);
                            altRouteNum = 1 + (((l.getElement(4) + l.getElement(5)*128)/4) & 0x3F);
                            altTurnoutGroup = 1 + ((l.getElement(4) & 0x3) << 2);
                            if ((l.getOpCode() == LnConstants.OPC_ALM_WRITE) &&
                                    ((l.getElement(3) & 0x1) == 0)) {
                                return Bundle.getMessage("LN_MSG_CMD_STN_ROUTE_QUERY",
                                        routeNum,
                                        turnoutGroup, turnoutGroup + 3,
                                        altRouteNum,
                                        altTurnoutGroup, altTurnoutGroup + 3);
                            }
                            String turnA, turnB, turnC, turnD;
                            String statA, statB, statC, statD;
                            if ((l.getElement(7) == 0x7f) && (l.getElement(8) == 0x7f)) {
                                turnA = "Unused";
                                statA = "";
                            } else {
                                turnA = Integer.toString(1 + l.getElement(7) + ((l.getElement(8) & 0x0f) << 7));
                                statA = (l.getElement(8) & 0x20) == 0x20 ?"c":"t";
                            }

                            if ((l.getElement(9) == 0x7f) && (l.getElement(10) == 0x7f)) {
                                turnB = "Unused";
                                statB = "";
                            } else {
                                turnB = Integer.toString(1 + l.getElement(9) + ((l.getElement(10) & 0x0f) << 7));
                                statB = (l.getElement(10) & 0x20) == 0x20 ?"c":"t";
                            }

                            if ((l.getElement(11) == 0x7f) && (l.getElement(12) == 0x7f)) {
                                turnC = "Unused";
                                statC = "";
                            } else {
                                turnC = Integer.toString(1 + l.getElement(11) + ((l.getElement(12) & 0x0f) << 7));
                                statC = (l.getElement(12) & 0x20) == 0x20 ?"c":"t";
                            }

                            if ((l.getElement(13) == 0x7f) && (l.getElement(14) == 0x7f)) {
                                turnD = "Unused";
                                statD = "";
                            } else {
                                turnD = Integer.toString(1 + l.getElement(13) + ((l.getElement(14) & 0x0f) << 7));
                                statD = (l.getElement(14) & 0x20) == 0x20 ?"c":"t";
                            }

                            return Bundle.getMessage((l.getOpCode() ==
                                        LnConstants.OPC_ALM_WRITE)?
                                            "LN_MSG_CMD_STN_ROUTE_WRITE":
                                            "LN_MSG_CMD_STN_ROUTE_REPORT",
                                    routeNum ,
                                    turnoutGroup, turnoutGroup+3,
                                    altRouteNum, altTurnoutGroup, altTurnoutGroup+3,
                                    turnA, statA, turnB, statB,
                                    turnC, statC, turnD, statD);

                    }

                    return "routes unknown\n";
                default:
                    return Bundle.getMessage(
                            ((l.getOpCode() == LnConstants.OPC_ALM_WRITE)
                            ? "LN_MSG_ALM_WRITE"
                            : "LN_MSG_ALM_WRITE_REPLY"),
                            l.getElement(2),
                            l.getElement(3),
                            getAlmTaskType(l.getElement(3)),
                            l.getElement(4),
                            l.getElement(5),
                            l.getElement(6),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(7))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(8))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(9))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(10))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(11))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(12))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(13))),
                            Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                    StringUtil.twoHexFromInt(l.getElement(14))));
            }
        } else if (l.getElement(1) == 0x15) {
            int slot = ( (l.getElement(2) & 0x07 ) *128) + l.getElement(3); // slot number for this request

            String result = interpretExtendedSlotRdWr(l, slot) ;
            if (result.length() > 0) {
                return result;
            }
        }
        return "";

    }
    private static String interpretOpcExpMoveSlots(LocoNetMessage l) {
        int src = ((l.getElement(1) & 0x03) * 128) + (l.getElement(2) & 0x7f);
        int dest = ((l.getElement(3) & 0x03) * 128) + (l.getElement(4) & 0x7f);

        if ((src >= 0x79) && (src <= 0x7f)) {
            return "";
        }
        if ((dest >= 0x79) && (dest <= 0x7f)) {
            return "";
        }

        boolean isSettingStatus = ((l.getElement(3) & 0b01110000) == 0b01100000);
        if (isSettingStatus) {
            int stat = l.getElement(4);
            return Bundle.getMessage("LN_MSG_OPC_EXP_SET_STATUS",
                    src,
                    LnConstants.CONSIST_STAT(stat),
                    LnConstants.LOCO_STAT(stat),
                    LnConstants.DEC_MODE(stat));
        }
        boolean isUnconsisting = ((l.getElement(3) & 0b01110000) == 0b01010000);
        if (isUnconsisting) {
            // source and dest same, returns slot contents
            return Bundle.getMessage("LN_MSG_OPC_EXP_UNCONSISTING",
                    src);
        }
        boolean isConsisting = ((l.getElement(3) & 0b01110000) == 0b01000000);
        if (isConsisting) {
            //add dest to src, returns dest slot contents
            return Bundle.getMessage("LN_MSG_OPC_EXP_CONSISTING",
                    src,dest);
        }
       /* check special cases */
        if (src == 0) {
            /* DISPATCH GET */
            // maybe
            return Bundle.getMessage("LN_MSG_MOVE_SL_GET_DISP");
        } else if (src == dest) {
            /* IN USE */
            // correct
            return Bundle.getMessage("LN_MSG_MOVE_SL_NULL_MOVE", src);
        } else if (dest == 0) {
            /* DISPATCH PUT */

            return Bundle.getMessage("LN_MSG_MOVE_SL_DISPATCH_PUT", src);
        } else {
            /* general move */

            return Bundle.getMessage("LN_MSG_MOVE_SL_MOVE", src, dest);
        }
    }

    private static String interpretPocExpLocoSpdDirFunction(LocoNetMessage l) {
        int slot = ((l.getElement(1) & 0x03) * 128) + (l.getElement(2) & 0x7f);
        if ((l.getElement(1) & LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_SPEED) == 0) {
            // speed and direction
            int spd = l.getElement(4);
            String direction = Bundle.getMessage((l.getElement(1) & 0b00001000) != 0
                    ? "LN_MSG_DIRECTION_REV" : "LN_MSG_DIRECTION_FWD");
            String throttleID = Integer.toHexString(l.getElement(3));
            return Bundle.getMessage("LN_MSG_OPC_EXP_SPEED_DIRECTION", slot, spd, direction, throttleID);
        }
        // Build a string for the functions on off
        String[] fn = new String[8];
        for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
            fn[bitIndex] = (l.getElement(4) >> (7 - bitIndex) & 1) == 1 ? Bundle.getMessage("LN_MSG_FUNC_ON")
                    : Bundle.getMessage("LN_MSG_FUNC_OFF");
        }
        if ((l.getElement(1) &
                LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION) == LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6_MASK) {
            return Bundle.getMessage("LN_MSG_OPC_EXP_FUNCTIONS_F0_F6", slot, fn[3], fn[7], fn[6], fn[5], fn[4], fn[2],
                    fn[1]);
        } else if ((l.getElement(1) &
                LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION) == LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13_MASK) {
            return Bundle.getMessage("LN_MSG_OPC_EXP_FUNCTIONS_F7_F13", slot, fn[7], fn[6], fn[5], fn[4], fn[3], fn[2],
                    fn[1]);
        } else if ((l.getElement(1) &
                LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION) == LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20_MASK) {
            return Bundle.getMessage("LN_MSG_OPC_EXP_FUNCTIONS_F14_F20",slot, fn[7], fn[6], fn[5], fn[4], fn[3], fn[2],
                    fn[1]);
        } else if ((l.getElement(1) &
                LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION) == LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF_MASK) {
            return Bundle.getMessage("LN_MSG_OPC_EXP_FUNCTIONS_F21_F28",slot, fn[7], fn[6], fn[5], fn[4], fn[3], fn[2],
                    fn[1], Bundle.getMessage("LN_MSG_FUNC_OFF"));
        } else if ((l.getElement(1) &
                LnConstants.OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION) == LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON_MASK) {
            return Bundle.getMessage("LN_MSG_OPC_EXP_FUNCTIONS_F21_F28", slot, fn[7], fn[6], fn[5], fn[4], fn[3], fn[2],
                    fn[1], Bundle.getMessage("LN_MSG_FUNC_ON"));
        }
        return "";
    }

    private static String interpretExtendedSlotRdWr(LocoNetMessage l, int slot) {
        /**
         * ************************************************
         * extended slot read/write message               *
         * ************************************************
         */
        /*
         * If its a "Special" slot (Stats etc) use a different routine
         */
        if (slot > 247 && slot < 252) {
            return interpretExtendedSlot_StatusData(l,slot);
        }
        int trackStatus = l.getElement(7); // track status
        int id1 =  l.getElement(19);
        int id2 = l.getElement(18);
        int command = l.getOpCode();
        int stat = l.getElement(4); // slot status
        //int adr = l.getElement(5) + 128 * l.getElement(6); // loco address
        int adr = l.getElement(5);
        int spd = l.getElement(8); // command speed
        int dirf = l.getElement(10) & 0b00111111; // direction and F0-F4 bits
        String[] dirf0_4 = interpretF0_F4toStrings(dirf);
        int ss2 = l.getElement(18); // slot status 2 (tells how to use
        // ID1/ID2 & ADV Consist)
        int adr2 = l.getElement(6); // loco address high
        int snd = l.getElement(10); // Sound 1-4 / F5-F8
        String[] sndf5_8 = interpretF5_F8toStrings(snd);

        String locoAdrStr = figureAddressIncludingAliasing(adr, adr2, ss2, id1, id2);
        return Bundle.getMessage(((command == 0xEE)
                ? "LN_MSG_SLOT_LOCO_INFO_WRITE"
                : "LN_MSG_SLOT_LOCO_INFO_READ"),
                slot,
                locoAdrStr,
                LnConstants.CONSIST_STAT(stat),
                LnConstants.LOCO_STAT(stat),
                LnConstants.DEC_MODE(stat),
                directionOfTravelString((dirf & LnConstants.DIRF_DIR) == 0),
                spd, // needs re-interpretation for some cases of slot consisting state
                dirf0_4[0],
                dirf0_4[1],
                dirf0_4[2],
                dirf0_4[3],
                dirf0_4[4],
                sndf5_8[0],
                sndf5_8[1],
                sndf5_8[2],
                sndf5_8[3],
                trackStatusByteToString(trackStatus),
                Bundle.getMessage("LN_MSG_SLOT_HELPER_SS2_SIMPLE",
                        Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION",
                                StringUtil.twoHexFromInt(ss2))),
                Bundle.getMessage("LN_MSG_SLOT_HELPER_ID1_ID2_AS_THROTTLE_ID",
                        idString(id1, id2)));
    }

    private static String interpretExtendedSlot_StatusData(LocoNetMessage l, int slot) {
       String baseInfo = "";
       String detailInfo = "";
       switch (slot) {
           case 248:
                // Identifying information
                baseInfo = interpretExtendedSlot_StatusData_Base_Detail(l,slot);
                // Flags
                detailInfo = interpretExtendedSlot_StatusData_Flags(l,slot);
                break;
           case 249:
                // electric
                // Identifying information
                baseInfo = interpretExtendedSlot_StatusData_Base(l,slot);
                detailInfo = interpretExtendedSlot_StatusData_Electric(l,slot);
                break;
            case 251:
                // LocoNet stats
                // Identifying information
                baseInfo = interpretExtendedSlot_StatusData_Base(l,slot);
                detailInfo = interpretExtendedSlot_StatusData_LocoNet(l,slot);
                break;
            case 250:
                // Identifying information
                baseInfo = interpretExtendedSlot_StatusData_Base(l,slot);
                // Slots info
                detailInfo = interpretExtendedSlot_StatusData_Slots(l,slot);
                break;
            default:
                baseInfo = "Still working on it";
        }
       return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS",
               slot, baseInfo, detailInfo);
    }

    /**
     * Interpret the base information in bytes 16,18,19
     * for slots 249,250,251. not 248
     * @param l loconetmessage
     * @param slot slot number
     * @return a format message.
     */
    private static String interpretExtendedSlot_StatusData_Base(LocoNetMessage l, int slot) {
        String hwType = "";
        int hwSerial;
        switch (l.getElement(16)) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS240:
                hwType = "DCS240";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS210:
                hwType = "DCS210";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS52:
                hwType = "DCS52";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_BXP88:
                hwType = "BXP88";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_BXPA1:
                hwType = "BXPA1";
                break;
            default:
                hwType = "Unknown";
        }
        hwSerial = ((l.getElement(19) & 0x0f) * 128 ) + l.getElement(18);
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_BASE",
                hwType,
                hwSerial);
    }

    /**
     * Interp slot 248 base details
     * @param l loconetmessage
     * @param slot slot number
     * @return formated message
     */
    private static String interpretExtendedSlot_StatusData_Base_Detail(LocoNetMessage l, int slot) {
        double hwVersion ;
        double swVersion ;
        int hwSerial;
        String hwType;
        switch (l.getElement(14)) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS240:
                hwType = "DCS240";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS210:
                hwType = "DCS210";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_BXP88:
                hwType = "BXP88";
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_BXPA1:
                hwType = "BXPA1";
                break;
            default:
                hwType = "Unknown";
        }
        hwSerial = ((l.getElement(19) & 0x0f) * 128 ) + l.getElement(18);
        hwVersion = ((double)(l.getElement(17) & 0x78) / 8 ) + ((double)(l.getElement(17) & 0x07) / 10 ) ;
        swVersion = ((double)(l.getElement(16) & 0x78) / 8 ) + ((double)(l.getElement(16) & 0x07) / 10 ) ;
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_BASEDETAIL",
                hwType,
                hwSerial,
                hwVersion,
                swVersion);
    }

    /**
     * Interp slot 249 electric stuff, bytes 4,5,6,7,10,12
     * @param l loconetmessage
     * @param slot slot number
     * @return formated message
     */
    private static String interpretExtendedSlot_StatusData_Electric(LocoNetMessage l, int slot) {
        double voltsTrack;
        double voltsIn;
        double ampsIn;
        double ampsLimit;
        double  voltsRsLoaded;
        double  voltsRsUnLoaded;
        voltsTrack = ((double)l.getElement(4)) * 2 / 10 ;
        voltsIn = ((double)l.getElement(5)) * 2 / 10;
        ampsIn = ((double)l.getElement(6)) / 10;
        ampsLimit = ((double)l.getElement(7)) / 10;
        voltsRsLoaded = ((double)l.getElement(12)) * 2 / 10;
        voltsRsUnLoaded = ((double)l.getElement(10)) * 2 / 10;
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_ELECTRIC",
                voltsTrack,
                voltsIn,
                ampsIn,
                ampsLimit,
                voltsRsLoaded,
                voltsRsUnLoaded);
    }

    /**
     * Interp slot 249 loconet stats, bytes 4 & 5,6 & 7
     * @param l loconetmessage
     * @param slot slot number
     * @return formated message
     */
    private static String interpretExtendedSlot_StatusData_LocoNet(LocoNetMessage l, int slot) {
        double msgTotal;
        double msgErrors;
        msgTotal = (l.getElement(4) + ( l.getElement(5) * 128)) ;
        msgErrors = (l.getElement(6) + ( l.getElement(7) * 128)) ;
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_LOCONET",
                msgTotal,
                msgErrors);
    }


    private static String interpretExtendedSlot_StatusData_Flags(LocoNetMessage l, int slot) {
        //TODO need more sample data
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_FLAGS");
    }

    /**
     * Interp slot 250 slots used/free etc
     * @param l loconetmessage
     * @param slot slot number
     * @return formated message
     */
    private static String interpretExtendedSlot_StatusData_Slots(LocoNetMessage l, int slot) {
        //TODO there is still more data in this slot.
        double msgInUse;
        double msgIdle;
        double msgFree;
        msgInUse = (l.getElement(4) + ( l.getElement(5) * 128)) ;
        msgIdle = (l.getElement(6) + ( l.getElement(7) * 128)) ;
        msgFree = (l.getElement(8) + ( l.getElement(9) * 128)) ;
        return Bundle.getMessage("LN_MSG_OPC_EXP_SPECIALSTATUS_SLOTS",
                msgInUse,
                msgIdle,
                msgFree);
    }

    private static final String ds54sensors[] = {"AuxA", "SwiA", "AuxB", "SwiB", "AuxC", "SwiC", "AuxD", "SwiD"};    // NOI18N
    private static final String ds64sensors[] = {"A1", "S1", "A2", "S2", "A3", "S3", "A4", "S4"};                    // NOI18N
    private static final String se8csensors[] = {"DS01", "DS02", "DS03", "DS04", "DS05", "DS06", "DS07", "DS08"};    // NOI18N

    private final static Logger log = LoggerFactory.getLogger(LocoNetMessageInterpret.class);


}
