export default function processEnergyData(data, dateParse) {
  const energyData = data.map(({ date }, i) => {
    const processed = { date: dateParse(date) };

    for (const key of ['helect', 'hgas', 'hwaterCool']) {
      processed[key] = replaceEmptyData(data, key, i);
    }
    processed.carbon = getCarbonData(processed);

    return processed;
  });

  return energyData;
}

function replaceEmptyData(data, key, now) {
  if (+data[now][key]) {
    return +data[now][key];
  }

  let prev = now;
  let after = now;

  let isOverPrev = false;
  let isOverAfter = false;

  while (!+data[prev][key]) {
    prev--;
    if (prev < 0) {
      prev = data.length - 1;
    }
    if (!isOverPrev && Math.abs(prev - now) > 3) {
      isOverPrev = true;
    }
  }

  while (!+data[after][key]) {
    after++;
    if (after >= data.length) {
      after = 0;
    }
    if (!isOverAfter && Math.abs(after - now) > 3) {
      isOverAfter = true;
    }
  }

  // 하나가 4개월 이상 격차 발생 - 3개월 이내 데이터 리턴
  if (isOverPrev + isOverAfter === 1) {
    if (!isOverPrev) {
      return +data[prev][key];
    }
    return +data[after][key];
  }
  
  // 모두 4개월 이상 격차 발생 - 더 작은 격차 데이터 리턴
  if (isOverPrev + isOverAfter === 2) {
    if (isOverPrev < isOverAfter) {
      return +data[prev][key];
    }
    if (isOverPrev > isOverAfter) {
      return +data[after][key];
    }
  }

  // 이전, 이후 데이터의 평균 리턴
  return Math.floor((+data[prev][key] + +data[after][key]) / 2);
}

function getCarbonData({ helect, hgas, hwaterCool }) {
  return Math.floor(helect * 0.4663 + hgas * 2.22 + hwaterCool * 0.332);
}
