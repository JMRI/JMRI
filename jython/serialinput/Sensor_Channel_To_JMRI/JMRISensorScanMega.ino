//  Initialize and Scan Sensor Data for Serial Transmission to JMRI Sensor Table
//  Author: Geoff Bunza 2018
//   Version 1.2
//  Transmission starts with a synchronizing character, here the character "A" followed by 
//  1 byte with  bit 7 Sensor ON/OFF bit 6-0 sensor # 1-127
//  Transmission is received in JMRI via SerialSensorMux.py Python Script
//  It is assumed that the Arduino sensor mux starts up before the Python Script which will transmit "!!!\n"
//  To indicate the script is running and ready for reception
//  The Arduino will then poll all (up to 70) sensors and transmit thir states for initialization in the JMRI sensor table
//  The serial port assigned to this Arduino must correspond to the Serial Port in the corresponding Sensor Script
//  The Arduino will update JMRI ONLY upon detecting a sensor change minimizing overhead transmission to JMRI
//
#define Sensor_Pin_Max   70  // Max sensor pin NUMBER (plus one) Mega=70,UNO,Pro Mini,Nano=20
#define Sensor_Pin_Start  2  // Starting Sensor Pin number (usually 2 as 0/1 are TX/RX
#define Sensor_Offset     18  // This Offset will be ADDED to the value of each Sensor_Pin to determine the sensor
                             // number sent to JMRI, so pin D12 will set sensor AR:(12+Sensor_Offset) in JMRI
                             // This would allow one Arduino Sensor channel to set sensors 2-69 and another to 
                             // Set sensors 70-137 for example; this offset can also be negative
#define Sensors_Active_Low 1 // Set Sensors_Active_Low to 1 if sensors are active LOW
                             // Set Sensors_Active_Low to 0 if sensors are active HIGH
#define open_delay 15        // longer delay to get past script initialization
#define delta_delay 4        // Short delay to allow the script to get all the characters
int i;
char  sensor_state [70];     // up to 70 sensors on a Mega2560
char  new_sensor_state ;     // temp to process the possible state change
char  incomingByte = 0;      // working temp for character processing

void setup(){
    Serial.begin(19200);              // Open serial connection.
    while (Serial.available() == 0);  // wait until we get a charater from JMRI
    incomingByte=Serial.read();       // get the first character
    while ((Serial.available() > 0) && (incomingByte != '!')) incomingByte=Serial.read(); //get past !!!
    while ((Serial.available() > 0) ) incomingByte=Serial.read();                       //flush anything else
    delay(open_delay);                 // take a breath
    for ( i=Sensor_Pin_Start; i<Sensor_Pin_Max; i++)  {  //Initialize all sensors in JMRI and grab each sensor
       pinMode(i, INPUT_PULLUP);       // define each sensor pin as coming in
       sensor_state[i] = (digitalRead( i ))^Sensors_Active_Low;    // read & save each sensor state & invert if necessary
       Serial.print("A"); Serial.print (char((sensor_state[i]<<7)+i+Sensor_Offset));  // send "A <on/off><snesor #>" to JMRI script
       delay(delta_delay);             // in milliseconds, take a short breath as not to overwhelm JMRI's seraial read
    }
}
void loop()  {
   for ( i=Sensor_Pin_Start; i<Sensor_Pin_Max; i++)  {     // scan every sensor over and over for any sensor changes
       new_sensor_state = (digitalRead( i ))^Sensors_Active_Low;  // read & save each sensor state & invert if necessary
       if (new_sensor_state != sensor_state[i] )  {               // check if the sensor changed ->if yes update JMRI
         Serial.print("A"); Serial.print (char((new_sensor_state<<7)+i+Sensor_Offset)); // send "A <on/off><snesor #>" to JMRI script
         sensor_state[i] = new_sensor_state ;                     // save the updated sensor state
         delay(delta_delay);            // in milliseconds, take a short breath as not to overwhelm JMRI's seraial read
       }
   }
}
