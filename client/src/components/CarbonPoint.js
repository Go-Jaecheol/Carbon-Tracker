import React, { useEffect, useState } from "react";
import styled from "styled-components";

import { getCurrentDate } from "../utils/getCurrentDate";

const Wrapper = styled.div`
  background-color: #E5FFF7;
  box-shadow: 2px 4px 2px rgb(0 0 0 / 7%);
  border-radius: 7px;
  padding: 15px;
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
`

const Title = styled.div`
  font-size: 1.3em;
  font-weight: 700;
`

const Point = styled.div`
  font-size: 1.1em;
  font-weight: 700;
  color: #FB6B6B;
`

const ReductionRate = styled.div`
  display: flex;
  align-items: center;
`

const DownArrow = styled.span`
  color: #06AA8D;
  font-size: 1.5em;
  font-weight: 700;
`

export default function CarbonPoint({ carbonPoint }) {
  const [currMonth, setCurrMonth] = useState(null);

  useEffect(() => {
    const currDate = getCurrentDate();
    setCurrMonth(+currDate.slice(4));
  }, [])

  return (
    <Wrapper height={200}>
      <Title>{currMonth < 7 ? '상반기' : '하반기'} 예상 탄소 포인트</Title>
      <Point>{carbonPoint['예상 탄소 포인트']} P</Point>
      <ReductionRate>전년 대비 전기 사용량 {carbonPoint['전기 에너지 감축률']}% 
        <DownArrow> ↓</DownArrow>
      </ReductionRate>
      <ReductionRate>전년 대비 가스 사용량 {carbonPoint['가스 에너지 감축률']}% 
        <DownArrow> ↓</DownArrow>
      </ReductionRate>
      <ReductionRate>전년 대비 수도 사용량 {carbonPoint['수도 에너지 감축률']}% 
        <DownArrow> ↓</DownArrow>
      </ReductionRate>
    </Wrapper>
  )
}