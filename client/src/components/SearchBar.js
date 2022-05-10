import React, { useState } from "react";
import styled from "styled-components";

const SearchBarContainer = styled.div`
    position: relative;
    span {
        position: absolute;
        left: 15px;
        top: 10px;
    }
`

const SearchInput = styled.input`
    height: 40px;
    background-color: lightgray;
    width: calc(100% - 55px);
    padding: 0 15px 0 40px;
    border: none;
    border-radius: 20px;
    font-weight: 600;
    font-size: 18px;
`

export default function SearchBar() {
    const [target, setTarget] = useState("");

    const handleChange = (event) => {
        setTarget(event.target.value);
    }

    return (
        <SearchBarContainer>
            <span>🔎</span>
            <SearchInput value={target} onChange={handleChange} />
        </SearchBarContainer>
    )
}