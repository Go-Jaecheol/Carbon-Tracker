import * as d3 from 'd3';

export default function updateEnergyChart(data, currElement, chartItems, legend, margin) {
  const { yItems, lineItems, axises } = chartItems;
  const isChartOne = yItems.length === 1;

  const width = currElement.offsetWidth;
  const height = currElement.offsetHeight - margin.top - margin.bottom;
  
  // svg 추가 및 viewBox 지정
  const svgElement = d3
    .select(currElement)
    .call((g) => g.select('svg').remove()) 
    .append('svg') 
    .attr('viewBox', `0, 0, ${width * 1.05}, ${height}`);

  // 차트가 하나일 시 y축 axise 위치 수정
  if (isChartOne) {
    axises[1] = (g) => g.attr(
      'transform', `translate(${margin.left}, 0)`
    ).call( d3.axisLeft(yItems[0]) );
  }

  // axise 추가
  axises.forEach(axis => {
    svgElement.append('g')
      .call(axis)
      .attr('font-size', '0.7em');
  });

  // label 추가
  legend.forEach(({ label }, i) => {
    svgElement.append('text')
      .attr('transform', `translate(${
        !isChartOne && !i // 탄소 label 위치 결정
          ? width : isChartOne 
            ? margin.left / 3 * (3 - i)
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
      .datum(data)
      .attr('fill', 'none')
      .attr('stroke', legend[i].color)
      .attr('stroke-width', 2)
      .attr('stroke-linejoin', 'round')
      .attr('stroke-linecap', 'round')
      .attr('d', line(data));
  });
}