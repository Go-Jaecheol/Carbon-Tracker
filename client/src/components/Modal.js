import React, { useState, useEffect } from 'react';
import * as d3 from 'd3';
import styled from 'styled-components';

import EnergyChart from './EnergyChart';
import EnergyTable from './EnergyTable';
import CustomButton from './common/CustomButton';
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
  width: 1150px;
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

const HousingAddress = styled.div`
  padding: 10px 30px;
  display: flex;
  justify-content: space-between;
`;

const DetailButton = styled.div`
  color: gray;
  border-bottom: 1px solid white;
  cursor: pointer;
  &:hover {
    color: black;
    border-bottom: 1px solid;
  }
`;

const RightWrapper = styled.div``;

const KR_DateFormat_URL =
  'https://cdn.jsdelivr.net/npm/d3-time-format@3/locale/ko-KR.json';

export default function Modal({ housing, close }) {
  const { kaptCode, kaptName, doroJuso } = housing;
  const [energyData, setEnerygyData] = useState(null);
  const [invalidData, setInvalidData] = useState(null);
  const [isShowTable, setShowTable] = useState(false);

  useEffect(() => {
    const dateParse = d3.timeParse('%Y%m');
    // d3 한국식 날짜 설정
    const formatDateLocale = async () => {
      const locale = await d3.json(KR_DateFormat_URL);
      d3.timeFormatDefaultLocale(locale);
    };

    // 에너지 데이터 요청
    const requestEnergyData = async () => {
      const response = await getHousingEnergyUsage(kaptCode);
      const [energyData, invalidData] = processEnergyData(response, dateParse);
      setEnerygyData(energyData);
      setInvalidData(invalidData);
    };

    formatDateLocale().then(() => requestEnergyData());
  }, [kaptCode]);

  return (
    <ModalBackground>
      <ModalWindow>
        {isShowTable ? (
          energyData && (
            <EnergyTable 
              kaptName={kaptName} 
              energyData={energyData} 
              goBack={() => setShowTable(false)}
              close={close}
            />
          )
        ) : (
          <>
            <LeftWrapper>
              <div>
                <HousingName>{kaptName}</HousingName>
                <HousingAddress>
                  <div>{doroJuso}</div>
                  <DetailButton onClick={() => setShowTable(true)}>
                    자세히 보기
                  </DetailButton>
                </HousingAddress>
              </div>
              {energyData && (
                <EnergyChart
                  energyData={energyData}
                  invalidData={invalidData}
                />
              )}
            </LeftWrapper>
            <RightWrapper>
              <CustomButton icon={'✕'} action={close} />
              {/* 현 시각 탄소 배출량 */}
              {/* 올해 예상 탄소 포인트 */}
            </RightWrapper>
          </>
        )}
      </ModalWindow>
    </ModalBackground>
  );
}
