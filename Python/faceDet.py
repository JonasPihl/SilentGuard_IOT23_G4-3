from flask import jsonify
import hue
import cv2
import os
import time
import signal
import xml.etree.ElementTree as ET
import firebase_admin
from firebase_admin import credentials, messaging

# Load the cascade for face detection
face_cascade = cv2.CascadeClassifier('facedetection.xml')

# To capture video from webcam. 

logged = False
visitor_detected = False
last_detected = 0.0
user_watching_feed = False
x_color = 0.0
y_color = 0.0
start_hour = 0
start_minute = 0
end_hour = 0
end_minute = 0
registration_token = None


def send_notification():

    try:
        global registration_token
        # Get the FCM registration token from the Android device
        registration_token = "dIaqJX9ARWmLANRkzKrtkh:APA91bEYxqgZ4zxc6XIMLzxh8YTKj8Pe6GKoGU98Kd8vzGYTCy5qIaDvi83b9PjSy2mMBACPT6_8QCi4EPd612CWMyLEY_FmbDed1bGFssKYYdnunQZ4BRmZh1Onwa5-wMhAxog1Cy9I"
        # registration_token = "dBkkpQw_SWmssAFVVn9xNw:APA91bFmppdKioH02MBg0wdVEFjePWLLpRaX2U5Tp_SKZTlZ8i8Z-nzyyTmipNn1rDuPqFiaJUZ0EFsN8DIHz0EbmUoYvTTCMy29BfxlkhNWRE67HkqYCt4Ivi_-ExMZkY6wbhfmbLhD"
        # Construct the message
        message = messaging.Message(
            data={},
            notification=messaging.Notification(
                title='Visitor Detected',
                body='We have detected an visitor',
            ),
            token=registration_token,
        )
        # Send the message
        response = messaging.send(message)
        print(response)
    except Exception as e:
        return "failed"


def signal_handler(signum, frame):
    # Add cleanup code if needed
    exit()


signal.signal(signal.SIGTERM, signal_handler)


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


def on_off(status_request):
    global on_off_status
    on_off_status = status_request


def set_color():
    # read assets.xml file and save the XY values
    tree = ET.parse('assets.xml')
    root = tree.getroot()

    global x_color, y_color
    x_color = float(root.find('x_value').text)
    y_color = float(root.find('y_value').text)


def set_time():
    global start_hour, start_minute, end_hour, end_minute
    tree = ET.parse('assets.xml')
    root = tree.getroot()

    start_hour = int(root.find('start_hour').text)
    start_minute = int(root.find('start_min').text)

    end_hour = int(root.find('end_hour').text)
    end_minute = int(root.find('end_min').text)


def set_registration_token():
    global registration_token
    tree = ET.parse('assets.xml')
    root = tree.getroot()

    registration_token = root


def face_detection_loop():
    global on_off_status, user_watching_feed, logged, visitor_detected, last_detected, face_cascade
    cap = cv2.VideoCapture(0)

    hue_prestate = None
    has_alerted = False
    while True:
        # Read the frame
        _, img = cap.read()
        # Convert to grayscale
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        # Detect the faces
        faces = face_cascade.detectMultiScale(gray, 1.1, 4)

        if user_watching_feed:
            # send image feed to the android application here
            print("Sending image feed to android application")
        print(visitor_detected)
        print(len(faces))

        if len(faces) > 0:
            last_detected = time.process_time()
            visitor_detected = True
            if not logged:
                print(str(len(faces)) + " faces detected")
                log_visitor(img)
                logged = True

                send_notification()
                if is_current_time_between():
                    hue_prestate = hue.get_state_of_light(1)
                    call_hue(1)
                    has_alerted = True

        elif visitor_detected and len(faces) == 0:
            if time.process_time() - last_detected > 7:
                visitor_detected = False
                logged = False
                # Call hue script to stop the lights here
                if has_alerted:
                    hue.pre_state(1, hue_prestate)


def is_current_time_between():
    if start_hour < time.localtime().tm_hour > end_hour:
        return True
    elif start_hour == time.localtime().tm_hour and start_minute < time.localtime().tm_min:
        return True
    elif end_hour == time.localtime().tm_hour and end_minute > time.localtime().tm_min:
        return True
    return False


def call_hue(id):
    hue.alarm_state(id, x_color, y_color)


if __name__ == '__main__':
    #for the notifications on android
    cred = credentials.Certificate("silentguard-8402d-975a61385fb5.json")
    firebase_admin.initialize_app(cred)

    #sets from assets.xml
    set_color()
    set_time()
    set_registration_token()

    face_detection_loop()
