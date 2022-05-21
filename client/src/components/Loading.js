import React from "react";
import styled from 'styled-components';

import Header from "./Header";

const LoadingContainer = styled.div`
    height: 100vh;
    animation: skeleton-gradient 1.5s infinite ease-in-out;
    @keyframes skeleton-gradient {
        0% {
            background-color: rgba(165, 165, 165, 0.1);
        }
        50% {
            background-color: rgba(165, 165, 165, 0.3);
        }
        100% {
            background-color: rgba(165, 165, 165, 0.1);
        }
    }
`

export default function Loading() {
    return (
        <LoadingContainer>
            <Header loading={true} />
        </LoadingContainer>
    )
}