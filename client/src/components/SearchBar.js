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
    background-color: white;
    max-width: 700px;
    width: calc(100% - 55px);
    padding: 0 15px 0 40px;
    border: solid 1px;
    border-radius: 20px;
    font-weight: 600;
    font-size: 18px;
    &:focus {
        outline: none;
    }
`

const SuggestionContainer = styled.div`
    position: absolute;
    background-color: white;
    max-width: 755px;
    width: calc(100%);
    padding: 0 0 13px 0;
    border: solid;
    border-width: 0px 1px 1px;
    border-radius: 0 0 15px 15px;
`

const Suggestion = styled.div`
    padding: 10px 0 10px 40px;
    &:hover{
        background-color: whitesmoke;
    }
`

let pending = null;

export default function SearchBar() {
    const [target, setTarget] = useState("");
    const [suggestion, setSuggestion] = useState([]);
    const [focused, setFocused] = useState(false);

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
            <SearchInput 
                value={target} 
                style={(suggestion.length > 0 && focused ? {"borderRadius": "20px 20px 0 0", "borderBottomWidth": "0px"} : {})} 
                onChange={handleChange} 
                onFocus={() => setFocused(true)}
                onBlur={() => setFocused(false)}
            />
            {suggestion.length > 0 && focused &&
            <SuggestionContainer>
                {suggestion.map(name => <Suggestion key={name}>{name}</Suggestion>)}
            </SuggestionContainer>
            }
        </SearchBarContainer>
    )
}