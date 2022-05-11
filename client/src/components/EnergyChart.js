import React, { useRef, useEffect } from 'react';
import * as d3 from 'd3';

import { data } from './data';

export default function EnergyChart() {
  const ref = useRef(null);

  useEffect(() => {
    const margin = { top: 20, right: 20, bottom: 20, left: 200 };
    const currentElement = ref.current;
    const width = currentElement.offsetWidth;
    const height = 500;

    const documentElement = d3
      .select(currentElement)
      .call((g) => g.select('svg').remove()) 
      .append('svg') 
      .attr('viewBox', `0, 0, ${width}, ${height}`); 

    const parseDate = d3.timeParse('%Y.%m');

    const energyData = data.map(({ date, energy }) => ({
      date: parseDate(date),
      elec: energy[0],
      gas: energy[1],
      water: energy[2],
    }));

    const x = d3.scaleUtc().range([margin.left, width - margin.right]);
    const elecY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
    const gasY = d3.scaleLinear().range([height - margin.bottom, margin.top]);
    const waterY = d3.scaleLinear().range([height - margin.bottom, margin.top]);

    x.domain(d3.extent(energyData, (data) => data.date));
    elecY.domain([0, d3.max(energyData, (data) => data.elec)]).nice();
    gasY.domain([0, d3.max(energyData, (data) => data.gas)]).nice();
    waterY.domain([0, d3.max(energyData, (data) => data.water)]).nice();

    const elecLine = d3.line()
      .x((data) => x(data.date))
      .y((data) => elecY(data.elec));

    const gasLine = d3.line()
      .x((data) => x(data.date))
      .y((data) => gasY(data.gas));

    const waterLine = d3.line()
      .x((data) => x(data.date))
      .y((data) => waterY(data.water));

    const xAxis = (g) => {
      g.attr('transform', `translate(0, ${height - margin.bottom})`).call(
        d3.axisBottom(x)
          .ticks(width / 80)
          .tickSizeOuter(0)
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

    [xAxis, elecAxis, gasAxis, waterAxis].forEach(axis => {
      documentElement.append('g').call(axis);
    });

    [
      [elecLine, 'steelblue'], 
      [gasLine, 'red'], 
      [waterLine, 'green']
    ].forEach(([line, color]) => {
      documentElement.append('path')
      .datum(energyData)
      .attr('fill', 'none')
      .attr('stroke', color)
      .attr('stroke-width', 2)
      .attr('stroke-linejoin', 'round')
      .attr('stroke-linecap', 'round')
      .attr('d', line(energyData));
    });
  }, []);

  return <div ref={ref}></div>;
}
