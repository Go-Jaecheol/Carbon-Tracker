import React, { useRef, useEffect, useState } from 'react';
import * as d3 from 'd3';
import styled from 'styled-components';

import { getHousingEnergyUsage } from '../api/index';
import processEnergyData from '../utils/processEnergyData';

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
`

const ChartSelect = styled.select`
  margin-left: 20px;
  margin-bottom: 10px;
`;

const ChartLegendWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
`;

const ChartLegendItem = styled.div`
  margin-right: 40px;
  ${({ color }) => (`& span { color: ${color} }`)}
`;

const KR_DateFormat_URL = 
  'https://cdn.jsdelivr.net/npm/d3-time-format@3/locale/ko-KR.json';

const Legent_Labels = [
  { label: '탄소 배출량', color: '#FB6B6B'},
  { label: '전기 사용량', color: '#6956E5' },
  { label: '가스 사용량', color: '#56E564' },
  { label: '수도 사용량', color: '#E556C6' },
];

export default function EnergyChart() {
  const ref = useRef(null);
  const [energyData, setEnerygyData] = useState(null);
  const [chartType, setChartType] = useState(0);

  useEffect(() => {
    let dateParse;
    // d3 한국식 날짜 설정
    const formatDateLocale = async () => {
      const locale = await d3.json(KR_DateFormat_URL);
      d3.timeFormatDefaultLocale(locale);
      dateParse = d3.timeParse('%Y%m');
    }
    // 에너지 데이터 요청
    const requestEnergyData = async () => {
      const response = await getHousingEnergyUsage('A70283310');
      setEnerygyData(processEnergyData(response, dateParse));
    }

    formatDateLocale();
    requestEnergyData();
  }, []);

  useEffect(() => {
    if (!energyData) {
      return;
    }

    const currentElement = ref.current;
    const margin = { top: 40, right: 40, bottom: 20, left: 180 };
    const width = currentElement.offsetWidth;
    const height = currentElement.offsetHeight - margin.top - margin.bottom;

    if (chartType) {
      margin.right = 0;
      margin.left = margin.left / 3 + 20;
    }

    // x축, y축 정의
    const x = d3.scaleTime().range([margin.left, width - margin.right]);
    const elecY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
    const gasY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
    const waterY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
    const carbonY = d3.scaleLinear().range([height - margin.bottom, margin.top]);

    x.domain(d3.extent(energyData, (data) => data.date));
    carbonY.domain([0, d3.max(energyData, (data) => data.carbon)]).nice();
    elecY.domain([0, d3.max(energyData, (data) => data.helect)]).nice();
    gasY.domain([0, d3.max(energyData, (data) => data.hgas)]).nice();
    waterY.domain([0, d3.max(energyData, (data) => data.hwaterCool)]).nice();

    // Line generator 정의
    const carbonLine = d3.line()
      .curve(d3.curveBasis)
      .x((data) => x(data.date))
      .y((data) => carbonY(data.carbon));

    const elecLine = d3.line()
      .curve(d3.curveBasis)
      .x((data) => x(data.date))
      .y((data) => elecY(data.helect));

    const gasLine = d3.line()
      .curve(d3.curveBasis) 
      .x((data) => x(data.date))
      .y((data) => gasY(data.hgas));

    const waterLine = d3.line()
      .curve(d3.curveBasis)
      .x((data) => x(data.date))
      .y((data) => waterY(data.hwaterCool));

    // Axis 정의
    const xAxis = (g) => {
      g.attr('transform', `translate(0, ${height - margin.bottom})`).call(
        d3.axisBottom(x).ticks(width / 24)
      );
    };

    const carbonAxis = (g) => {
      g.attr('transform', `translate(${width - margin.right}, 0)`).call(
        d3.axisRight(carbonY)
      );
    }

    const elecAxis = (g) => {
      g.attr('transform', `translate(${margin.left}, 0)`).call(
        d3.axisLeft(elecY)
      );
    };
    
    const gasAxis = (g) => {
      g.attr('transform', `translate(${margin.left / 3 * 2}, 0)`).call(
        d3.axisLeft(gasY)
      );
    };

    const waterAxis = (g) => {
      g.attr('transform', `translate(${margin.left / 3}, 0)`).call(
        d3.axisLeft(waterY)
      );
    };

    // 차트 업데이트
    const updateChart = (axises, labels, lineItems) => {
      // svg 추가 및 viewBox 지정
      const svgElement = d3
        .select(currentElement)
        .call((g) => g.select('svg').remove()) 
        .append('svg') 
        .attr('viewBox', `0, 0, ${width * 1.05}, ${height}`)
      
      // 차트가 하나일 시 y축 axise 위치 수정
      if (chartType) {
        axises[1] = (g) => {
          g.attr('transform', `translate(${margin.left}, 0)`).call(
            d3.axisLeft(yItems[chartType - 1])
          );
        };
      }

      // axise 추가
      axises.forEach(axis => {
        svgElement.append('g')
          .call(axis)
          .attr('font-size', '0.7em');
      });

      // label 추가
      labels.forEach((label, i) => {
        svgElement.append('text')
        .attr('transform', `translate(${
          !chartType && !i 
            ? width 
            : margin.left / 3 * (4 - i)
          }, ${margin.top / 3})`)
        .style('text-anchor', 'end')
        .attr('font-size', '1em')
        .attr('font-weight', 700)
        .text(label);
      });

      // line chart 추가
      lineItems.forEach((line, i) => {
        svgElement.append('path')
          .datum(energyData)
          .attr('fill', 'none')
          .attr('stroke', !chartType 
            ? Legent_Labels[i].color 
            : Legent_Labels[chartType - 1].color)
          .attr('stroke-width', 2)
          .attr('stroke-linejoin', 'round')
          .attr('stroke-linecap', 'round')
          .attr('d', line(energyData));
      });
    }

    const yItems = [carbonY, elecY, gasY, waterY];
    const axises = [xAxis, carbonAxis, elecAxis, gasAxis, waterAxis];
    const lineItems = [carbonLine, elecLine, gasLine, waterLine];
    const labels = ['탄소', '전기', '가스', '수도'];

    if (!chartType) {
      updateChart(axises, labels, lineItems);
    } else {
      updateChart(
        [axises[0], axises[chartType]], 
        [labels[chartType - 1]], 
        [lineItems[chartType - 1]
      ]);
    }
  }, [energyData, chartType]);

  const handleSelectChange = (e) => {
    setChartType(+e.target.value);
  }

  return (
    <ChartWrapper ref={ref}>
      <ChartHeader>
        <ChartSelect onChange={handleSelectChange}>
          <option value="0">전체</option>
          <option value="1">탄소 배출량</option>
          <option value="2">전기 사용량</option>
          <option value="3">가스 사용량</option>
          <option value="4">수도 사용량</option>
        </ChartSelect>
        <ChartLegendWrapper>
          {Legent_Labels.map(({ label, color }) => (
            <ChartLegendItem key={label} color={color}>
              <span>●</span> {label}
            </ChartLegendItem>
          ))}
        </ChartLegendWrapper>
      </ChartHeader>
    </ChartWrapper>
  );
}
