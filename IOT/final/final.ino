#include <SoftwareSerial.h>

SoftwareSerial mySerial(9, 10);

int b1state = 0;
int b2state = 0;
int b3state = 0;

void setup() {
  Serial.begin(9600);
  mySerial.begin(9600);
 
  pinMode(3,INPUT);
  pinMode(4,INPUT);
  pinMode(5,INPUT);
}

void loop() {
 
  b1state = digitalRead(3);
  b2state = digitalRead(4);
  b3state = digitalRead(5);
 
  if(b1state == HIGH){
    Serial.println("Button road pressed");
    SendMessage("save road Reported via IOT Device");
  }
  
  if(b2state == HIGH){
    Serial.println("Button natural pressed");
    SendMessage("save natural Reported via IOT Device");
  }
  
  if(b3state == HIGH){
    Serial.println("Button fire pressed");
    SendMessage("save fire Reported via IOT Device");
  }
}

void SendMessage(String message)
{
  mySerial.println("AT+CMGF=1");    //Sets the GSM Module in Text Mode
  delay(1000);  // Delay of 1000 milli seconds or 1 second
  mySerial.println("AT+CMGS=\"77100\"\r"); // Replace x with mobile number
  delay(1000);
  mySerial.println(message);// The SMS text you want to send
  delay(100);
   mySerial.println((char)26);// ASCII code of CTRL+Z
  delay(1000);
}
