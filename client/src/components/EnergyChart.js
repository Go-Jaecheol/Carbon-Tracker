import React, { useRef, useEffect, useState } from 'react';
import styled from 'styled-components';

import createEnergyChart from '../utils/createEnergyChart';
import updateEnergyChart from '../utils/updateEnergyChart';

const ChartWrapper = styled.div`
  width: 800px;
  height: 500px;
  padding: 15px 10px;
  background: white;
  box-shadow: 2px 4px 16px rgb(0 0 0 / 7%);
  border-radius: 15px;
`;

const ChartHeader = styled.div`
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
`;

const ChartSelect = styled.select`
  margin-left: 20px;
  margin-bottom: 10px;
`;

const ChartLegendWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-right: 20px;
`;

const ChartLegendItem = styled.div`
  margin-left: 30px;
  ${({ color }) => `& span { color: ${color} }`}
`;

const chartMargin = { top: 40, right: 40, bottom: 20, left: 180 };
const energyNames = ['carbonEnergy', 'helect', 'hgas', 'hwaterCool'];
const legends = [
  { label: '탄소', color: '#FB6B6B', unit: 'kgCO2eq' },
  { label: '전기', color: '#6956E5', unit: 'kWh' },
  { label: '가스', color: '#56E564', unit: 'm³' },
  { label: '수도', color: '#E556C6', unit: 'm³' },
];

export default function EnergyChart({ energyData, invalidData }) {
  const ref = useRef(null);
  const [chartItems, setChartItems] = useState(null);
  const [chartType, setChartType] = useState(-1);

  // 차트 생성
  useEffect(() => {
    const excludedEnergys = [];
    // 유효하지 않은 에너지 제외
    for (const key of invalidData.keys()) {
      const targetIdx = energyNames.indexOf(key);
      excludedEnergys.push(legends[targetIdx].label);
      energyNames.splice(targetIdx, 1);
      legends.splice(targetIdx, 1);
    }

    if (excludedEnergys.length) {
      alert(excludedEnergys.join(',') + ' 사용량 데이터가 없습니다.');
    }

    setChartItems(
      createEnergyChart(energyData, energyNames, ref.current, chartMargin)
    );
  }, [energyData, invalidData]);

  // 차트 업데이트
  useEffect(() => {
    if (!chartItems) return;

    const oneChartItem = {};

    if (chartType > -1) {
      const { yItems, lineItems, axises } = chartItems;
      oneChartItem.yItems = [yItems[chartType]];
      oneChartItem.lineItems = [lineItems[chartType]];
      oneChartItem.axises = [axises[0], axises[chartType + 1]];
    }

    updateEnergyChart(
      energyData,
      ref.current,
      chartType === -1 ? chartItems : oneChartItem,
      chartType === -1 ? legends : [legends[chartType]],
      chartMargin
    );
  }, [energyData, chartItems, chartType]);

  const handleSelectChange = (e) => {
    setChartType(+e.target.value);
  };

  return (
    <ChartWrapper ref={ref}>
      <ChartHeader>
        <ChartSelect onChange={handleSelectChange}>
          <option value='-1'>전체</option>
          {legends.map(
            (legend, i) =>
              legend && (
                <option key={legend.label} value={i}>
                  {i ? `${legend.label} 사용량` : `${legend.label} 배출량`}
                </option>
              )
          )}
        </ChartSelect>
        <ChartLegendWrapper>
          {legends.map((legend, i) =>
            legend && (
              <ChartLegendItem key={legend.label} color={legend.color}>
                <span>●</span> {legend.label} {i ? '사용량' : '배출량'}{' '}
                <span>{legend.unit}</span>
              </ChartLegendItem>
            )
          )}
        </ChartLegendWrapper>
      </ChartHeader>
    </ChartWrapper>
  );
}
