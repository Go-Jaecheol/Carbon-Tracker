import React from 'react';
import styled from 'styled-components';

import EnergyChart from './EnergyChart';

const ModalBackground = styled.div`
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.3);
  ${({ isOpen }) => (
    isOpen 
    ? `display: flex` 
    : `display: none`
  )}
`;

const ModalWindow = styled.div`
  width: 80%;
  height: 80%;
  margin-top: 65px;
  display: flex;
  justify-content: space-between;
  background: white;
  box-shadow: 2px 4px 16px rgb(0 0 0 / 16%);
  border-radius: 15px;
  padding: 25px 20px;
`;

const LeftWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
`;

const HousingName = styled.h2`
  margin: 0 30px;
`;

const HousingAddress = styled.p`
  margin-left: 30px;
`

const RightWrapper = styled.div``;

const CloseButton = styled.button`
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 100%;
  background: none;
  font-size: 1.3em;
  cursor: pointer;
  &:hover {
    background: #d3d3d375;
  }
`;

export default function Modal({ housing, isOpen, close }) {
  const { kaptCode, kaptName, doroJuso } = housing;
  return (
    <ModalBackground isOpen={isOpen}>
      {isOpen && (
        <ModalWindow>
          <LeftWrapper>
            <div>
              <HousingName>{kaptName}</HousingName>
              <HousingAddress>{doroJuso}</HousingAddress>
            </div>
            <EnergyChart housingCode={kaptCode} />
          </LeftWrapper>
          <RightWrapper>
            <CloseButton onClick={close}>✕</CloseButton>
            {/* 현 시각 탄소 배출량 */}
            {/* 올해 예상 탄소 포인트 */}
          </RightWrapper>
        </ModalWindow>
      )}
    </ModalBackground>
  );
}
