// EDD Tumbler Composter Arduino Code
// Kalash, Harris, Connor

/** Includes **/
#include <Adafruit_AHTX0.h>
#include <Adafruit_SGP30.h>
#include <DallasTemperature.h>
#include <LiquidCrystal.h>
#include <OneWire.h>
#include <Wire.h>

/** LCD Initialization **/
const int LCD_RS = 12;
const int LCD_RW = 11;
const int LCD_EN = 10;
const int LCD_D4 = 5;
const int LCD_D5 = 4;
const int LCD_D6 = 3;
const int LCD_D7 = 2;
LiquidCrystal lcd(LCD_RS, LCD_RW, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7);

/** Sensor Initialization **/
#define ONE_WIRE_BUS 8
OneWire oneWire(ONE_WIRE_BUS);  
DallasTemperature tempSensor(&oneWire);
Adafruit_AHTX0 dht20;
Adafruit_SGP30 sgp;

//const int MOTOR = ;

// Setup
void setup() {
  // Serial console
  Serial.begin(9600);
  while (!Serial) { delay(10); };

  // LCD
  lcd.begin(16, 2);

  // Connect to SGP30
  if (! sgp.begin()){
    Serial.println("Sensor not found :(");
    while (1);
  }
  Serial.print("Found SGP30 serial #");
  Serial.print(sgp.serialnumber[0], HEX);
  Serial.print(sgp.serialnumber[1], HEX);
  Serial.println(sgp.serialnumber[2], HEX);

  // Connect to DHT20
  if (!dht20.begin()) {
    Serial.println("DHT20 not found");
    while (1) delay(10);
  }

  // Connect to DS18B20 temperature sensor
  tempSensor.begin();
  
}


// Loop
void loop() {
  // DHT20 and DS18B20 sensor readings
  sensors_event_t dhtHumidity, dhtTemp;     // Container for DHT20 humidity and temperature readings
  dht20.getEvent(&dhtHumidity, &dhtTemp);   // Read DHT20 temperature and humidity
  tempSensor.requestTemperatures();         // Send request to DS18B20 OneWire bus
  double tempC = tempSensor.getTempCByIndex(0); // Read DS18B20 temperature

  // SGP30 reading
  if (! sgp.IAQmeasure()) {
    Serial.println("Measurement failed");
    return;
  }
  

  // Convert sensor readings to strings
  
  // Output sensor readings
  Serial.println("DHT20 Data: ");
  //Serial.print("\tTemperature: %d", dhtTemp);
  //Serial.print("\tHumidity: %d", dhtHumidity);
  Serial.println("DS18B20 Data: ");
  //Serial.print("\tTemperature: %f", tempC);
  Serial.println("SGP30 Data: ");
  //Serial.print("\teCO2: %d", );
  

  delay(500);
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
