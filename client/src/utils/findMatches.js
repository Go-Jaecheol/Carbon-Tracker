export default function findMatches(target, candidates, addressType = "bjdJuso") {
    const matched = [];
    if(!target) return matched;

    for(let candidate of candidates) {
        if(target.length > candidate[addressType].length) continue;
        if(candidate[addressType].includes(target)) matched.push(candidate);
        if(matched.length === 10) break;
    }

    return matched;
}