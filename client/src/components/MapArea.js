import React, { useEffect, useState } from "react";
import { useRecoilState } from "recoil";
import { Map, MapMarker, MarkerClusterer, useMap } from "react-kakao-maps-sdk";

import { housingState } from "../atoms";
import { getHousingInformation } from "../controllers/map";

const defaultPosition = {
  center: { lat: 35.855, lng: 128.56 },
  level: 7
}

export default function MapArea() {
  const [housingInformation, setHousingInformation] = useRecoilState(housingState);

  useEffect(() => {
    async function setup() {
      if(housingInformation.length) return;
      const data = await getHousingInformation();
      setHousingInformation([...data]);
    }
    setup();
  });

  const EventMarkerContainer = ({ housing }) => {
    const { Ma, La, kaptName } = housing;
    const map = useMap();
    const [visible, setVisible] = useState(false);

    return (
      <MapMarker
        position={{ lat: Ma, lng: La }}
        onClick={(marker) => map.panTo(marker.getPosition())}
        onMouseOver={() => setVisible(true)}
        onMouseOut={() => setVisible(false)}
      >
        {visible && (<div>{kaptName}</div>)}
      </MapMarker>
    )
  }
 
  return (
    <Map
      center={defaultPosition.center}
      style={{ width: "100%", height: "100vh" }}
      level={defaultPosition.level}
    >
      <MarkerClusterer
        averageCenter={true}
        minLevel={5}
      >
        {housingInformation.map(housing => (
          <EventMarkerContainer 
            key={housing.kaptCode} 
            housing={housing}  
          />
        ))}
      </MarkerClusterer>
    </Map>
  );
}
