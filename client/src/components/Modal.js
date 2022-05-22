import React, { useState, useEffect } from 'react';
import * as d3 from 'd3';
import styled from 'styled-components';

import EnergyChart from './EnergyChart';
import { getHousingEnergyUsage } from '../api/index';
import processEnergyData from '../utils/processEnergyData';

const ModalBackground = styled.div`
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.3);
`;

const ModalWindow = styled.div`
  width: 80%;
  height: 80%;
  margin-top: 65px;
  display: flex;
  justify-content: space-between;
  background: white;
  box-shadow: 2px 4px 16px rgb(0 0 0 / 16%);
  border-radius: 15px;
  padding: 25px 20px;
`;

const LeftWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
`;

const HousingName = styled.h2`
  margin: 0 30px;
`;

const HousingAddress = styled.p`
  margin-left: 30px;
`;

const RightWrapper = styled.div``;

const CloseButton = styled.button`
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 100%;
  background: none;
  font-size: 1.3em;
  cursor: pointer;
  &:hover {
    background: #d3d3d375;
  }
`;

const KR_DateFormat_URL =
  'https://cdn.jsdelivr.net/npm/d3-time-format@3/locale/ko-KR.json';

export default function Modal({ housing, close }) {
  const { kaptCode, kaptName, doroJuso } = housing;
  const [energyData, setEnerygyData] = useState(null);

  useEffect(() => {
    // d3 한국식 날짜 설정
    let dateParse;
    const formatDateLocale = async () => {
      const locale = await d3.json(KR_DateFormat_URL);
      d3.timeFormatDefaultLocale(locale);
      dateParse = d3.timeParse('%Y%m');
    };

    // 에너지 데이터 요청
    const requestEnergyData = async () => {
      const response = await getHousingEnergyUsage(kaptCode);
      setEnerygyData(processEnergyData(response, dateParse));
    };

    formatDateLocale();
    requestEnergyData();
  }, [kaptCode]);

  return (
    <ModalBackground>
      <ModalWindow>
        <LeftWrapper>
          <div>
            <HousingName>{kaptName}</HousingName>
            <HousingAddress>{doroJuso}</HousingAddress>
          </div>
          <EnergyChart energyData={energyData} />
        </LeftWrapper>
        <RightWrapper>
          <CloseButton onClick={close}>✕</CloseButton>
          {/* 현 시각 탄소 배출량 */}
          {/* 올해 예상 탄소 포인트 */}
        </RightWrapper>
      </ModalWindow>
    </ModalBackground>
  );
}
