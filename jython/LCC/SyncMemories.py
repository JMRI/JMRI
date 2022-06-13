# This script synchronizes the text value of a Memory Variable or a Block Value
# between different JMRI computers on the same LCC / OpenLCB network.
#
# When one JMRI computer changes the respective Memory Variable or Block Value,
# then it notifies the other JMRI computers of this change, and the new value
# is copied over to those other computers using the LCC network. No other
# connection is necessary between the JMRI machines (i.e., no computer network
# or special cleint / server setup), it is sufficient that they all have
# interfaces to the same LCC network.
#
# If desired, a uni-directional synchronization can also be set up, where a
# JMRI is set up as source of truth, and changes only get copied from this
# machine to the others, but the source of truth never changes from the
# network. This can be helpful for remote displays where the logic is
# centralized.
#
# Author: Balazs Racz, copyright 2022
# Part of the JMRI distribution
#
# How to use:
# ^^^^^^^^^^^
#
# 1. Make sure you have an LCC or OpenLCB connection configured on all
# participating computers.
#
# 2. Make sure that your JMRI XML file is loaded. You should be able to see the
# Blocks and Memory Variables in the respective tables. If you want to use
# auto-load upon startup, make sure that loading the XML is first in the
# automatic startup actions.
#
# 3. Load this jython script
#
# 4. For each memory variable or block that you want to export, you need to
# execute one line of python with the export command. This one line can be
# added either to the bottom of this script, or added to a second script that
# is loaded afterwards. See example below on what to put there.
#
# That's it! From now on whenever one computer changes the value of the
# block/variable, all others will also change that variable to the same value.
#
#
# Requirements and theory of operation:
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#
# For each memory variable to synchronize between JMRI computers you will
# assign a specific LCC Event ID. You have to give this Event ID to the export
# command with the name of the memory variable / block. Both user name and
# system name is OK. The name has to match between the different machines, and
# the event ID has to match between the different machines.
#
# When a machine sees a local change to the Memory Variable's value, it will
# produce an Event Report with the given Event ID to the LCC bus. This Event
# Report message also carries the machine's LCC identifier (Node ID).
#
# A second machine, upon seeing the given Event ID message, will reach out to
# the first machine using a datagram message to fetch the new value of the
# Memory Variable. The first machine will respond with the new value in a
# response datagram. The second machine then updates its local table with the
# new value.
#
# It is possible to have more than two machines participating, in this case
# each machine will copy the last announced change from whichever machine it
# happened on (and produced the Event). There will be two datagrams for each
# machine that needs to copy the value.
#
# Only string type values are supported. Anything else will be converted to
# string first, then transmitted as that string. The remote computer will then
# not have the original object type anymore, but a String instead. Unicode
# strings are handled correctly and transmitted using UTF-8 encoding.
#
# The maximum length of the value supported is about 68 bytes, which is split
# between the name of the memory variable and the value that is transmitted. So
# for example for the memory variable "IM:AUTO:0001", which is 12 bytes long,
# the maximum length of the value supported is 56 bytes.
#
#
# How to set the Event IDs:
# ^^^^^^^^^^^^^^^^^^^^^^^^^
#
# Easiest is to request a block of Event IDs from the website
# https://registry.openlcb.org/requestuniqueidrange This will give you 256 *
# 256 * 256 (16 million) Event IDs, so that will last a while. You will get the
# first 5 bytes specified, and can set the last three as you wish.
#
# You can use Event IDs from any LCC device you have. That's usually 65
# thousand for each device you purchased. Make sure not to pick those Event IDs
# that the device itself is using. For example if you have a device with a Node
# ID of 02.01.57.00.00.37, then you could probably use Event IDs like
# 02.01.57.00.00.37.FF.00 to 02.01.57.00.00.37.FF.99 and then count downwards
# from there if more are needed.
#
#
# Examples:
# ^^^^^^^^^
#
# Start upon load:
# - In JMRI PanelPro preferences, select Start Up.
#
# - click Add, then Open File, and select the XML file that contains your
#   turnouts, memory variables, blocks, panels. Make sure this entry is on the
#   top.
#
# - click Add, select Run Script, and pick this script file. This should be in
#   the middle, under the XML.
#
# - click Add, select Run Script, and pick the file that contains your export
#   commands. This should be at the bottom.
#
#
# ExportScript.py (remove one # from the beginning of each line):
#
# #Example for exporting a memory variable in producer-consumer mode.
# lccSyncService.addMemVariablePC("IM:AUTO:0001", "02.01.57.00.00.37.FF.55")
# #Example for exporting a block value in consumer-only mode.
# lccSyncService.addBlockValueC("IBMYBLOCK", "02.01.57.00.00.37.FF.33")
#
# All commands:
#
# - addMemVariablePC
# - addMemVariableP
# - addMemVariableC
# - addBlockValuePC
# - addBlockValueP
# - addBlockValueC
#
# PC means export-import
# P means export only
# C means import only

