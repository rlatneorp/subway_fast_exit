## 설계 당시 모습
<img width="1280" height="809" alt="Image" src="https://github.com/user-attachments/assets/0b727222-66b2-45d7-b530-f6c23335ccbf" />

## 구현된 앱 모습
<img width="2151" height="2400" alt="Image" src="https://github.com/user-attachments/assets/fa0f30e2-80f4-43bd-9f64-998f4ed0a629" />

## 앱 아이콘
<img width="764" height="764" alt="Image" src="https://github.com/user-attachments/assets/46ac2185-a06d-41f9-a152-6b54ce71f951" />

## 시연 영상
[https://tv.kakao.com/v/459479091](https://tv.kakao.com/v/459485471)

## 기능구현 목록

1. UI를 안드로이드 스튜디오를 이용해 xml로 만든다
    - Layout을 적절하게 사용
    - 피그마에 그려둔 조감도대로 만든다
2. 현재 시간을 알려준다
    - 안드로이드 스튜디오 기능을 이용하여 간단하게 만든다
3. 역이 검색하면 검색이 되고 그 역의 정보를 가져오게 한다
    - 검색 API를 가져와서 역의 정보를 읽어온다
4. 해당 역에 도착해서 어플을 키면 승강기 정보를 알려주게 한다
    - 해당 역에 도착하면 자동으로 위치를 찾아 역 정보를 읽는다
5. 해당위치 버튼을 누르면 GPS 정보가 갱신된다
    - 해당위치를 눌러 바로 갱신되게끔 한다
6. 문의를 누르면 문의 내용이 이메일로 간다

## 설계하기 전에 알아야할 것

### UI를 어떻게 할 것인지

안드로이드 어플을 만들때 ui는 어떻게 만들어야하는지 찾아야한다

### GPS정보를 어떻게 얻어올 것인지

GPS는 어떻게 쓸 수 있는지 알아야한다

### API를 어떻게 사용할 것인지

코틀린에서는 API를 어떻게 사용하는지 알아야한다

### 어플을 어떻게 실행할지

휴대폰이나 시뮬레이션을 작동하는 방법을 알아야한다

### 필요한 API들이 무엇인지 알아야한다

안드로이드 어플을 만들때 필요한 것들이 무엇인지 알아야한다

## 사용한 API

지하철 승강기 정보 API → https://data.seoul.go.kr/dataList/OA-15994/S/1/datasetView.do

https://data.seoul.go.kr/dataList/OA-22122/S/1/datasetView.do

GPS → https://developers.google.com/maps/documentation/geolocation/overview?hl=ko

카카오 맵 → https://apis.map.kakao.com/android_v2/

