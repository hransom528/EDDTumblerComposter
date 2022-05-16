 // EDD Tumbler Composter Arduino Code
// Kalash, Harris, Connor

/** Includes **/
#include <Adafruit_AHTX0.h>
#include <Adafruit_SGP30.h>
#include <DallasTemperature.h>
#include <LiquidCrystal_I2C.h>
#include <OneWire.h> 
#include <AltSoftSerial.h>
#include <SoftwareSerial.h> // TODO: Update with AltSoftSerial or HardwareSerial
#include <Wire.h>

/** LCD Initialization **/
short LCD_ADDR = 0x27;
LiquidCrystal_I2C lcd(LCD_ADDR, 16, 2);

/** Sensor Initialization **/
#define ONE_WIRE_BUS 2
OneWire oneWire(ONE_WIRE_BUS);  
DallasTemperature tempSensor(&oneWire);
Adafruit_AHTX0 dht20;
Adafruit_SGP30 sgp;
int counter = 0;

/** Output devices **/
const byte MOTOR = 23;
const byte SPEAKER = 9;

/** WiFi Module **/
const byte RX1 = 10;
const byte TX1 = 11;
const int ESP01_CLOCK = 9600;
SoftwareSerial esp01(RX1, TX1);
//SoftwareSerial esp01(RX1, TX1); // RX, TX
bool wifiAvailable = false;
const String SERVER_TIMEOUT = "900";
bool phoneConnected = false;

/** Multitasking **/
long lcdInterval = 5000;
long lcdPreviousMillis = 0;
byte lcdCycle = 0;

// Setup
void setup() {
  // Serial console
  Serial.begin(9600);
  while (!Serial) { delay(10); };
  Serial.print("Connected to serial monitor");
  
  // Connect to ESP01 WiFi module
  Serial.println("\nConnecting to ESP01 WiFi module...");
  esp01.begin(ESP01_CLOCK);
  sendCommand("AT");
  sendCommand("AT+GMR");
  sendCommand("AT+CWMODE=3");                 // Configure ESP8266 as an access point
  sendCommand("AT+CIFSR");                    // Get the ip address
  sendCommand("AT+CIPMUX=1");                 // Configure for multiple connections
  //sendCommand("AT+CIPSERVER=0,80");           // Turns off server on port 80 to disconnect previous connections
  sendCommand("AT+CIPSERVER=1,80");           // Turn on server on port 80
  sendCommand("AT+CIPSTO=" + SERVER_TIMEOUT); // Set server timeout
  sendCommand("AT+CWSAP?");                   // Gets final configuration of ESP8266
  Serial.println("WiFi Module Configured Successfully"); 
  
  // Connect to DS18B20 temperature sensor
  tempSensor.begin();
  
  // Initialize LCD
  lcd.init();
  lcd.clear();
  lcd.backlight();

  // Connect to SGP30 air quality sensor
  if (! sgp.begin()){
    Serial.println("SGP30 not found");
    while (1);
  }
  Serial.print("Found SGP30 serial #");
  Serial.print(sgp.serialnumber[0], HEX);
  Serial.print(sgp.serialnumber[1], HEX);
  Serial.println(sgp.serialnumber[2], HEX);

  // Connect to DHT20 humidity sensor
  if (!dht20.begin()) {
    Serial.println("DHT20 not found");
    while (1) delay(10);
  } 
}