import java
import jmri
import org
import array
    
class KVDatagramService:
    # Datagram type (first byte) of a datagram to the KV service. This is the
    # client -> server direction.
    REQUEST_TYPE = 0x22
    # Datagram type (first byte) of a datagram returned from the KV
    # service. This is the server -> client direction.
    RESPONSE_TYPE = 0x23

    # Command byte in request datagram to fetch the value for a key. The next
    # byte is a length byte, then as many bytes to describe the key.
    REQUEST_GET_ENTRY = 0x0
    # Command byte in response datagram describing a key-value pair. Two
    # pascal-strings follow, each with a leading length byte then as many
    # payload bytes as indicated.
    RESPONSE_GET_ENTRY_SUCCESS = 0x1
    # Command byte in response datagram describing an error in lookup. Next two
    # bytes are the error code. Then a pascal-string for the key.
    RESPONSE_GET_ENTRY_FAIL = 0x41

    # Incoming datagram is too short to be processed
    ERROR_TOO_SHORT = 0x1081
    # Unknown subcommand in datagram (unimplemented subcmd)
    ERROR_UNKNOWN_CMD = 0x1041
    # Error message when a key was not found. This does not get packaged into a
    # datagram rejected, but in the response datagram RESPONSE_GET_ENTRY_FAIL.
    ERROR_KEY_NOT_FOUND = 0x1030  
    
    def __init__(self):
        self._entries = {}
        olcbConfigMgr = jmri.InstanceManager.getDefault().getInstance(jmri.jmrix.openlcb.OlcbConfigurationManager)
        print(olcbConfigMgr)
        olcbInterface = olcbConfigMgr.get(org.openlcb.OlcbInterface)
        self._if = olcbInterface
        print(olcbInterface)
        ourNode = olcbConfigMgr.get(org.openlcb.NodeID)
        print(ourNode)
        self._nodeId = ourNode
        self._dcs = olcbInterface.getDatagramService()
        print(self._dcs)
        self._srvHandler = self.DatagramHandler(self.REQUEST_TYPE, self._onServerDatagram)
        self._dcs.registerForReceive(self._srvHandler)

    # Adds an entry to the KV store.
    #
    # @param key object describing the key (string, unicode or list of integers
    # representing bytes)
    #
    # @param value object describing the value to export (string, unicode or
    # list of integers representing bytes)
    def addOrUpdateEntry(self, key, value):
        nk = KVDatagramService._normalizeKey(key)
        self._entries[nk] = KVDatagramService._valueToList(value)

    # Removes an entry from the KV store.
    #
    # @param key object describing the key (string, unicode or list of integers
    # representing bytes)
    def deleteEntry(self, key):
        nk = KVDatagramService._normalizeKey(key)
        del self._entries[nk]

    # Verifies an entry in the KV store.
    #
    # @param key object describing the key (string, unicode or list of integers
    # representing bytes)
    #
    # @param value object describing the value to export (string, unicode or
    # list of integers representing bytes)
    #
    # @return True if the key exists and the current value is as specified,
    # false otherwise.
    def checkEntry(self, key, value):
        nk = KVDatagramService._normalizeKey(key)
        return self._entries[nk] == KVDatagramService._valueToList(value)

    # Takes an input object and represents it as a key. Handles raw strings,
    # unicode strings and lists of integers as well.
    # returns an encoded key.
    @staticmethod
    def _normalizeKey(key):
        intlist = KVDatagramService._valueToList(key)
        return KVDatagramService._encodeKey(intlist)
        
    # Takes a list of integers, and generates an encoding of that key to use in
    # the dictionary.
    @staticmethod
    def _encodeKey(key):
        return str(bytearray(key))

    # Takes an input object and represents it as a list of bytes. Handles raw
    # strings, unicode strings and lists of integers as well.
    @staticmethod
    def _valueToList(value):
        intlist = []
        if value is None:
            intlist = []
        elif isinstance(value, array.array):
            intlist = value.tolist()
        elif isinstance(value, unicode):
            intlist = KVDatagramService._uStringToList(value)
        elif isinstance(value, str):
            intlist = KVDatagramService._rawStringToList(value)
        elif isinstance(value, list):
            intlist = value
        else:
            print("Warning: in KVDatagramService.valueToList: Not sure how to handle jython object " + str(type(value)))
            intlist = KVDatagramService._uStringToList(str(value))
        return intlist
    
    # Converts a unicode string to a list of byte values encoded according to
    # UTF-8. The input should be a u-string, e.g. u"aaaűűű"
    @staticmethod
    def _uStringToList(unicodeString):
        return [ord(x) for x in unicodeString.encode('UTF-8')]        

    # Converts a string to a list of byte values with native encoding. The
    # input should be a string that's already encoded to UTF-8 characters,
    # i.e. either coming from a system that inputs UTF-8, or has been converted
    # using s.encode('UTF-8').
    @staticmethod
    def _rawStringToList(rawString):
        return [ord(x) for x in rawString]        

    # Decodes a list of bytes into a unicode string.
    #
    # @param byteList a list of bytes of UTF-8 encoded string (e.g. produced by
    # _ustringToList)
    #
    # @return a unicode string
    @staticmethod
    def _listToUString(byteList):
        return bytearray(byteList).decode('UTF-8')
    
    # Converts a 16-byte integer value to a 2-element int array of bytes in
    # network-endian order.
    @staticmethod
    def _errorToArray(code):
        return [(code >> 8) & 0xFF, code & 0xFF]

    # Callback invoked when a server datagram arrives.
    #
    # @param remoteNode a NodeID object for the sender of the query
    #
    # @param payload a java array of integers containing the request datagram
    # payload
    #
    # @param reply a handler object from DatagramService to acknowledge the
    # datagram.
    def _onServerDatagram(self, remoteNode, payload, reply):
        print("server handler")
        print(remoteNode)
        print(payload)
        print(payload[1:3].tolist())
        if len(payload) < 2:
            reply.acceptData(self.ERROR_TOO_SHORT)
            return
        if payload[1] == self.REQUEST_GET_ENTRY:
            return self._serverGetEntry(remoteNode, payload, reply)
        print("unknown subcmd")
        reply.acceptData(self.ERROR_UNKNOWN_CMD)

    # Handles a "get KV pair" server request.
    #
    # @param remoteNode a NodeID object for the sender of the query
    #
    # @param payload a java array of integers containing the request datagram
    # payload
    #
    # @param reply a handler object from DatagramService to acknowledge the
    # datagram.
    def _serverGetEntry(self, remoteNode, payload, reply):
        if len(payload) < 3:
            reply.acceptData(self.ERROR_TOO_SHORT)
            return
        length = payload[2]
        if len(payload) < 3 + length:
            reply.acceptData(self.ERROR_TOO_SHORT)
            return
        print("normalizing key")
        keyArray = payload[3:3+length]
        keyList = keyArray.tolist()
        key = KVDatagramService._encodeKey(keyList)
        print("checking key " + key)
        if key not in self._entries:
            print("computing notfound")
            resp = [self.RESPONSE_TYPE, self.RESPONSE_GET_ENTRY_FAIL] + KVDatagramService._errorToArray(self.ERROR_KEY_NOT_FOUND) + [len(keyList)] + keyList
            print("Request key " + str(keyList) + " not found.")
            reply.acceptData(org.openlcb.implementations.DatagramService.ACCEPT_REPLY_PENDING)           
            self._dcs.sendData(remoteNode, resp)
            return
        print("retrieving value")
        val = self._entries[key]
        print("computing resp")
        resp = [self.RESPONSE_TYPE, self.RESPONSE_GET_ENTRY_SUCCESS, len(keyList)] + keyList + [len(val)] + val
        print("Request key " + str(keyList) + " response " + str(val))
        reply.acceptData(org.openlcb.implementations.DatagramService.ACCEPT_REPLY_PENDING)
        self._dcs.sendData(remoteNode, resp)
        return 
        
    class DatagramHandler(org.openlcb.implementations.DatagramService.DatagramServiceReceiveMemo):
        def __init__(self, datagram_type, cb):
            org.openlcb.implementations.DatagramService.DatagramServiceReceiveMemo.__init__(self, datagram_type)
            self._cb = cb

        def handleData(self, remoteNode, payload, reply):
            print("handleData")
            self._cb(remoteNode, payload, reply)
            


