import React, { useEffect, useState } from "react";
import styled from "styled-components";
import { FiArrowLeft } from 'react-icons/fi';

import CustomButton from './common/CustomButton';

const TableWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
`

const HeadWrapper = styled.div`
  width: 90%;
  height: 6%;
  display: flex;
  align-items: center;
  justify-content: space-between;
`

const HousingName = styled.h2`
  margin: 0;
`;

const Table = styled.table`
  width: 90%;
  height: 90%;
  text-align: center;
  border: 1px solid lightgray;
  border-radius: 10px; 
`

const TableHead = styled.thead`
  color: #464F60;
  width: 100%;
  height: 15%;
  display: table;
  table-layout: fixed;
`

const TableBody = styled.tbody`
  color: #464F60;
  display: block;
  overflow-y: auto;
  height: 85%;
`

const TableRow = styled.tr`
  display: table;
  table-layout: fixed;
  width: 100%;
  height: 50px;
  
  ${({ type }) => `
    ${type && `background-color: #F9FAFC`}
  `};

  & > td:nth-child(1) {
    font-weight: bold;
  };
`

export default function EnergyTable({ kaptName, energyData, goBack, close }) {
  const theadItems = [
    '날짜',
    '탄소 배출량(kgCO2eq)',
    '전기 사용량(kWh)',
    '가스 사용량(m³)',
    '수도 사용량(m³)',
  ];

  const [avgEnergy, setAvgEnrgy] = useState({})

  useEffect(() => {
    const monthCount = 24;
    const sumEnergy = {
      carbonEnergy: 0, helect: 0, hgas: 0, hwaterCool: 0
    }

    energyData.forEach(data => {
      sumEnergy.carbonEnergy += data.carbonEnergy;
      sumEnergy.helect += data.helect;
      sumEnergy.hgas += data.hgas;
      sumEnergy.hwaterCool += data.hwaterCool;
    });

    const { carbonEnergy, helect, hgas, hwaterCool } = sumEnergy;

    setAvgEnrgy({
      carbonEnergy: Math.floor(carbonEnergy / monthCount),
      helect: Math.floor(helect / monthCount),
      hgas: Math.floor(hgas / monthCount),
      hwaterCool: Math.floor(hwaterCool / monthCount)
    });
  }, [energyData])

  return (
    <TableWrapper>
      <HeadWrapper>
        <CustomButton icon={<FiArrowLeft size='25' />} action={goBack}/>
        <HousingName>{kaptName}</HousingName>
        <CustomButton icon={'✕'} action={close} />
      </HeadWrapper>
      <Table>
        <TableHead>
          <tr>
            {theadItems.map((item) => (
              <th key={item}>{item}</th>
            ))}
          </tr>
        </TableHead>
        <TableBody>
          {energyData.map((data, i) => (
            <TableRow key={data.date} type={i % 2} >
              <td>{`${data.date.getFullYear()}.${data.date.getMonth() + 1}`}</td>
              <td>{data.carbonEnergy}</td>
              <td>{data.helect}</td>
              <td>{data.hgas}</td>
              <td>{data.hwaterCool}</td>
            </TableRow>
          ))}
          <TableRow style={{borderTop: '0.5px solid'}}>
            <td>평균</td>
            <td>{avgEnergy.carbonEnergy}</td>
            <td>{avgEnergy.helect}</td>
            <td>{avgEnergy.hgas}</td>
            <td>{avgEnergy.hwaterCool}</td>
          </TableRow>
        </TableBody>
      </Table>
    </TableWrapper>
  );
}
