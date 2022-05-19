function getPartialMatch(N) {
    const pi = new Array(N.length).fill(0);
    let begin = 1, matched = 0;
    while(begin + matched < N.length) {
        if(N[begin + matched] === N[matched]) {
            matched++;
            pi[begin + matched - 1] = matched;
        }
        else {
            if(matched === 0) begin++;
            else {
                begin += matched - pi[matched - 1];
                matched = pi[matched - 1];
            }
        }
    }
    return pi;
}

function kmpSearch(H, N, pi) {
    let begin = 0, matched = 0;
    while(begin <= H.length - N.length) {
        if(matched < N.length && H[begin + matched] === N[matched]) {
            if(++matched === N.length ) return true;
        }
        else {
            if(matched === 0) begin++;
            else {
                begin += matched - pi[matched - 1];
                matched = pi[matched - 1];
            }
        }
    }
    return false;
}

export default function findMatches(target, candidates, addressType = "bjdJuso") {
    const matched = [];
    const pi = getPartialMatch(target);
    for(let candidate of candidates) {
        if(target.length > candidate[addressType].length) continue;
        if(kmpSearch(candidate[addressType], target, pi)) matched.push(candidate);
        if(matched.length === 5) break;
    }
    return matched;
}