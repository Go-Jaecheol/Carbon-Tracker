import React from "react";
import styled from "styled-components";

const Wrapper = styled.div`
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const Loading = styled.div`
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: black;
  border-bottom-color: black;
  animation: spinner .8s ease infinite;
  @keyframes spinner {
  from {transform: rotate(0deg); }
  to {transform: rotate(360deg);}
}
`;

const Spinner = () => {
  return (
    <Wrapper>
      <Loading />
    </Wrapper>
  );
};

export default Spinner;
