import cv2
import os
import time
from flask import Flask, jsonify, send_file, send_from_directory

# Load the cascade for face detection
face_cascade = cv2.CascadeClassifier('facedetection.xml')

# To capture video from webcam. 
cap = cv2.VideoCapture(0)
logged = False
visitor_detected = False
last_detected = 0.0


def get_videostream():
    return cap


def log_visitor(captured_image):

    # Local time and save it as a string with DD-MM-YYYY(H:M) Format
    date_to_save = time.localtime()
    date_string = str(date_to_save[2]) + "-" + str(date_to_save[1]) + "-" + str(date_to_save[0]) + "(" + str(
        date_to_save[3]) + ":" + str(date_to_save[4]) + ")"

    # Saves the image to the selected path
    path = 'images'
    cv2.imwrite(os.path.join(path, date_string + '.jpg'), captured_image)

    # ensure that we only log the visit 1 time
    global logged
    logged = True


while True:
    # Read the frame
    _, img = cap.read()
    # Convert to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Detect the faces
    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    if len(faces) > 0:
        last_detected = time.process_time()

        visitor_detected = True
        if not logged:
            log_visitor(img)
        # Call hue script to start the lights here

    elif visitor_detected & len(faces) == 0:
        if time.process_time() - last_detected > 7:
            visitor_detected = False
            logged = False
            # Call hue script to stop the lights here
