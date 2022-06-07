import React, { useState } from "react";
import styled from "styled-components";

import { getTodayEngergyUsage } from "../api";
import { getCurrentDate } from "../utils/getCurrentDate";

const Wrapper = styled.div`
  background-color: #E5FFF7;
  box-shadow: 2px 4px 2px rgb(0 0 0 / 7%);
  border-radius: 7px;
  padding: 15px;
  width: 100%;
  height: 130px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 50px;
`

const Title = styled.div`
  font-size: 1.3em;
  font-weight: 700;
`

const Carbon = styled.div`
  font-size: 1.1em;
  font-weight: 700;
  color: #FB6B6B;
`

const thirtyMonths = [1, 3, 5, 7, 8, 10, 12];
const thirtyOneMonths = [4, 6, 9, 11];

export default function TodayCarbon({ kaptdaCnt, avgWater }) {
  const [avgCarbon, setAvgCarbon] = useState(null);
  const [pineTree, setPineTree] = useState(null);
  const [loading, setLoading] = useState(true);

  useState(() => {
    const requestTodayEnergyData = async () => {
      const response = await getTodayEngergyUsage(kaptdaCnt);

      const currDate = getCurrentDate();
      const month = +currDate.slice(4);
      const year = +currDate.slice(0, 4);

      let dayCount;

      if (thirtyMonths.includes(month)) {
        dayCount = 30;
      } else if (thirtyOneMonths.includes(month)) {
        dayCount = 31;
      } else {
        dayCount = year % 4 ? 28 : 29;
      }

      const avgElec = Math.floor(response.elec_predict / dayCount);
      const avgGas = Math.floor(response.gas_predict / dayCount);
      const avgCarbon = avgElec + avgGas + Math.floor(avgWater / dayCount);
    
      setAvgCarbon(avgCarbon);
      setPineTree(Math.floor(Math.round(avgCarbon / 6.6 / 0.1) * 0.1));
      setLoading(false)
    }

    requestTodayEnergyData();
  })

  return (
    <Wrapper>
      <Title>오늘 예상 탄소 배출량</Title>
      {loading
        ?
        <div>계산중...</div>
        : 
        <>
          <Carbon>{avgCarbon} kgCO2eq</Carbon>
          <div>
            <span style={{fontSize: '2em'}}>🌲</span> 소나무 {pineTree}그루
          </div>
        </>
      }
     
    </Wrapper>
  )
}