# Raspberry Pi Tumbler Code
# EDD 2022 - Condurso Class 7

# Imports
import time
import board
import busio
import pwmio
import adafruit_motor
import Adafruit_DHT
import adafruit_sgp30
from w1thermsensor import W1ThermSensor, Unit

# Initializes sensors
# TODO: Fix DHT initialization
dhtDevice = adafruit_dht.DHT22(board.D18)
i2c = busio.I2C(board.SCL, board.SDA, frequency=100000)
sensor = W1ThermSensor()

# create a PWMOut object on Pin A2.
# TODO: Determine PWM pin
pwm = pwmio.PWMOut(board.D2, duty_cycle=2 ** 15, frequency=50)

# Initializes servo
servo1 = servo.Servo(pwm)


# MAIN
def main():
  pass
  

if __name__ == "__main__":
  main()
  
