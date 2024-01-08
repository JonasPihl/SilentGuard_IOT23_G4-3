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

        # Construct the message
        message = messaging.Message(
            data={},
            notification=messaging.Notification(
                title='Visitor Detected',
                body="We have detected a visitor at your door"
            ),
            token=registration_token,
        )
        # Send the message
        response = messaging.send(message)
        print(response)
    except Exception as e:
        return "failed"


def signal_handler(signum, frame):
    exit()


def generate_frames():
    camera = cv2.VideoCapture(0)  # Use the correct camera index or video file path
    while True:
        success, frame = camera.read()
        if not success:
            break
        ret, buffer = cv2.imencode('.jpg', frame)
        if not ret:
            break
        frame = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')


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

    registration_token = root.find('registratation_token').text


def face_detection_loop():
    global logged, visitor_detected, last_detected, face_cascade
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
                    print("call Hue")
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
    cred = credentials.Certificate(CERTIFICATE HERE)
    firebase_admin.initialize_app(cred)

    #sets from assets.xml
    set_color()
    set_time()
    set_registration_token()

    face_detection_loop()
