import os
import subprocess
from flask import Flask, jsonify, request, send_from_directory
import cv2
from flask import Flask, Response, jsonify, request, send_from_directory
from flask_socketio import SocketIO

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


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
