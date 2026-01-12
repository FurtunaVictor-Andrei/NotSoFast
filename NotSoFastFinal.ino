#include <SoftwareSerial.h>
const int forwardPin = 8;
const int forwardPin2= 7;
const int backwardPin=12;
const int backwardPin2 =13;
const int trigPin=9;
const int echoPin=10;
float duration, distance;
SoftwareSerial BT(2, 3);
char cmd;
int quest;

void setup() {
  
 
  pinMode(forwardPin, OUTPUT);
  pinMode(forwardPin2, OUTPUT);
  pinMode(backwardPin, OUTPUT);
  pinMode(backwardPin2, OUTPUT);
  BT.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  Serial.begin(9600);

}

void loop() {
  digitalWrite(trigPin, HIGH);
  
  digitalWrite(trigPin, LOW);
  duration=pulseIn(echoPin, HIGH, 30000);
  distance = duration * 0.034/2;
  
  if(BT.available()){
    cmd=BT.read();
  }
  if(distance>20)
  {
    quest=1;
  if(cmd=='F')
  {
    forward();
  }
  if(cmd=='S')
  {
    stop1();
  }
  if(cmd=='B')
  {
    backward();
  }
  if(cmd=='L')
  {
    left();
  }
  if(cmd=='R')
  {
    right();
  }
  }
  else
  {
    if(quest==1)
    {
      BT.print('T');
      quest=0;
    }
    if(cmd=='B')
    {
      backward();
    }
    else
    {
    stop1();
    }
  }
  
  
  
}

void forward()
{
  digitalWrite(forwardPin, HIGH);
  digitalWrite(forwardPin2, HIGH);
  digitalWrite(backwardPin, LOW);
  digitalWrite(backwardPin2, LOW);
}
void backward()
{
  digitalWrite(backwardPin, HIGH);
  digitalWrite(backwardPin2, HIGH);
  digitalWrite(forwardPin, LOW);
  digitalWrite(forwardPin2, LOW);
}
void left()
{
  digitalWrite(forwardPin, HIGH);
  digitalWrite(backwardPin2, HIGH);
  digitalWrite(forwardPin2, LOW);
  digitalWrite(backwardPin, LOW);
}
void right()
{
  digitalWrite(forwardPin, LOW);
  digitalWrite(backwardPin2, LOW);
  digitalWrite(forwardPin2, HIGH);
  digitalWrite(backwardPin, HIGH);
}
void stop1()
{
  digitalWrite(forwardPin, LOW);
  digitalWrite(forwardPin2, LOW);
  digitalWrite(backwardPin, LOW);
  digitalWrite(backwardPin2, LOW);
}