// Loop
void loop() {
  // Loop timing
  unsigned long currentMillis = millis();
  
  // Checks if ESP8266 received data
  wifiAvailable = wifiInput();
  if (wifiAvailable) {
    String inputString = esp01.readString();
    Serial.println(inputString);
    if (inputString.equals("0,CONNECT")) {
      phoneConnected = true;
    }
    else if (inputString.equals("0, CLOSED")) {
      phoneConnected = false;
    }
  }
  
  // DHT20 and DS18B20 sensor readings
  sensors_event_t dhtHumidityEvent, dhtTempEvent;     // Container for DHT20 humidity and temperature readings
  dht20.getEvent(&dhtHumidityEvent, &dhtTempEvent);   // Read DHT20 temperature and humidity
  double dhtTemp = dhtTempEvent.temperature;
  double dhtHumidity = dhtHumidityEvent.relative_humidity;
  tempSensor.requestTemperatures();                   // Send request to DS18B20 OneWire bus

  // Sets SGP30 absolute humidity to enable humidity compensation for air quality readings
  sgp.setHumidity(getAbsoluteHumidity(dhtTemp, dhtHumidity));

  
  // SGP30 reading and output
  Serial.println("SGP30 Data: ");
  if (!sgp.IAQmeasure()) {
    Serial.println("SGP30 measurement failed");
    return;
  }
  Serial.print("\tTVOC "); Serial.print(sgp.TVOC); Serial.print(" ppb\t");
  Serial.print("\teCO2 "); Serial.print(sgp.eCO2); Serial.println(" ppm");
  if (! sgp.IAQmeasureRaw()) {
    Serial.println("SGP30 raw Measurement failed");
    return;
  }
  Serial.print("\tRaw H2: "); Serial.print(sgp.rawH2); Serial.print(" \t");
  Serial.print("\tRaw Ethanol: "); Serial.print(sgp.rawEthanol); Serial.println("");
  
  // Output DHT20 and DS18B20 sensor readings to serial monitor
  Serial.println("DHT20 Data: ");
  Serial.print("\tTemperature C: ");
  Serial.print(dhtTemp);
  Serial.print("\tHumidity: ");
  Serial.println(dhtHumidity);
  Serial.println("DS18B20 Data: ");
  Serial.print("\tTemperature C: ");
  Serial.println(tempSensor.getTempCByIndex(0));
  Serial.print("\tTemperature F: ");
  Serial.println(tempSensor.getTempFByIndex(0));
  Serial.println('\n');

  // Output readings to LCD with a 5s cycle
  if (delayMillis(currentMillis, &lcdPreviousMillis, lcdInterval)) {
    switch (lcdCycle) {
      case 0:
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("TempC: ");
        lcd.print(tempSensor.getTempCByIndex(0));
        lcd.setCursor(0, 1);
        lcd.print("Humidity: ");
        lcd.print(dhtHumidity);
        lcdCycle = 1;
        break;
      case 1:
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("TVOC(ppB): ");
        lcd.print(sgp.TVOC);
        lcd.setCursor(0, 1);
        lcd.print("eCO2(ppM): ");
        lcd.print(sgp.eCO2);
        lcdCycle = 2;
        break;
      case 2:
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Raw H2: ");
        lcd.print(sgp.rawH2);
        lcd.setCursor(0, 1);
        lcd.print("Raw Ethanol: ");
        lcd.print(sgp.rawEthanol);
        lcdCycle = 0;
        break;
    }
  }
  
  // Small loop delay
  delay(10);

  // SGP30 periodic calibration
  counter++;
  if (counter == 30) {
    counter = 0;

    uint16_t TVOC_base, eCO2_base;
    if (! sgp.getIAQBaseline(&eCO2_base, &TVOC_base)) {
      Serial.println("Failed to get baseline readings");
      return;
    }
    Serial.print("****Baseline values: eCO2: 0x"); Serial.print(eCO2_base, HEX);
    Serial.print(" & TVOC: 0x"); Serial.println(TVOC_base, HEX);
  }
}

// Tests for data from esp8266
bool wifiInput() {
  bool wifiAvailable = esp01.available();
  if (wifiAvailable >= 1) {
    return true; 
  }
  else {
    return false;  
  }
}

// Sends AT serial command to Bluetooth and WiFi modules
void sendCommand(String command) {
  String message = "";
  esp01.println(command);
  while (!esp01.available()) { delay(5); }
  message =  esp01.readString();
  Serial.println(message);
}


/* return absolute humidity [mg/m^3] with approximation formula
* @param temperature [°C]
* @param humidity [%RH]
*/
uint32_t getAbsoluteHumidity(float temperature, float humidity) {
    // approximation formula from Sensirion SGP30 Driver Integration chapter 3.15
    const float absoluteHumidity = 216.7f * ((humidity / 100.0f) * 6.112f * exp((17.62f * temperature) / (243.12f + temperature)) / (273.15f + temperature)); // [g/m^3]
    const uint32_t absoluteHumidityScaled = static_cast<uint32_t>(1000.0f * absoluteHumidity); // [mg/m^3]
    return absoluteHumidityScaled;
}

// Implements delay function using millis(), allowing for multitasking
bool delayMillis(long currentMillis, long *previousMillis, long interval) {
  if (currentMillis - *previousMillis >= interval) {
    *previousMillis = millis();
    return true;
  }
  else {
    return false; 
  }
}
