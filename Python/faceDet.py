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
face_cascade = cv2.CascadeClassifier('Python/facedetection.xml')

# To capture video from webcam. 

logged = False
visitor_detected = False
last_detected = 0.0
user_watching_feed = False


def send_notification():
    print(firebase_admin.credentials)

    try:
       # data = request.get_json()
        # Get the FCM registration token from the Android device
        registration_token = "dIaqJX9ARWmLANRkzKrtkh:APA91bEYxqgZ4zxc6XIMLzxh8YTKj8Pe6GKoGU98Kd8vzGYTCy5qIaDvi83b9PjSy2mMBACPT6_8QCi4EPd612CWMyLEY_FmbDed1bGFssKYYdnunQZ4BRmZh1Onwa5-wMhAxog1Cy9I"
        #registration_token = "dBkkpQw_SWmssAFVVn9xNw:APA91bFmppdKioH02MBg0wdVEFjePWLLpRaX2U5Tp_SKZTlZ8i8Z-nzyyTmipNn1rDuPqFiaJUZ0EFsN8DIHz0EbmUoYvTTCMy29BfxlkhNWRE67HkqYCt4Ivi_-ExMZkY6wbhfmbLhD"
        # Construct the message
        message = messaging.Message(
            data={'title': "blabla"},
            notification=messaging.Notification(
                title= 'Test Message',
                body= 'this is a test message',
       ),
            token=registration_token,
        )
        # Send the message
        response = messaging.send(message)
        print(response)

        return jsonify({"success": True, "response": response}), 200

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500


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
    path = 'Python/images'
    cv2.imwrite(os.path.join(path, date_string + '.jpg'), captured_image)

    # ensure that we only log the visit 1 time
    global logged
    logged = True

def on_off(status_request):
    global on_off_status
    on_off_status = status_request

def update_color():
    # read assets.xml file and save the XY values
    tree = ET.parse('Python/assets.xml')
    root = tree.getroot()

    xy_values = []
    xy_values[0] = float(root.find('x_value').text)
    xy_values[1] = float(root.find('y_value').text)

    return xy_values


def face_detection_loop():
    global on_off_status, user_watching_feed, logged, visitor_detected, last_detected, face_cascade
    cap = cv2.VideoCapture(0)

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

        if len(faces) > 0:
            last_detected = time.process_time()
            print(str(len(faces)) + " faces detected")
            visitor_detected = True
            if not logged:
                log_visitor(img)
                send_notification()

        elif visitor_detected & len(faces) == 0:
            if time.process_time() - last_detected > 7:
                visitor_detected = False
                logged = False
                # Call hue script to stop the lights here


if __name__ == '__main__':
    cred = credentials.Certificate("Python/silentguard-8402d-975a61385fb5.json")
    firebase_admin.initialize_app(cred)

    face_detection_loop()
