// Input sketch for JMRI 
//
// Bob Jacobsen   copyright (C) 2016
//
//

// For Arduino Mega analog and digital input
int aInPins[] = {0,1,2,3,4,5,6,7};
int dInPins[] = {22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45};

int speed = 19200;
int pause = 10;  // a small inter-line pause to all JMRI to catch up



int numAInPins = sizeof(aInPins)/sizeof(int);
int numDInPins = sizeof(dInPins)/sizeof(int);

void setup() {
  // for all digital inputs  
  for (int i = 0; i < numDInPins; i++) {
    // set each dInPin to input, pullup
    pinMode(dInPins[i], INPUT);
    digitalWrite(dInPins[i], 1);
  }
  
  // open serial link
  Serial.begin(speed);
}


void loop() { // one output line per loop pass

   bool first = true;  // no comma before 1st value
   
  // write a line, first the analog data
  for (int i = 0; i < numAInPins; i++) {
    if (!first) Serial.print(',');
    first = false;
    Serial.print(analogRead(aInPins[i]));
  }

  // then the digital
  for (int i = 0; i < numDInPins; i++) {
    if (!first) Serial.print(',');
    first = false;
    Serial.print((digitalRead(dInPins[i])==HIGH) ? '1' : '0');  // HIGH sent as 1
  }  
  
  // write end of line
  Serial.println();
  
  // and now wait
  delay(pause);

}
