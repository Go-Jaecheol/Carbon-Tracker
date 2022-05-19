import { get, post } from "../utils/http.js";

const baseURL = process.env.REACT_APP_API_BASE_URL

// GET
export const getHousingInformation = async () => await get(baseURL + "/aptListAll");

// POST
export const getHousingEnergyUsage = async (housingCode) => {
    if (energyUsageCache[housingCode]) {
        return energyUsageCache[housingCode];
    }

    const body = { code: housingCode };

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

    energyUsageCache[housingCode] = data;

    return data;
}

const energyUsageCache = {};