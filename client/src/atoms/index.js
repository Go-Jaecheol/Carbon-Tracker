import { atom, selector } from "recoil";
import { getHousingInformation } from "../controllers/map";

export const housingState = atom({
    key: "housingState",
    default: selector({
        key: "housingState/Default",
        get: () => getHousingInformation()
    })
});