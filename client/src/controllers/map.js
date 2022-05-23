import * as API from "../api/index.js";
import { getCoordinate } from "../utils/kakao.js";

export const getHousingInformation = async () => {
    try {
        const { data } = await API.getHousingInformation();
        
        let ret = await Promise.all(data.map(housing => {
            return new Promise((resolve, reject) => {
                getCoordinate(housing.bjdJuso)
                .then(coordinate => resolve({...housing, ...coordinate}))
                .catch((err) => reject(err));
            });
        }));

        return ret.filter(elem => elem?.Ma);
    } catch (error) {
        console.log(error);
    }
};