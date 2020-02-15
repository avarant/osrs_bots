import pyautogui
import random
import scipy
import time
from scipy import interpolate

# Any duration less than this is rounded to 0.0 to instantly move the mouse.
pyautogui.MINIMUM_DURATION = 0  # Default: 0.1
# Minimal number of seconds to sleep between mouse moves.
pyautogui.MINIMUM_SLEEP = 0  # Default: 0.05
# The number of seconds to pause after EVERY public function call.
pyautogui.PAUSE = 0  # Default: 0.1

cp = random.randint(3, 5)  # Number of control points. Must be at least 2.
x1, y1 = pyautogui.position()  # Starting position
x2, y2 = (437, 178) # Destination

# Distribute control points between start and destination evenly.
x = scipy.linspace(x1, x2, num=cp)
y = scipy.linspace(y1, y2, num=cp)

# Randomise inner points a bit (+-RND at most).
RND = 10
xr = scipy.random.randint(-RND, RND, size=cp)
yr = scipy.random.randint(-RND, RND, size=cp)
xr[0] = yr[0] = xr[-1] = yr[-1] = 0
x += xr
y += yr

# Approximate using Bezier spline.
degree = 3 if cp > 3 else cp - 1  # Degree of b-spline. 3 is recommended.
                                  # Must be less than number of control points.
tck, u = scipy.interpolate.splprep([x, y], k=degree)
u = scipy.linspace(0, 1, num=max(pyautogui.size()))
points = scipy.interpolate.splev(u, tck)

time.sleep(5)
# print(pyautogui.position())

# pyautogui.click()
# time.sleep(1)
# pyautogui.mouseDown()

# Move mouse.
duration = 0.1
timeout = duration / len(points[0])
for point in zip(*(i.astype(int) for i in points)):
    # pyautogui.platformModule._moveTo(*point)
    pyautogui.moveTo(*point)
    print(point)
    time.sleep(timeout)

# pyautogui.mouseUp()