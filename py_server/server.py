from flask import Flask, make_response, render_template, request
import base64
app = Flask(__name__)



def load_default_image():
    image = open("static/default.jpg", "rb")
    image_data = image.read()
    image.close()
    return image_data


app.image_data = load_default_image()


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/image.jpg", methods=["GET"])
def get_image():
    response = make_response(app.image_data)
    response.headers['Content-Type'] = "image/jpeg"
    return response

@app.route("/image.jpg", methods=["POST"])
def write_image():
    app.image_data = base64.b64decode(request.data)
    return "Ok"


if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")