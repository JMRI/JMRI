package jmri.util.com.rbnb;

// This class comes from the Java2s code examples at
// http://www.java2s.com/Code/Java/Network-Protocol/UDPOutputStream.htm
/*

 Copyright 2007 Creare Inc.

 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 

 http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */

/*
 *****************************************************************
 ***                ***
 ***  Name :  UDPOutputStream                                 ***
 ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
 ***  For  :  E-Scan            ***
 ***  Date :  October, 2001          ***
 ***                ***
 ***  Copyright 2001 Creare Inc.        ***
 ***  All Rights Reserved          ***
 ***                ***
 ***  Description :            ***
 ***       This class extends OutputStream, providing its API  ***
 ***   for calls to a UDPSocket.                               ***
 ***                ***
 ***   NB: THIS CLASS IS NOT THREADSAFE.  DO NOT SHARE ONE    ***
 ***      INSTANCE OF THIS CLASS AMONG MULTIPLE THREADS.       ***
 ***                ***
 *****************************************************************
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPOutputStream extends OutputStream {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final int DEFAULT_MAX_BUFFER_SIZE = 8192;

    protected DatagramSocket dsock = null;
    DatagramPacket dpack = null;
    InetAddress iAdd = null;
    int port = 0;

    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    byte[] outdata = null;
    int idx = 0; // buffer index; points to next empty buffer byte
    int bufferMax = DEFAULT_MAX_BUFFER_SIZE;

    /*
     * ******************** constructors *******************
     */
    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Default constructor.                                ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream() {
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Constructor.  Sets size of buffer.                  ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream(int buffSize) {
        setBufferSize(buffSize);
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Constructor.  Sets the address and port of the  UDP ***
     ***   socket to write to.                                     ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream(String address, int portI)
            throws UnknownHostException, SocketException, IOException {

        open(InetAddress.getByName(address), portI);
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  November, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Constructor.  Sets the address and port of the  UDP ***
     ***   socket to write to.                                     ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream(InetAddress address, int portI)
            throws SocketException, IOException {

        open(address, portI);
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Constructor.  Sets the address and port of the  UDP ***
     ***   socket to write to.  Sets the size of the buffer.       ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream(String address, int portI, int buffSize)
            throws UnknownHostException, SocketException, IOException {

        open(InetAddress.getByName(address), portI);
        setBufferSize(buffSize);
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  UDPOutputStream                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Constructor.  Sets the address and port of the  UDP ***
     ***   socket to write to.  Sets the size of the buffer.       ***
     ***                ***
     *****************************************************************
     */
    public UDPOutputStream(InetAddress address, int portI, int buffSize)
            throws SocketException, IOException {

        open(address, portI);
        setBufferSize(buffSize);
    }

    /*
     * ********** opening and closing the stream ***********
     */
    /*
     *****************************************************************
     ***                ***
     ***  Name :  open                                             ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       The user may use this method to set the address and ***
     ***   port of the UDP socket to write to.                     ***
     ***                ***
     *****************************************************************
     */
    public void open(InetAddress address, int portI)
            throws SocketException, IOException {

        dsock = new DatagramSocket();
        iAdd = address;
        port = portI;
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  close                                       ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Close the UDP socket and UDPOutputStream.           ***
     ***                ***
     *****************************************************************
     */
    @Override
    public void close() throws IOException {
        dsock.close();
        dsock = null;
        idx = 0;
    }

    /*
     * ********* writing to and flushing the buffer ***********
     */
    /*
     *****************************************************************
     ***                ***
     ***  Name :  flush                                     ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Flush current buffer contents to UDP socket.        ***
     ***                ***
     *****************************************************************
     */
    @Override
    public void flush() throws IOException {
        if (idx == 0) {  // no data in buffer
            return;
        }

        // copy what we have in the buffer so far into a new array;
        // if buffer is full, use it directly.
        if (idx == buffer.length) {
            outdata = buffer;
        } else {
            outdata = new byte[idx];
            System.arraycopy(buffer,
                    0,
                    outdata,
                    0,
                    idx);
        }

        // send data
        dpack = new DatagramPacket(outdata, idx, iAdd, port);
        dsock.send(dpack);

        // reset buffer index
        idx = 0;
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  write(int)                                     ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Writes the input value to the UDP socket.  May      ***
     ***   buffer the value.                                       ***
     ***       Input value is converted to a byte.                 ***
     ***                ***
     *****************************************************************
     */
    @Override
    public void write(int value) throws IOException {
        buffer[idx] = (byte) (value & 0x0ff);
        idx++;

        if (idx >= buffer.length) {
            flush();
        }
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  write(byte[])                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Writes the input byte array to the UDP socket.  May ***
     ***   buffer the values.                                      ***
     ***                ***
     *****************************************************************
     */
    @Override
    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  write(byte[], int, int)                         ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Writes len bytes of the input byte array to the UDP ***
     ***   socket, starting at offset off.  May buffer the values. ***
     ***                ***
     *****************************************************************
     */
    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        int lenRemaining = len;

        try {
            while (buffer.length - idx <= lenRemaining) {
                System.arraycopy(data,
                        off + (len - lenRemaining),
                        buffer,
                        idx,
                        buffer.length - idx);
                lenRemaining -= buffer.length - idx;
                idx = buffer.length;
                flush();
            }

            if (lenRemaining == 0) {
                return;
            }

            System.arraycopy(data,
                    off + (len - lenRemaining),
                    buffer,
                    idx,
                    lenRemaining);
            idx += lenRemaining;
        } catch (ArrayIndexOutOfBoundsException e) {
            // 04/03/02 UCB - DEBUG
            System.err.println("len: " + len);
            System.err.println("lenRemaining: " + lenRemaining);
            System.err.println("idx: " + idx);
            System.err.println("buffer.length: " + buffer.length);
            System.err.println("offset: " + off);
            System.err.println("data.length: " + data.length);
            throw e;
        }
    }

    /*
     * ***************** buffer size accesors *****************
     */
    /*
     *****************************************************************
     ***                ***
     ***  Name :  getBufferSize                                 ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       How many bytes are buffered before being flushed.   ***
     ***                ***
     *****************************************************************
     */
    public int getBufferSize() {
        return buffer.length;
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  setMaxBufferSize()                        ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  November, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Allows user to set upper limit on output buffer     ***
     ***   size.  Set by default to DEFAULT_MAX_BUFFER_SIZE.       ***
     ***                ***
     *****************************************************************
     */
    public void setMaxBufferSize(int max) {
        bufferMax = max;
    }

    /*
     *****************************************************************
     ***                ***
     ***  Name :  setBufferSize()                                ***
     ***  By   :  U. Bergstrom   (Creare Inc., Hanover, NH)  ***
     ***  For  :  E-Scan            ***
     ***  Date :  October, 2001          ***
     ***                ***
     ***  Copyright 2001 Creare Inc.        ***
     ***  All Rights Reserved          ***
     ***                ***
     ***  Description :            ***
     ***       Sets the length of the buffer.  Must be at least 1  ***
     ***   byte long.  Tries to flush any data currently in buffer ***
     ***   before resetting the size.                              ***
     ***                ***
     *****************************************************************
     */
    public void setBufferSize(int buffSize) {
        try {
            flush();
        } catch (IOException ioe) {
        }

        if (buffSize == buffer.length) {
            // a no-op; we are already the right size
            return;
        } else if (buffSize > 0) {
            if (buffSize > bufferMax) {
                buffer = new byte[bufferMax];
            } else {
                buffer = new byte[buffSize];
            }
        } else {
            buffer = new byte[1];
        }
    }
}
