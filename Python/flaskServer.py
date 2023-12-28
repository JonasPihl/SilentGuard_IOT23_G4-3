import os
import subprocess
from flask import Flask, jsonify, request, send_from_directory
import cv2
from flask import Flask, Response, jsonify, request, send_from_directory
from flask_socketio import SocketIO
import xml.etree.ElementTree as ET
import firebase_admin
from firebase_admin import credentials, messaging

app = Flask(__name__)
process = None
running = False


@app.route('/state_of_server')
def state_of_server():
    return jsonify(running)


@app.route('/start_stream')
def start_stream():
    global process, running
    if process is not None:
        process.terminate()
    process = subprocess.Popen([
    '/home/p3/mjpg-streamer/mjpg-streamer-experimental/mjpg_streamer',
    '-i', '/home/p3/mjpg-streamer/mjpg-streamer-experimental/input_uvc.so -r 640x480',
    '-o', '/home/p3/mjpg-streamer/mjpg-streamer-experimental/output_http.so -w ./www'])
    #TODO change the path to the mjpg-streamer folder to the correct path in the pi
    
    return jsonify({"message": "Success"})


@app.route('/stop_stream')
def stop_stream():
    global process, running
    if process is not None:
        process.terminate()
    return jsonify({"message": "Success"})


@app.route('/get_image_list')
def get_image_list():
    image_files = [f for f in os.listdir("images") if f.endswith(".jpg")]
    return jsonify({"image_list": image_files})


@app.route('/images/<path:filename>')
def serve_image(filename):
    return send_from_directory("images", filename)


@app.route('/video')
def get_feed():
    global process, running

    if running is True:
        process.terminate()
    return Response(get_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


def get_frames():
    while True:
        success, frame = cap.read()
        if not success:
            break
        else:
            _, buffer = cv2.imencode('.jpg', frame)
            frame = buffer.tobytes()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')


@app.route('/on_off', methods=['POST'])
def on_off():
    global process, running
    try:
        status_request = request.args.get('value')
        if status_request is not None:
            if status_request == "true":
                running = True
                process = subprocess.Popen(["python3", "faceDet.py"])
            else:
                running = False
                process.terminate()
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


@app.route('/send-notification')
def send_notification():
    try:
       # data = request.get_json()
        # Get the FCM registration token from the Android device
        registration_token = "dIaqJX9ARWmLANRkzKrtkh:APA91bEYxqgZ4zxc6XIMLzxh8YTKj8Pe6GKoGU98Kd8vzGYTCy5qIaDvi83b9PjSy2mMBACPT6_8QCi4EPd612CWMyLEY_FmbDed1bGFssKYYdnunQZ4BRmZh1Onwa5-wMhAxog1Cy9I"
        # Construct the message
        message = messaging.Message(
            data={
                'title': 'Test Message',
                'body': 'this is a test message',
            },
            token=registration_token,
        )
        # Send the message
        response = messaging.send(message)
        print(response)

        return jsonify({"success": True, "response": response}), 200

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500


if __name__ == '__main__':
    cred = credentials.Certificate("silentguard-8402d-975a61385fb5.json")
    firebase_admin.initialize_app(cred)
    app.run(host='0.0.0.0', port=5000,debug=True)


