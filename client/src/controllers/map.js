import * as API from "../api/index.js";
import getCoordinate from "../utils/kakao/getCoordinate.js";

export const getHousingInformation = async () => {
    try {
        const { data } = await API.getHousingInformation();
        
        const ret = await Promise.all(data.map(housing => {
            return new Promise((resolve, reject) => {
                getCoordinate(housing.bjdJuso)
                .then(coordinate => resolve({...housing, ...coordinate}))
                .catch((err) => reject(err));
            });
        }));

        return ret;
    } catch (error) {
        console.log(error);
    }
};