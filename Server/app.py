from dataclasses import field
from flask import Flask, request, Response, url_for
from flask_pymongo import PyMongo
import csv
import pandas as pd
app = Flask(__name__)
app.debug = True


app.config["MONGO_URI"] = "mongodb+srv://pospe:detection@potholedetection.ar7o8.mongodb.net/potholeDatabase?retryWrites=true&w=majority"
mongodb_client = PyMongo(app)
db = mongodb_client.db
app.i = 0


@app.route("/process_data", methods=["POST"])
def process_data():
    if "filename" not in request.form:
        return "failure"
    path = "data/"+request.form['filename']+'.csv'
    df = pd.read_csv(path)
    b = df["Az"]
    a = b.tolist()
    c = df["latitude"]
    d = df["longitude"]
    k = min(100, len(a))
    st = min(k+60, len(a))
    ms = a[60:st]
    pot = []
    sb = []
    temp = []
    lat = []
    long = []
    f = 0
    for i in range(k-1, len(a)):
        m = sum(ms)/k
        if(abs(m-a[i]) > 0.4*m):
            f = 1
            lat.append(c[i])
            long.append(d[i])
            temp.append(a[i])

        else:
            if(f):
                mt = sum(temp)/len(temp)
                lt = lat[int(len(lat)/2)]
                lg = long[int(len(long)/2)]
                el = (lt, lg)
                if(mt-m > 0):
                    sb.append(el)
                else:
                    pot.append(el)

                temp = []
                lat = []
                long = []
                f = 0
            ms.append(a[i])
            ms.pop(0)
    result = ""
    for i in pot:
        result += str(i[0])+","+str(i[1])+"\n"
    result += "\n"
    for i in sb:
        result += str(i[0])+","+str(i[1])+"\n"
    return result


@app.route("/record_data", methods=["POST"])
def record_data():
    if "filename" not in request.form:
        return "failure"
    if request.form["filename"] == "None":
        fields = ['datetime', 'latitude', 'longitude',
                  'Ax', 'Ay', 'Az', 'Gx', 'Gy', 'Gz']
        filename = "data/" + str(app.i)+".csv"
        r = []
        r.append(request.form["datetime"])
        r.append(request.form["latitude"])
        r.append(request.form["longitude"])
        r.append(request.form["Ax"])
        r.append(request.form["Ay"])
        r.append(request.form["Az"])
        r.append(request.form["Gx"])
        r.append(request.form["Gy"])
        r.append(request.form["Gz"])

        with open(filename, 'w') as csvfile:
            csvwriter = csv.writer(csvfile)
            csvwriter.writerow(fields)
            csvwriter.writerow(r)
        app.i += 1
        return str(app.i-1)
    else:
        filename = "data/"+request.form["filename"]+".csv"
        r = []
        r.append(request.form["datetime"])
        r.append(request.form["latitude"])
        r.append(request.form["longitude"])
        r.append(request.form["Ax"])
        r.append(request.form["Ay"])
        r.append(request.form["Az"])
        r.append(request.form["Gx"])
        r.append(request.form["Gy"])
        r.append(request.form["Gz"])
        with open(filename, 'a') as f_object:
            writer_object = csv.writer(f_object)
            writer_object.writerow(r)
            f_object.close()
        return str(app.i)


@ app.route("/report_pothole", methods=["POST"])
def report_pothole():
    try:
        if "latitude" not in request.form or "longitude" not in request.form:
            return "failure"
        if 'file' in request.files:
            mongodb_client.save_file(
                request.form['latitude'] + "," + request.form['longitude']+".jpg", request.files['file'])
            db.locations.insert_one(
                {'type': "pothole", 'latitude': request.form['latitude'], 'longitude': request.form['longitude'], 'file': request.form['latitude'] + "," + request.form['longitude']+".jpg"})
        else:
            db.locations.insert_one(
                {'type': "pothole", 'latitude': request.form['latitude'], 'longitude': request.form['longitude'], 'file': "None"})
        return "success"
    except:
        return "failure"


@ app.route("/report_speedbreaker", methods=["POST"])
def report_speedbreaker():
    try:
        if "latitude" not in request.form or "longitude" not in request.form:
            return "failure"
        if 'file' in request.files:
            mongodb_client.save_file(
                request.form['latitude']+"," + request.form['longitude']+".jpg", request.files['file'])
            db.locations.insert_one(
                {'type': "speedbreaker", 'latitude': request.form['latitude'], 'longitude': request.form['longitude'], 'file': request.form['latitude']+"," + request.form['longitude']+".jpg"})
        else:
            db.locations.insert_one(
                {'type': "speedbreaker", 'latitude': request.form['latitude'], 'longitude': request.form['longitude'], 'file': "None"})
        return "success"
    except:
        return "failure"


@ app.route("/get_all_potholes", methods=["GET"])
def get_all_potholes():
    try:
        loc = db.locations.find({'type': "pothole"})
        s = ""
        for i in loc:
            s += str(i['_id'])+','+i['latitude']+','+i['longitude']+"\n"
        return s
    except:
        return "failure"


@ app.route("/get_all_speedbreakers", methods=["GET"])
def get_all_speedbreakers():
    try:
        loc = db.locations.find({'type': "speedbreaker"})
        s = ""
        for i in loc:
            s += str(i['_id'])+','+i['latitude']+','+i['longitude']+"\n"
        return s
    except:
        return "failure"


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
