import cv2
import os
import time

# Load the cascade for face detection
face_cascade = cv2.CascadeClassifier('facedetection.xml')

# To capture video from webcam. 
cap = cv2.VideoCapture(0)
logged = False


def log_visitor(captured_image):
    date_to_save = time.localtime()
    year = date_to_save[0]
    month = date_to_save[1]
    day = date_to_save[2]
    date_string = str(day) + "-" + str(month) + "-"+ str(year) + "(" + str(date_to_save[3]) + ":" + str(date_to_save[4]) + ")"
    print(date_string)

    global logged
    logged = True
    path = '/Users/jonas/Documents/testdir'
    cv2.imwrite(os.path.join(path, date_string + '.jpg'), captured_image)

while True:
    # Read the frame
    _, img = cap.read()
    # Convert to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Detect the faces
    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    if len(faces) > 0:
        if not logged:
            log_visitor(img)
        #Call for hue script here