class LCCSyncService:
    def __init__(self):
        self._md = {}
        self._kv = KVDatagramService()
        self._evHandler = LCCSyncService.EventDispatcher(self)
        self._kv._if.registerMessageListener(self._evHandler)
        self._respDgHandler = KVDatagramService.DatagramHandler(KVDatagramService.RESPONSE_TYPE, self._handleResponseDatagram)
        self._kv._dcs.registerForReceive(self._respDgHandler)
        

    # Helper function to emit a PCER message.
    #
    # @param eventId an openlcb.EventID object
    def _produceEvent(self, eventId):
        msg = org.openlcb.ProducerConsumerEventReportMessage(self._kv._if.getNodeId(), eventId)
        self._kv._if.getOutputConnection().put(msg, None)

    # Emits producer and consumer identified messages for a given event ID.
    def _emitIdentified(self, eventId):
        msg = org.openlcb.ProducerIdentifiedMessage(self._kv._if.getNodeId(), eventId, org.openlcb.EventState.Unknown)
        self._kv._if.getOutputConnection().put(msg, None)
        msg = org.openlcb.ConsumerIdentifiedMessage(self._kv._if.getNodeId(), eventId, org.openlcb.EventState.Unknown)
        self._kv._if.getOutputConnection().put(msg, None)
        
    def _handleResponseDatagram(self, remoteNode, payload, reply):
        print("client handler")
        if len(payload) < 2:
            reply.acceptData(self.ERROR_TOO_SHORT)
            return
        if payload[1] == KVDatagramService.RESPONSE_GET_ENTRY_FAIL:
            print("Failed to fetch KV entry from remote node " + str(remoteNode) + " for " + payload[6:])
            reply.acceptData(0)
            return
        if payload[1] != KVDatagramService.RESPONSE_GET_ENTRY_SUCCESS:
            reply.acceptData(KVDatagramService.ERROR_UNKNOWN_CMD)
            return
        # now: we have a get entry success response
        koffs = 2
        if len(payload) <= koffs:
            reply.acceptData(KVDatagramService.ERROR_TOO_SHORT)
            return
        klen = payload[koffs]
        voffs = koffs + 1 + klen
        if len(payload) <= voffs:
            reply.acceptData(KVDatagramService.ERROR_TOO_SHORT)
            return
        vlen = payload[voffs]
        if len(payload) <= voffs + vlen:
            reply.acceptData(KVDatagramService.ERROR_TOO_SHORT)
            return
        key = payload[koffs + 1:koffs + 1 + klen]
        value = payload[voffs + 1:voffs + 1 + vlen]
        print("response key = " + str(key) + " value = " + str(value))
        reply.acceptData(0)
        nkey = KVDatagramService._normalizeKey(key)
        if nkey not in self._md:
            print("unknown response key: " + str(nkey))
            return
        entry = self._md[nkey]
        dispval = KVDatagramService._listToUString(value.tolist())
        if entry._getter() != dispval:
            self._kv.addOrUpdateEntry(key, value)
            entry._setter(dispval)
        
    # We have one object of this class for each object that we export/import to
    # LCC. The structure contains all the per-entry fields, and also
    # acts as targets to callbacks.
    class ExportedEntry:
        def __init__(self, parent, key, eventId, bean, propName, getter, setter):
            """Constructor.

            @param parent the LCCSyncService owning this ExportedEntry

            @param key a string describing the key to export this entry
            under. This will be used in the KV service and remote datagrams.

            @param eventId an openlcb EventID which to generate and listen for
            upon update

            @param bean the JMRI NamedBean object

            @param propName a string representing which property we are
            exporting.

            @param getter a callback on the bean that returns the current value
            of the property. Takes no arguments, returns an object (preferably
            a string/unicode). Example: bean.getValue

            @param setter a callback on the bean that sets the desired value of
            the property. Takes one argument, which will be filled with a
            unicode string. Example: bean.setValue

            """
            self._parent = parent
            self._key = key
            self._eventId = eventId
            self._bean = bean
            self._propName = propName
            self._getter = getter
            self._setter = setter
            pass

        def getKeyAsList(self):
            return KVDatagramService._valueToList(self._key)

        def getKeyForDict(self):
            return KVDatagramService._normalizeKey(self._key)
        
        # Called when the underlying namedbean has the watched property changed.
        #
        # @param event is a PropertyChangeEvent from java beans
        def onUpdate(self, event):
            newval = event.getNewValue()
            if self._parent._kv.checkEntry(self._key, newval):
                # no change
                print("No change in entry. skipping update event.")
                return
            self._parent._kv.addOrUpdateEntry(self._key, newval)
            self._parent._produceEvent(self._eventId)

        # Called when an incoming event report message comes with this event ID.
        def onEvent(self, msg):
            print("incoming event ID received: " + str(msg.getEventID()))
            if msg.getSourceNodeID() == self._parent._kv._if.getNodeId():
                # We emitted this event, ignore.
                print("Event from ourselves.")
                return
            # Sends an inquiry datagram to the sender of this event.
            kList = self.getKeyAsList()
            req = [KVDatagramService.REQUEST_TYPE, KVDatagramService.REQUEST_GET_ENTRY, len(kList)] + kList
            self._parent._kv._dcs.sendData(msg.getSourceNodeID(), req)

    # This class receives messages from the OpenLCB interface, and forwards
    # select Event Report messages to registered listeners. Internally it has a
    # dictionary of EventIDs. This makes it efficient to listen to a large
    # number of different EventIDs.
    class EventDispatcher(org.openlcb.MessageDecoder):
        def __init__(self, parent):
            self._tbl = {}
            self._parent = parent

        # @param msg ProducerConsumerEventReportMessage
        # @param sender ignored
        def handleProducerConsumerEventReport(self, msg, sender):
            if msg.getEventID() not in self._tbl:
                print("EventDispatcher: event " + str(msg.getEventID()) + " not registered.")
                return;
            cblist = self._tbl[msg.getEventID()]
            for cb in cblist: cb(msg)

        # Registers an event ID with a callback. When some node produces this
        # event, the callback will be invoked with the event message.
        #
        # @param event an openlcb.EventID object
        #
        # @param cb a callback with one argument (of
        # ProducerConsumerEventReportMessage object)
        def addEvent(self, event, cb):
            if not event in self._tbl:
                self._parent._emitIdentified(event)
                self._tbl[event] = []
            self._tbl[event].append(cb)

    # Registers an entry as a producer-consumer.
    #
    # @param entry an ExportedEntry object
    def _registerEntryRW(self, entry):
        # exports current value
        self._kv.addOrUpdateEntry(entry._key, entry._getter())
        # listens to local changes in the value
        entry._bean.addPropertyChangeListener(entry._propName, entry.onUpdate)
        # listens to remote events
        self._evHandler.addEvent(entry._eventId, entry.onEvent)
        # Stores metadata dictionary indexed by the key object
        self._md[entry.getKeyForDict()] = entry

    # Registers an entry as a consumer only. This means that local changes will
    # never be exported to the layout, but a change from the layout will be
    # applied to the local JMRI object.
    #
    # @param entry an ExportedEntry object
    def _registerEntryRO(self, entry):
        # exports current value
        self._kv.addOrUpdateEntry(entry._key, entry._getter())
        # listens to remote events
        self._evHandler.addEvent(entry._eventId, entry.onEvent)
        # Stores metadata dictionary indexed by the key object
        self._md[entry.getKeyForDict()] = entry

    # Registers an entry as a producer only. This means that local changes will
    # be exported to the layout, but a change from the layout will be not be
    # applied to the local JMRI object.
    #
    # @param entry an ExportedEntry object
    def _registerEntryWO(self, entry):
        # exports current value
        self._kv.addOrUpdateEntry(entry._key, entry._getter())
        # listens to local changes in the value
        entry._bean.addPropertyChangeListener(entry._propName, entry.onUpdate)
        # Stores metadata dictionary indexed by the key object
        self._md[entry.getKeyForDict()] = entry

    # Synchronizes a memory variable's value to/from the LCC network.
    #
    # @param name is a string with the name (user name or system name) of a
    # memory variable. If this does not exist in the memory variable table,
    # then we will attempt to create a new memory for it. Example:
    # "IM:AUTO:0023". This name must be the same on all the different JMRI
    # computers.
    #
    # @param eventIdString is a string with dotted-hex notation describing an
    # LCC Event ID. This Event ID will be used to signal on the LCC network
    # that the value has changed, and must be the same on all the different
    # JMRI machines. Example: "05.01.01.01.18.DD.01.23". This should come from
    # an Event ID range that you own.
    def addMemVariablePC(self, name, eventIdString):
        b = memories.provideMemory(name)
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryRW(e)

    # Exports a memory variable's value to the LCC network
    # (producer-only). This means that local changes will be exported to the
    # layout, but a change in a different JMRI machine will be not be applied
    # to the local JMRI object.
    def addMemVariableP(self, name, eventIdString):
        b = memories.provideMemory(name)
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryWO(e)

    # Imports a memory variable's value from the LCC network
    # (consumer-only). This means that local changes will never be exported to
    # the layout, but a change exported by a different JMRI machine will be
    # applied to the local JMRI object.
    def addMemVariableC(self, name, eventIdString):
        b = memories.provideMemory(name)
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryRO(e)
        
    # Synchronizes a block's value to/from the LCC network.
    #
    # @param name is a string with the block name (user name or system
    # name). The block must already exist. Example: "IB:AUTO:0023". This name
    # must be the same on all the different JMRI computers.
    #
    # @param eventIdString is a string with dotted-hex notation describing an
    # LCC Event ID. This Event ID will be used to signal on the LCC network
    # that the value has changed, and must be the same on all the different
    # JMRI machines. Example: "05.01.01.01.18.DD.01.23". This should come from
    # an Event ID range that you own.
    def addBlockValuePC(self, name, eventIdString):
        b = blocks.getBlock(name)
        if not b:
            print("Block " + name + " not found.")
            return
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryRW(e)

    # Exports a block's value to the LCC network (producer-only). This means
    # that local changes will be exported to the layout, but a change in a
    # different JMRI machine will be not be applied to the local JMRI object.
    def addBlockValueP(self, name, eventIdString):
        b = blocks.getBlock(name)
        if not b:
            print("Block " + name + " not found.")
            return
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryWO(e)

    # Imports a block's value from the LCC network (consumer-only). This means
    # that local changes will never be exported to the layout, but a change
    # exported by a different JMRI machine will be applied to the local JMRI
    # object.
    def addBlockValueC(self, name, eventIdString):
        b = blocks.getBlock(name)
        if not b:
            print("Block " + name + " not found.")
            return
        e = LCCSyncService.ExportedEntry(
            parent=self, key=name, eventId=org.openlcb.EventID(eventIdString),
            bean=b, propName="value", getter=b.getValue, setter=b.setValue)
        self._registerEntryRO(e)
        
        
lccSyncService = LCCSyncService()

# Example for exporting a memory variable in producer-consumer mode.
# lccSyncService.addMemVariablePC("IM:AUTO:0001", "02.01.57.00.00.37.FF.55")

# Example for exporting a block value in consumer-only mode.
# lccSyncService.addBlockValueC("IBMYBLOCK", "02.01.57.00.00.37.FF.33")

print("OK!")

        
