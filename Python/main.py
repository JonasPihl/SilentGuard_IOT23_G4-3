import random
import time

import hue


def alarm():
    i = 0
    hue.define_pre_state(1)
    while i < 300:

        x = random.random()
        y = random.random()
        print(f'{x}:{y}')
        hue.alarm_state(1, 0.4, 0.1)
        i =+ i + 1
    hue.pre_state(1)
alarm()
