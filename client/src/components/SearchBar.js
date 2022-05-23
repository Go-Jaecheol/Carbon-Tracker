import React, { useState } from "react";
import { useRecoilValue } from "recoil";
import styled from "styled-components";

import { housingState, mapState } from "../atoms";
import findMatches from "../utils/findMatches";
import { getKakaoLatLng } from "../utils/kakao";

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
    &:focus {
        background-color: whitesmoke;
    }
`

let pending = null;
const { kakao } = window;

export default function SearchBar() {
    const [target, setTarget] = useState("");
    const [suggestion, setSuggestion] = useState([]);
    const housingInformation = useRecoilValue(housingState);
    const map = useRecoilValue(mapState);

    const handleChange = (event) => {
        setTarget(event.target.value);
        if(pending !== null) clearTimeout(pending);
        pending = setTimeout(() => {
            setSuggestion([...findMatches(event.target.value, housingInformation)]);
            pending = null;
        }, 500);
    }

    const handleClick = (info) => {
        setTarget(info.bjdJuso);
        setSuggestion([]);
        map.setLevel(2);
        map.setCenter(getKakaoLatLng(info.Ma, info.La));
    }

    return (
        <SearchBarContainer>
            <span>🔎</span>
            <SearchInput 
                value={target} 
                style={(suggestion.length > 0 ? {"borderRadius": "20px 20px 0 0", "borderBottomWidth": "0px"} : {})} 
                onChange={handleChange} 
            />
            {suggestion.length > 0 &&
            <SuggestionContainer>
                {suggestion.map(info => 
                    <Suggestion 
                        key={info.bjdJuso}
                        onClick={() => handleClick(info)}
                    >
                    {info.bjdJuso}
                    </Suggestion>)
                }
            </SuggestionContainer>
            }
        </SearchBarContainer>
    )
}