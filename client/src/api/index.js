import { get, post } from "../utils/http.js";

const baseURL = process.env.REACT_APP_API_BASE_URL

// GET
export const getHousingInformation = async () => await get(baseURL + "/aptListAll");

// POST