import React, { useState } from "react";
import styled from "styled-components";

import findMatches from "../utils/findMatches";

const housingSample = [
    "대구광역시 중구 동인동1가 33-1 동인시티타운",
    "대구광역시 중구 태평로3가 1 대구역센트럴자이아파트",
    "대구광역시 중구 남산동 437-1 인터불고코아시스"
];

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
    max-width: 700px;
    width: calc(100% - 55px);
    padding: 0 15px 0 40px;
    border: none;
    border-radius: 20px;
    font-weight: 600;
    font-size: 18px;
    &:focus {
        outline: none;
    }
`

const SuggestionContainer = styled.div`
    position: absolute;
    background-color: lightgray;
    max-width: 755px;
    width: calc(100%);
    padding: 0 0 13px 0;
    border-radius: 0 0 15px 15px;
`

const Suggestion = styled.div`
    padding: 10px 0 10px 40px;
    &:hover{
        background-color: darkgray;
    }
`

let pending = null;

export default function SearchBar() {
    const [target, setTarget] = useState("");
    const [suggestion, setSuggestion] = useState([]);

    const handleChange = (event) => {
        setTarget(event.target.value);
        if(pending !== null) clearTimeout(pending);
        pending = setTimeout(() => {
            setSuggestion([...findMatches(event.target.value, housingSample)]);
            pending = null;
        }, 500);
    }

    return (
        <SearchBarContainer>
            <span>🔎</span>
            <SearchInput value={target} onChange={handleChange} style={(suggestion.length > 0 ? {"border-radius": "20px 20px 0 0"} : {})} />
            {suggestion.length > 0 &&
            <SuggestionContainer>
                {suggestion.map(name => <Suggestion>{name}</Suggestion>)}
            </SuggestionContainer>
            }
        </SearchBarContainer>
    )
}