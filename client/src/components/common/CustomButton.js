import React from "react";
import styled from "styled-components";

const Button = styled.button`
  width: 35px;
  height: 35px;
  border: none;
  border-radius: 100%;
  background: none;
  font-size: 1.3em;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  &:hover {
    background: #d3d3d375;
  }
`;

export default function CustomButton({ icon, action }) {
  return <Button onClick={action}>{icon}</Button>
}