import React from "react";
import { Map, MapMarker } from "react-kakao-maps-sdk";

const daegu = { lat: 35.855, lng: 128.56 };

const housingSample = [
  {
    name: "대구광역시 중구 동인동1가 33-1 동인시티타운",
    lat: 35.8723582,
    lng: 128.602149,
  },
  {
    name: "대구광역시 중구 태평로3가 1 대구역센트럴자이아파트",
    lat: 35.8756692,
    lng: 128.5876,
  },
  {
    name: "대구광역시 중구 남산동 437-1 인터불고코아시스",
    lat: 35.864756,
    lng: 128.588999,
  },
];

export default function MapArea() {
  return (
    <Map
      center={{ ...daegu }}
      style={{ width: "100%", height: "100vh" }}
      level={7}
    >
      {housingSample.map(({ name, lat, lng }) => (
        <MapMarker
          key={name}
          position={{ lat, lng }}
          onClick={() => console.log(name)}
        />
      ))}
    </Map>
  );
}
