import { atom } from "recoil";

export const housingState = atom({
    key: "housingState",
    default: []
});

export const mapState = atom({
    key: "matState",
    default: { lat: 35.855, lng: 128.56 }
});