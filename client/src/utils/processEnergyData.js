const SIZE = 24;
const MID = Math.floor(SIZE / 2);

export default function getProcessedEnergyData(data, dateParse) {
  const queue = [MID];
  while (queue.length) {
    processEnergyData(data, dateParse, queue);
  }
  return data;
}

function processEnergyData(data, dateParse, queue) {
  const idx = queue.shift();
  
  for (const key of ['helect', 'hgas', 'hwaterCool']) {
    const value = +data[idx][key];
    data[idx][key] = value ? value : replaceEmptyData(data, key, idx);
  }

  data[idx].date = dateParse(data[idx].date);
  data[idx].hgas *= 0.09;
  data[idx].carbonEnergy = getCarbonData(data[idx]);

  if (0 < idx && idx <= MID) {
    queue.push(idx - 1);
  }

  if (MID <= idx && idx < SIZE - 1) {
    queue.push(idx + 1);
  }
}

function replaceEmptyData(data, key, idx) {
  let prev = idx;
  let after = idx;

  while (0 <= prev && prev <= SIZE - 1 && !+data[prev][key]) {
    prev--;
  }

  while (0 <= after && after <= SIZE - 1 && !+data[after][key]) {
    after++;
  }

  return selectReplaceData(data, key, idx, prev, after);
}

function selectReplaceData(data, key, idx, prev, after) {
  const prevGap = prev < 0 ? Infinity : idx - prev;
  const afterGap = after >= SIZE ? Infinity : after - idx;

  // 이전, 이후 달 모두 4개월 이상 격차 발생
  if (afterGap > 3 && prevGap > 3) {
    if (prevGap < afterGap) {
      return +data[prev][key];
    }
    if (prevGap > afterGap) {
      return +data[after][key];
    }
  }
  // 이전 달이 4개월 이상 격차 발생
  else if (prevGap > 3) {
    return +data[after][key];
  }
  // 이후 달이 4개월 이상 격차 발생
  else if (afterGap > 3) {
    return +data[prev][key];
  }

  // 이전, 이후 달의 격차가 같거나 모두 3개월 이하 격차 발생
  return Math.floor((+data[prev][key] + +data[after][key]) / 2);
}

function getCarbonData({ helect, hgas, hwaterCool }) {
  return Math.floor(helect * 0.4663 + hgas * 2.22 + hwaterCool * 0.332);
}
