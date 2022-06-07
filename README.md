# Carbon Tracker

<a href="https://www.youtube.com/watch?v=vmxlkb18iG0">📺 Youtube</a>

## 목적

기후위기의 주된 원인인 지구온난화를 막기 위한, 탄소 절감은 이제 인류 생존을 위한 필수 요소가 되었다.
이에 대응하여 대구시 **주택 단지별 탄소 배출량 및 탄소 포인트를 예측 및 시각화**하여 개인의 탄소 절감과 기관의 유연한 기후 대응을 돕고자 한다.

### 탄소 배출량 측정 방법

- 탄소 배출량(kgCO2eq.) = 에너지 사용량 x 국가 고유 온실 가스 배출 계수

  - 전기 온실 가스 배출 계수: 0.4663(kwh)
  - 가스 온실 가스 배출 계수: 2.22(m³)
  - 수도 온실 가스 배출 계수: 0.3332(m³)

### 예상 탄소 포인트 측정 방법

- 전년 동일 월 대비 감축률에 따라 포인트를 부여

  - 감축률에 따른 포인트 양은 https://cpoint.or.kr/ 참고

- 반기별 월별 포인트 합산한 값을 예상 탄소 포인트로 측정

## 문서

- <a href='https://delicate-slug-432.notion.site/0691e3f8caf74f0780a9b3937b9814d4?v=60ba5bb9b1304c7f97d3f5b8dec38bbb'>사용자 요구사항 정의서</a>
- <a href='https://delicate-slug-432.notion.site/705e8b8539b84aa49adb3a072ce81527'>유스케이스 명세서</a>
- <a href='https://delicate-slug-432.notion.site/f244ed4f714d47c882bf6f0fbe4f6f92'>인터페이스 설계</a>

## 시스템 설계

![image](https://user-images.githubusercontent.com/33220404/172395870-c0994407-f38b-4f69-9f96-7a6fe41fac61.png)

## 진행 상황

- [X] 필요한 공공데이터 수집 및 가공
- [X] 주택 단지 에너지 정보 API 구현
- [X] 카카오 map 컴포넌트 구현
- [X] D3 에너지 사용량 시각화
- [X] 도로명/법정명 주소 좌표 변환 API 구현
- [X] Elasticsearch 조회 기능 구현 및 연동
- [X] 결측치 데이터 대체 알고리즘 구현
- [X] 검색 알고리즘 구현 및 지도 연동
- [X] 에너지 사용량 테이블 시각화 
- [X] 예상 탄소 포인트 계산 Service 구현
- [X] 기후 데이터를 통한 하루 예상 탄소 배출량 예측 모델 생성
- [X] 예상 탄소 포인트/탄소 배출량 컴포넌트 구현
- [ ] 테스팅 및 개선

<img width="1440" alt="image" src="https://user-images.githubusercontent.com/33220404/170810550-3c8ec9cd-0157-432b-a2a8-25131fa77244.png">

<img width="1440" alt="image" src="https://user-images.githubusercontent.com/33220404/170810582-07095669-3604-4247-b64f-069f7090f23a.png">

<img width="1440" alt="image" src="https://user-images.githubusercontent.com/33220404/172394528-92a41f92-ace6-451c-8034-53a60863b38e.png">

<img width="1440" alt="image" src="https://user-images.githubusercontent.com/33220404/172394642-fa1fe514-c6a4-40e0-9c19-4a7f2be986fb.png">

## Contributor

[Go-Jaecheol](https://github.com/Go-Jaecheol)

[KingDonggyu](https://github.com/KingDonggyu)

[SeongukBaek](https://github.com/SeongukBaek)

[woong-jae](https://github.com/woong-jae)

