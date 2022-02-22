// EDD Tumbler Composter Arduino Code
// Kalash, Harris, Connor

/** Includes **/
#include <Adafruit_AHTX0.h>
#include <Adafruit_SGP30.h>
#include <DallasTemperature.h>
#include <LiquidCrystal_I2C.h>
#include <OneWire.h>
#include <Wire.h>

/** LCD Initialization **/
LiquidCrystal_I2C lcd(0x27, 16, 2);

/** Sensor Initialization **/
#define ONE_WIRE_BUS 2
OneWire oneWire(ONE_WIRE_BUS);  
DallasTemperature tempSensor(&oneWire);
Adafruit_AHTX0 dht20;
Adafruit_SGP30 sgp;
int counter = 0;

const short MOTOR = 23;

// Setup
void setup() {
  // Serial console
  Serial.begin(9600);
  while (!Serial) { delay(10); };
  
  // Connect to DS18B20 temperature sensor
  tempSensor.begin();
  
  // LCD
  lcd.init();
  lcd.clear();
  lcd.backlight();

  // Connect to SGP30
  if (! sgp.begin()){
    Serial.println("SGP30 not found");
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

  
}


// Loop
void loop() {
  // DHT20 and DS18B20 sensor readings
  sensors_event_t dhtHumidityEvent, dhtTempEvent;     // Container for DHT20 humidity and temperature readings
  dht20.getEvent(&dhtHumidityEvent, &dhtTempEvent);   // Read DHT20 temperature and humidity
  double dhtTemp = dhtTempEvent.temperature;
  double dhtHumidity = dhtHumidityEvent.relative_humidity;
  tempSensor.requestTemperatures();                   // Send request to DS18B20 OneWire bus

  // Sets SGP30 absolute humidity to enable humidity compensation for air quality readings
  sgp.setHumidity(getAbsoluteHumidity(dhtTemp, dhtHumidity));

  // SGP30 reading
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
  
  // Output sensor readings to serial monitor
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

  // Output readings to LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("TempC: ");
  lcd.print(tempSensor.getTempCByIndex(0));
  lcd.setCursor(0, 1);
  lcd.print("Humidity: ");
  lcd.print(dhtHumidity);
  

  // Loop delay == 5s
  delay(5000);

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
