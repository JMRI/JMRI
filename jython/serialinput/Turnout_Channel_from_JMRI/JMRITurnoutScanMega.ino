//  Receive Bulk Data from the Turnout Table via Serial Transmission from JMRI
//  Author: Geoff Bunza 2018
//   Version 1.1
//  This is preconfigured for an Arduino Mega2560, pins 2-69, no inversion, no offset
//  Transmission is of the form "ATxxx.S where AT is the user name prefix from the JMRI turnout table 
//  xxx is the number of the turnout (User name is built of the form AT123, 
//  and "S" is the "State" of the turnout where Closed=0 and Thrown=1
//  Transmission is received from JMRI via TurnoutDataTransfer.py Python Script
//  It is assumed that the Arduino starts up before the Python Script 
//  The script will set up listeners for all its turnouts and only update them when they change
//  The Arduino will update the appropriate pin, which can have an offset, and be o[ptionally inverted
//  The serial port assigned to this Arduino must correspond to the Serial Port in the corresponding TurnoutDataTransfer.py Script
//
//
#define Data_Pin_Max    70  // Max sensor pin NUMBER (plus one) Mega=70,UNO,Pro Mini,Nano=20
#define Data_Pin_Start   2  // Starting Sensor Pin number (usually 2 as 0/1 are TX/RX
#define Data_Pin_Offset  0  // This Offset will be ADDED to the value of each Turnout number to determine the Data pin
                            // number used by the Arduino, so pin AT12 will set pin 12+Data_Offset)
                            // This would allow one Arduino Turnout Data channel to use AT2-69 set pins 2-69 and another to 
                            // use AT70-137 to set its pins 2-69 for example; this offset can also be negative
#define Data_Invert      0  // Set Data_Active_Low to 1 to invert incomin Turnout data Closed=1 Thrown=0
                            // Set Data_Active_Low to 0 to leave incoming data untouched Closed=0 Thrown=1
#define open_delay 15       // longer delay to get past script initialization  - leave this alone
#define delta_delay 4       // Short delay to allow the script to get all the characters - leave this alone
int i;
char  firstByte ;           // temp to process the possible state change
char  secondByte ;
char  incomingByte = 0;     // working temp for character processing
int   turnout = 99;
#define port_delay 600      // Serial Port delay (microsec) for error compensation - increase this if you get funny data at high speeds
void setup(){
    Serial.begin(19200);     // Open serial connection.
    for (i=Data_Pin_Start; i< Data_Pin_Max; i++) {  // Set up all the output pins
    pinMode (i, OUTPUT);
    digitalWrite (i, LOW);
  }  
}
void loop()  {
    if (Serial.available() > 0)  {  // wait until we get a charater from JMRI
      // get the first character
      firstByte = Serial.read();
      delayMicroseconds(port_delay);
      while (firstByte != 'A') {
        firstByte = Serial.read();
        delayMicroseconds(port_delay);
      }
      secondByte = Serial.read();       // get the second character
      if ((firstByte=='A') && (secondByte == 'T'))  {
        i=0;
        turnout = Serial.parseInt();
        turnout = turnout + Data_Pin_Offset ;
        incomingByte=Serial.read();
        delayMicroseconds(port_delay);
        incomingByte=Serial.read();
        if (incomingByte =='1') digitalWrite (turnout, 1 ^ Data_Invert);
        if (incomingByte =='0') digitalWrite (turnout, 0 ^ Data_Invert);
        //Serial.print (turnout);
        //Serial.print ("  "); 
        //Serial.println (incomingByte);
      }
  }
}
