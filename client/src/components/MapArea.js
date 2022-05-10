import React from "react";
import { Map, MapMarker } from "react-kakao-maps-sdk"

const daegu = { lat: 35.84, lng: 128.56 };

export default function MapArea() {
    return (
      <Map 
        center={{...daegu}}
        style={{width: "100%", height: "100vh"}}
        level={7}
      >
      </Map>
    );
}