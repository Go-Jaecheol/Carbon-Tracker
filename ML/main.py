import json

from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib
import requests
from datetime import datetime, timedelta
from os import environ
from dotenv.main import load_dotenv


app = FastAPI()
load_dotenv()


class ElecItem(BaseModel):
    household: int
    avg_temp: float
    max_temp: float
    min_temp: float
    avg_humid: float


class GasItem(BaseModel):
    household: int
    avg_temp: float
    max_temp: float
    min_temp: float
    avg_humid: float
    avg_wind: float


@app.get("/forecast")
async def forecast():
    now = datetime.now()
    base_time = now.hour - 1
    if base_time < 3:
        now += timedelta(days=-1)
        base_time = '2300'
    else:
        base_time = '0200'
    base_date = ''.join(str(now.date()).split('-'))
    url = 'http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst'
    params = {'serviceKey': environ['FCST_SERVICE_KEY'], 'pageNo': '1', 'numOfRows': '500', 'dataType': 'JSON', 'base_date': base_date,
              'base_time': base_time, 'nx': '89', 'ny': '90'}

    response = requests.get(url, params=params)

    tmp, tmn, tmx, reh, wsd = 0, 0, 0, 0, 0
    json_object = json.loads(response.content)
    json_array = json_object.get("response").get("body").get("items").get("item")
    for l in json_array:
        if l.get("category") == 'TMN': tmn = float(l.get("fcstValue"))
        elif l.get("category") == 'TMX': tmx = float(l.get("fcstValue"))
        elif l.get("category") == 'REH':
            reh = float(l.get("fcstValue"))
        elif l.get("category") == 'WSD':
            wsd = float(l.get("fcstValue"))
    tmp = (tmn + tmx) / 2
    result = [tmp, tmx, tmn, reh, wsd]
    return {'avg_temp': tmp, 'max_temp': tmx, 'min_temp': tmn, 'avg_humid': reh, 'avg_wind': wsd}


@app.post("/predict/elec")
async def elec_predict(item: ElecItem):
    model = joblib.load('./elec_model.pkl')
    item_dict = item.dict()
    df = pd.DataFrame.from_dict([item_dict])
    prediction = model.predict(df)
    return {'predict': int(prediction[0])}


@app.post("/predict/gas")
async def gas_predict(item: GasItem):
    model = joblib.load('./gas_model.pkl')
    item_dict = item.dict()
    df = pd.DataFrame.from_dict([item_dict])
    prediction = model.predict(df)
    return {'predict': int(prediction[0])}