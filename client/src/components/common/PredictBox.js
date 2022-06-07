import React from "react";
import styled from "styled-components";

const Wrapper = styled.div`
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
  ${({ height }) => `height: ${height}px`}
`

export default function PredictBox({ children, height = 130 }) {
  return (
    <Wrapper height={height}>
      {children}
    </Wrapper>
  )
}