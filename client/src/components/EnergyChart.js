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
`;

const ChartLegendItem = styled.div`
  margin-left: 40px;
  ${({ color }) => `& span { color: ${color} }`}
`;

const legend = [
  { label: '탄소', color: '#FB6B6B', unit: 'kgCO2eq' },
  { label: '전기', color: '#6956E5', unit: 'kWh' },
  { label: '가스', color: '#56E564', unit: 'm³' },
  { label: '수도', color: '#E556C6', unit: 'm³' },
];

const chartMargin = { top: 40, right: 40, bottom: 20, left: 180 };

export default function EnergyChart({ energyData }) {
  const ref = useRef(null);
  const [chartItems, setChartItems] = useState(null);
  const [chartType, setChartType] = useState(-1);

  // 차트 생성
  useEffect(() => {
    if (!energyData) return;

    setChartItems(createEnergyChart(energyData, ref.current, chartMargin));
  }, [energyData]);

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
      chartType === -1 ? legend : [legend[chartType]],
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
          <option value='0'>탄소 배출량</option>
          <option value='1'>전기 사용량</option>
          <option value='2'>가스 사용량</option>
          <option value='3'>수도 사용량</option>
        </ChartSelect>
        <ChartLegendWrapper>
          {legend.map(({ label, color, unit }, i) => (
            <ChartLegendItem key={label} color={color}>
              <span>●</span> {label} {i ? '사용량' : '배출량'}{' '}
              <span>{unit}</span>
            </ChartLegendItem>
          ))}
        </ChartLegendWrapper>
      </ChartHeader>
    </ChartWrapper>
  );
}
