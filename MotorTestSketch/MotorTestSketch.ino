// Harris Ransom, Connor Perrin, Kalash Kapadia
// PLTW EDD Condurso Class 7 

const byte MOTOR = 5;
const int DELAY_TIME = 5000;
short motorSpeed = 0;

// Setup
void setup() {
  pinMode(MOTOR, OUTPUT);
  Serial.begin(9600);
}

// Loop
void loop() {
  // Digital On/Off
  /*digitalWrite(MOTOR, HIGH);
  Serial.println("Motor ON");
  delay(DELAY_TIME);
  digitalWrite(MOTOR, LOW);
  Serial.println("Motor OFF");
  delay(DELAY_TIME);*/

  // Analog Ramp-Up/Ramp-Down
  Serial.println("Ramping up");
  for (int i=0; i<256; i++) {
    analogWrite(MOTOR, i);
    delay(15);
  }
  Serial.println("Ramping down");
  for (int i=255; i >= 0; i--) {
    analogWrite(MOTOR, i);
    delay(15);
  }
  
  delay(100);
}
