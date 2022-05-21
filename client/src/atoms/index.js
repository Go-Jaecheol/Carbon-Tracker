import { atom, selector } from "recoil";
import { getHousingInformation } from "../controllers/map";

const loadHousingCache = async () => {
    let data = localStorage.getItem("housing_info");
    if(!data) {
        data = await getHousingInformation();
        localStorage.setItem("housing_info", JSON.stringify(data));
    }
    else {
        data = JSON.parse(data);
    }
    return data;
}

export const housingState = atom({
    key: "housingState",
    default: selector({
        key: "housingState/Default",
        get: () => loadHousingCache()
    })
});