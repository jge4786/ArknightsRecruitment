# 사용법

- 앱 실행 시 처음 보이는 화면: 메인 화면
- **인식** 버튼: 플로팅 버튼
- **시작** 버튼 터치 후 **인식** 버튼 눌렀을 때 보이는 화면: 결과 화면

앱 실행 후, **메인 화면**의 **시작** 누르면 **플로팅 버튼** 표시

**플로팅 버튼** 터치 시 현재 화면에 보이는 텍스트 인식 후, **결과 화면**에 1성 혹은 4성 이상 확정 태그 조합 있으면 해당 태그 조합 표시

**결과 화면** 상단의 “-” 버튼 누를 경우 다시 **플로팅 버튼**으로 축소, “X” 버튼 누를 경우 앱 종료

앱 종료는 결과 화면의 “X” 버튼 말고 푸시 알림 터치로도 가능

# 빌드 방법

아마 파일 그대로 안드로이드 스튜디오에 넣고 빌드하면 될지도 다른 환경에서 안 해봐서 잘 몰?루겠음

# 업데이트

### 1. 공채 데이터만 업데이트할 경우

1. `src/main/assets/opData.json`에 추가된 오퍼 데이터 추가
2. 이후, `src/main/assets/Versions.json` 에 추가된 클라이언트 데이터 버전 입력
    1. 모르겠으면 그냥 `src/main/assets/defaultVersion.json` 에 명시된 값과 다르게 하면 됨

### 2. 앱 업데이트할 경우

1. 공채 데이터도 업데이트된 경우, 1번 내용 그대로 따라가기
2. `src/main/assets/defaultVersion.json` 에 현재 적용된 클라이언트 데이터 버전 입력

## 오퍼데이터 입력 방법

정해진 JSON 형태로 `opData.json`에 추가

```
{
    "tag" : <오퍼 태그>,
    "name" : <오퍼 이름>,
    "rarity" : <오퍼 희귀도>,
    "id" : <오퍼 ID>
}
```

id: 오퍼레이터 고유 ID. 안 겹치게만 넣으면 됨. String

rarity: 6성이면 6, 5성이면 5, 4성이면 4, 3성이면 3 등등. String

name: 실버애쉬 모스티마 등등. String

tag: 오퍼 정보에 표시되는 태그들인데 각 태그를 아래 표 보고 대응되는 숫자를 다 더하면 됨. 대응되는 숫자는 아래 참고. Int

```
가드 -> 2
스나이퍼 -> 4
디펜더 -> 8
메딕 -> 16
서포터 -> 32
캐스터 -> 64
스페셜리스트 -> 128
뱅가드 -> 256
근거리 -> 512
원거리 -> 1024
고급특별채용 -> 2048
제어형 -> 4096
누커 -> 8192
특별채용 -> 16384
힐링 -> 32768
지원 -> 65536
신입 -> 131072
코스트+ -> 262144
딜러 -> 524288
생존형 -> 1048576
범위공격 -> 2097152
방어형 -> 4194304
감속 -> 8388608
디버프 -> 16777216
쾌속부활 -> 33554432
강제이동 -> 67108864
소환 -> 134217728
로봇 -> 268435456
```

예를 들어, 훗날 공채에 짱이쁜 우리 뮤뮤가 추가됐다고 가정하면

뮤뮤는 뱅가드이고 6성이고 오퍼 정보에 표시되는 태그는 원거리, 제어형, 코스트+임

그럼 위 표에서 뱅가드, 고급특별채용, 원거리, 제어형, 코스트+에 대응되는 숫자를 다 더하면 됨

뱅가드 `256` , 고특채 `2048`, 원거리 `1024`, 제어형 `4096`, 코스트+ `262144` 

256 + 2048 + 1024 + 4096 + 262144 = 269568

따라서 뮤뮤 태그는 269568임

최종적으로 추가되는 json은

```
{
    "tag" : 269568
    "name" : "뮤엘시스",
    "rarity" : "6",
    "id" : "aaaaa"
}
```

이렇게 됨

### 개발환경

OS: Mac 14.2.1

IDE: Android Studio Flamingo | 2022.2.1 Patch 2

빌드툴: jbr-17.0.6