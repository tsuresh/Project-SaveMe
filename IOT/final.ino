#include <SoftwareSerial.h>

//bluetooth module
SoftwareSerial Genotronex(7, 6); // RX, TX

//set the switch states
int b1state = 0;
int b2state = 0;
int b3state = 0;

void setup() {
  Serial.begin(9600);
  Genotronex.begin(9600);
  
  //reporting buttons
  pinMode(8,INPUT);
  pinMode(9,INPUT);
  pinMode(10,INPUT);
}

void loop() {
  
  //peizo sensor values
  int sensorpin = analogRead(A1);
  float sensorv = sensorpin / 1023.0 * 5.0;
  
  if(sensorv > 0.2){df 
    report("road");
  }
  
  //read the reporting buttons
  b1state = digitalRead(8);
  b2state = digitalRead(9);
  b3state = digitalRead(10);
 
  if(b1state == LOW){
    report("road");
  }
  
  if(b2state == LOW){
    report("fire");
  }
  
  if(b3state == LOW){
    report("thief");
  }
  
  delay(100);
}
void report(String type){
  if(type == "road"){
    //report road accident
    Serial.println("Road accident occured");
    Genotronex.println("road");
  } else if(type == "fire"){
    //report fire
    Serial.println("Fire alert occured");
    Genotronex.println("fire");
  } else if(type == "thief"){
    //report thief
    Serial.println("Thief alert occured");
    Genotronex.println("thief");
  } else {
   //nothing happens 
    Serial.println("Invalid Command");
  }
}
