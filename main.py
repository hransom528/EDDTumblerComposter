# Raspberry Pi Tumbler Code
# EDD 2022 - Condurso Class 7

# Imports
import time
import board
import busio
import pwmio
import adafruit_motor   # Servo interfacing
import Adafruit_DHT     # DHT20 lib
import adafruit_sgp30   # SGP30 lib
from w1thermsensor import W1ThermSensor, Unit #DS18B20 lib

# Initializes sensors
# TODO: Fix DHT initialization
dhtDevice = Adafruit_DHT.DHT22(board.D18)
i2c = busio.I2C(board.SCL, board.SDA, frequency=100000)
sgp30 = adafruit_sgp30.Adafruit_SGP30(i2c)
print("SGP30 serial #", [hex(i) for i in sgp30.serial])
tempsensor = W1ThermSensor()

# create a PWMOut object on Pin A2.
# TODO: Determine PWM pin
pwm = pwmio.PWMOut(board.D2, duty_cycle=2 ** 15, frequency=50)

# Initializes servo
servo1 = servo.Servo(pwm)


# MAIN
def main():
    # Main loop
    while(True):
        # Get temperature from DS18B20
        tempC = tempsensor.get_temperature()
        tempF = tempsensor.get_temperature(Unit.DEGREES_F)
        
        
        # Gets eC02 and TVOC from SGP30
        eC02 = sgp30.eC02
        TVOC = sgp30.TVOC
        print("eCO2 = %d ppm \t TVOC = %d ppb" % (eCO2, TVOC))
  

# Runs main function
if __name__ == "__main__":
    main()
  
