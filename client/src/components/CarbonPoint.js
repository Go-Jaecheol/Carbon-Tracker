import React, { useEffect, useState } from "react";
import styled from "styled-components";

import { getCurrentDate } from "../utils/getCurrentDate";

const PointWrapper = styled.div`
  background-color: #E5FFF7;
  box-shadow: 2px 4px 2px rgb(0 0 0 / 7%);
  border-radius: 7px;
  padding: 15px;
  width: 100%;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
`

const PointTitle = styled.div`
  font-size: 1.1em;
  font-weight: 700;
`

export default function CarbonPoint({ carbonPoint }) {
  const [currMonth, setCurrMonth] = useState(null);

  useEffect(() => {
    const currDate = getCurrentDate();
    setCurrMonth(+currDate.slice(4));
  }, [])

  return (
    <PointWrapper>
      <PointTitle>{currMonth < 7 ? '상반기' : '하반기'} 예상 탄소 배출량</PointTitle>
      <div>{carbonPoint} P</div>
      <div>전년 대비</div>
    </PointWrapper>
  )
}