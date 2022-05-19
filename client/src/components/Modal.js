import React, { useState } from 'react';
import styled from 'styled-components';

const ModalBackground = styled.div`
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(1.5px);
  -webkit-backdrop-filter: blur(1.5px);
`;

const ModalWindow = styled.div`
  width: 60%;
  height: 70%;
  justify-content: space-between;
  background: white;
  box-shadow: 2px 4px 16px rgb(0 0 0 / 16%);
  border-radius: 15px;
  padding: 25px 40px;
  ${({ isOpen }) => (
    isOpen 
    ? `display: flex` 
    : `display: none`
  )}
`;

const LeftWrapper = styled.div``;

const HousingName = styled.h2`
  margin-top: 0;
`;

const HousingAddress = styled.p``;

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

export default function Modal() {
  const [isOpen, setOpen] = useState(true);

  const handleClick = () => {
    setOpen(false);
  };

  return (
    <ModalBackground>
      <ModalWindow isOpen={isOpen}>
        <LeftWrapper>
          <HousingName>침산화성파크드림아파트</HousingName>
          <HousingAddress>대구 북구 성북로 70</HousingAddress>
          {/* 에너지 차트 */}
        </LeftWrapper>
        <RightWrapper>
          <CloseButton onClick={handleClick}>✕</CloseButton>
          {/* 현 시각 탄소 배출량 */}
          {/* 올해 예상 탄소 포인트 */}
        </RightWrapper>
      </ModalWindow>
    </ModalBackground>
  );
}
