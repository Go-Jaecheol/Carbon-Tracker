import React, { useRef, useEffect, useState } from 'react';
import * as d3 from 'd3';
import styled from 'styled-components';

import { getHousingEnergyUsage } from '../api/index';

import { data } from './data';

const ChartWrapper = styled.div`
  width: 80%;
  padding: 20px;
  background: white;
  box-shadow: 2px 4px 16px rgb(0 0 0 / 7%);
  border-radius: 15px;
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
  const [chartType, setChartType] = useState(0);

  useEffect(() => {
    const currentElement = ref.current;
    const margin = { top: 40, right: 20, bottom: 20, left: 200 };
    const width = currentElement.offsetWidth;
    const height = 500;

    if (chartType) {
      margin.left /= 3;
    }

    // 한국식 날짜 가져오기
    d3.json(KR_DateFormat_URL).then(locale => {
      // data 가공
      d3.timeFormatDefaultLocale(locale);
      const dateParse = d3.timeParse('%Y.%m');
    
      const energyData = data.map(({ date, energy }) => ({
        date: dateParse(date),
        elec: energy[0],
        gas: energy[1],
        water: energy[2],
      }));

      // x축, y축 정의
      const x = d3.scaleTime().range([margin.left, width - margin.right]);
      const elecY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
      const gasY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
      const waterY = d3.scaleLinear().range([height - margin.bottom, margin.top]);

      x.domain(d3.extent(energyData, (data) => data.date));
      elecY.domain([0, d3.max(energyData, (data) => data.elec)]).nice();
      gasY.domain([0, d3.max(energyData, (data) => data.gas)]).nice();
      waterY.domain([0, d3.max(energyData, (data) => data.water)]).nice();

      // Line generator 정의
      const elecLine = d3.line()
        .x((data) => x(data.date))
        .y((data) => elecY(data.elec));

      const gasLine = d3.line()
        .x((data) => x(data.date))
        .y((data) => gasY(data.gas));

      const waterLine = d3.line()
        .x((data) => x(data.date))
        .y((data) => waterY(data.water));

      // Axis 정의
      const xAxis = (g) => {
        g.attr('transform', `translate(0, ${height - margin.bottom})`).call(
          d3.axisBottom(x).ticks(width / 80)
        );
      };

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
        const documentElement = d3
          .select(currentElement)
          .call((g) => g.select('svg').remove()) 
          .append('svg') 
          .attr('viewBox', `0, 0, ${width}, ${height}`); 
        
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
          documentElement.append('g')
            .call(axis)
            .attr('font-size', '0.7em');
        });
  
        // label 추가
        labels.forEach((label, i) => {
          documentElement.append('text')
          .attr('transform', `translate(${margin.left / 3 * (3 - i)}, ${margin.top / 3})`)
          .style('text-anchor', 'end')
          .attr('font-size', '1em')
          .text(label);
        });
  
        // line chart 추가
        lineItems.forEach((line, i) => {
          documentElement.append('path')
            .datum(energyData)
            .attr('fill', 'none')
            .attr('stroke', !chartType 
              ? Legent_Labels[i + 1].color 
              : Legent_Labels[chartType].color)
            .attr('stroke-width', 4)
            .attr('stroke-linejoin', 'round')
            .attr('stroke-linecap', 'round')
            .attr('d', line(energyData));
        });
      }

      const yItems = [elecY, gasY, waterY];
      const axises = [xAxis, elecAxis, gasAxis, waterAxis];
      const lineItems = [elecLine, gasLine, waterLine];
      const labels = ['전기', '가스', '수도'];

      if (!chartType) {
        updateChart(axises, labels, lineItems);
      } else {
        updateChart(
          [axises[0], axises[chartType]], 
          [labels[chartType - 1]], 
          [lineItems[chartType - 1]
        ]);
      }
    })
  });

  const handleSelectChange = (e) => {
    setChartType(+e.target.value);
  }

  return (
    <ChartWrapper ref={ref}>
      <ChartSelect onChange={handleSelectChange}>
        <option value="0">전체</option>
        <option value="1">전기 사용량</option>
        <option value="2">가스 사용량</option>
        <option value="3">수도 사용량</option>
      </ChartSelect>
      <ChartLegendWrapper>
        {Legent_Labels.map(({ label, color }) => (
          <ChartLegendItem key={label} color={color}>
            <span>●</span> {label}
          </ChartLegendItem>
        ))}
      </ChartLegendWrapper>
    </ChartWrapper>
  );
}
