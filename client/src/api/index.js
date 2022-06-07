import { get, post } from "../utils/http.js";
import { getCurrentDate } from "../utils/getCurrentDate.js";

const baseURL = process.env.REACT_APP_API_BASE_URL;
const predictBaseURL = process.env.REACT_APP_API_ML_URL;

// GET
export const getHousingInformation = async () => await get(baseURL + "/aptListAll");

// POST
export const getHousingEnergyUsage = async (housingCode) => {
    const body = { 
        code: housingCode,
        date: getCurrentDate()
    };

    const headers = { 'Content-Type': 'application/json' };
    
    const { status, data } = await post(
        baseURL + '/aptEnergyAll', 
        body,
        headers
    );

    if (status !== 200) {
        alert('에너지 사용량 데이터 요청에 실패했습니다.')
        return null;
    }

    return data;
}

export const getTodayEngergyUsage = async (kaptdaCnt) => {
    const cache = sessionStorage.getItem(kaptdaCnt);

    if (cache){
        return JSON.parse(cache);
    }

    const body = {
        household: kaptdaCnt
    };

    const headers = { 'Content-Type': 'application/json' };

    const { status, data } = await post(
        predictBaseURL + '/predict/now', 
        body,
        headers
    );

    if (status !== 200) {
        alert('오늘 예상 에너지 사용량 데이터 요청에 실패했습니다.')
        return null;
    }
    
    sessionStorage.setItem(kaptdaCnt, JSON.stringify(data));
    return data;
}