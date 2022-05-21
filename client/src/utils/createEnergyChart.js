import * as d3 from 'd3';

export default function createEnergyChart(energyData, currElement, margin) {
  const energys = ['carbonEnergy', 'helect', 'hgas', 'hwaterCool'];
  const width = currElement.offsetWidth;
  const height = currElement.offsetHeight - margin.top - margin.bottom;

  const x = d3.scaleTime()
      .range([margin.left, width - margin.right])
      .domain(d3.extent(energyData, (data) => data.date));

  const yItems = energys.map((energy) => {
    return d3.scaleLinear()
      .range([height - margin.bottom, margin.top])
      .domain([0, d3.max(energyData, (data) => data[energy])]).nice();
  })

  // line 정의
  const lineItems = energys.map((energy, i) => {
    return d3.line()
      .curve(d3.curveBasis)
      .x((data) => x(data.date))
      .y((data) => yItems[i](data[energy]));
  });

  // Axis 정의
  const xAxis = (g) => g.attr(
    'transform', `translate(0, ${height - margin.bottom})`
    ).call( d3.axisBottom(x).ticks(width / 24));

  const yAxises = yItems.map((y, i) => {
    if (!i) {
      return (g) => g.attr(
        'transform', `translate(${width - margin.right}, 0)`
        ).call(d3.axisRight(y));
    }
    return (g) => g.attr(
      'transform', `translate(${margin.left / 3 * (4 - i)}, 0)`
      ).call(d3.axisLeft(y));
  });

  return {
    yItems: yItems,
    lineItems: lineItems,
    axises: [xAxis, ...yAxises]
  };
}