import os
import subprocess
from flask import Flask, jsonify, request, send_from_directory
import cv2
from flask import Flask, Response, jsonify, request, send_from_directory
from flask_socketio import SocketIO
import xml.etree.ElementTree as ET

app = Flask(__name__)
process = None
running = False
socketio = SocketIO(app)

@socketio.on('message')
def send_notification(message):
    socketio.emit('message', message)

@socketio.on('/connect')
def test_connect():
    print("Client connected")
    
@app.route('/state_of_server')
def state_of_server():
    return jsonify(running)

@app.route('/get_image_list')
def get_image_list():
    image_files = [f for f in os.listdir("images") if f.endswith(".jpg")]
    return jsonify({"image_list": image_files})

@app.route('/images/<path:filename>')
def serve_image(filename):
    return send_from_directory("images", filename)

@app.route('/video')
def get_feed():
    global process,running

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

def update_color():
    # turn off faceDet
    on_off(0, 0)
    # wait for daceDet.py to stop
    # todo insert delay here

    # read color.xml file and save the XY values
    tree = ET.parse('color.xml')
    root = tree.getroot()
    x = float(root.find('x_value').text)
    y = float(root.find('y_value').text)

    # start up faceDet
    on_off(x, y)


@app.route('/on_off', methods=['POST'])
def on_off(x, y):
    global process, running
    try:
        status_request = request.args.get('value')
        if status_request is not None:
            if status_request == "true":
                running = True

                #Convert int values to strings for sending to subprocess
                x_str = str(x)
                y_str = str(y)

                process = subprocess.Popen(["python3", "faceDet.py"], stdin=subprocess.PIPE)
                process.communicate(input=f"{x_str}\n{y_str}".encode())
            else:
                running = False
                process.terminate()
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
