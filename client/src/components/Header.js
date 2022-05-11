import React from "react";
import styled from "styled-components";

import logo from "../assets/colorful_logo.png";
import SearchBar from "./SearchBar";

const HeaderContainer = styled.div`
    position: absolute;
    display: grid;
    grid-template-columns: 1fr 2fr;
    column-gap: 15px;
    align-items: center;
    height: 70px;
    width: 80%;
    padding: 0 10%;
    background-color: white;
    z-index: 2;
`;

const Logo = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    overflow: hidden;
    user-select: none;
    img {
        height: 120px;
    }
`

const Title = styled.div`
    font-size: 20px;
    font-weight: 600;
    margin: 0 20px;
`

export default function Header() {
    return(
        <HeaderContainer>
            <Logo>
                <img src={logo} alt="컬러풀 대구 로고" />
                <Title>Carbon Tracker</Title>
            </Logo>
            <SearchBar />
        </HeaderContainer>
    )
}