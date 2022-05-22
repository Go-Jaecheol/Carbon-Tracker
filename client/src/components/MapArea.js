import React, { useState } from "react";
import { useRecoilValue } from "recoil";
import { Map, MapMarker, MarkerClusterer, useMap } from "react-kakao-maps-sdk";

import { housingState } from "../atoms";
import Modal from "./Modal";

const defaultPosition = {
  center: { lat: 35.855, lng: 128.56 },
  level: 7
}

export default function MapArea() {
  const housingInformation = useRecoilValue(housingState);

  const EventMarkerContainer = ({ housing }) => {
    const { Ma, La, kaptName } = housing;
    const map = useMap();
    const [visible, setVisible] = useState(false);
    const [isModalOpen, setModalOpen] = useState(false);

    return (
      <>
        <Modal 
          housing={housing} 
          isOpen={isModalOpen} 
          close={() => setModalOpen(false)} 
        />
        <MapMarker
          position={{ lat: Ma, lng: La }}
          onClick={(marker) => {
            map.panTo(marker.getPosition());
            setModalOpen(true);
          }}
          onMouseOver={() => setVisible(true)}
          onMouseOut={() => setVisible(false)}
        >
          {visible && (<div>{kaptName}</div>)}
        </MapMarker>
      </>
    );
  };
 
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
