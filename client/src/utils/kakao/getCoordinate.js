const { kakao } = window;

const geocoder = new kakao.maps.services.Geocoder();

export default function getCoordinate(address) {
    return new Promise((resolve, reject) => {
        geocoder.addressSearch(address, function(result, status) {
            // 정상적으로 검색이 완료됐으면 
            if (status === kakao.maps.services.Status.OK) {
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                resolve(coords);
            }
            if(status === kakao.maps.services.Status.ZERO_RESULT) {
                resolve({});
            }
            else {
                reject(new Error("Server Response Error..."));
            }
        });
    });
}