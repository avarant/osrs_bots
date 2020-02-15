
import random
import time
import pyautogui

while True:
    pyautogui.click()
    time.sleep(random.random())
    pyautogui.click()
    time.sleep(random.randint(51,57) + random.random())
