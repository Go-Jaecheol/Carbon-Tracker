from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

app = FastAPI()


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