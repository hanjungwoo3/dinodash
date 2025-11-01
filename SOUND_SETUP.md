# 🔊 사운드 파일 설정 가이드

게임에 소리 효과를 추가하는 방법입니다.

## 📁 필요한 사운드 파일

게임에서 사용하는 2개의 사운드 파일이 필요합니다:

1. **jump_sound** - 고양이가 점프할 때
2. **game_over_sound** - 물웅덩이를 밟았을 때

## 🎵 사운드 파일 준비하기

### 옵션 1: 무료 사운드 다운로드 사이트

다음 사이트에서 무료 효과음을 다운로드할 수 있습니다:

- **Freesound.org** (https://freesound.org/)
  - 회원가입 필요
  - Creative Commons 라이선스
  
- **Zapsplat** (https://www.zapsplat.com/)
  - 무료 효과음 제공
  
- **Mixkit** (https://mixkit.co/free-sound-effects/)
  - 회원가입 불필요
  
- **Pixabay** (https://pixabay.com/sound-effects/)
  - 무료 효과음

### 추천 검색 키워드

- **점프 소리**: "jump", "pop", "boing", "hop", "spring"
- **게임 오버 소리**: "splash", "water", "fail", "game over", "meow sad"

## 📂 파일 추가 방법

### 1단계: raw 폴더 생성

Android Studio에서:

```
app/src/main/res/ 폴더에서 우클릭
> New > Android Resource Directory
> Resource type: raw 선택
> OK 클릭
```

또는 수동으로:
```
app/src/main/res/raw/ 폴더 생성
```

### 2단계: 사운드 파일 이름 변경

다운로드한 사운드 파일의 이름을 다음과 같이 변경:

- 점프 소리 → `jump_sound.mp3` (또는 .wav, .ogg)
- 게임 오버 소리 → `game_over_sound.mp3` (또는 .wav, .ogg)

**중요**: 파일 이름은 소문자와 언더스코어(_)만 사용해야 합니다!

### 3단계: 파일 복사

변경한 파일을 다음 경로에 복사:

```
app/src/main/res/raw/jump_sound.mp3
app/src/main/res/raw/game_over_sound.mp3
```

### 4단계: 프로젝트 다시 빌드

```
Build > Clean Project
Build > Rebuild Project
```

## 🎨 직접 소리 만들기 (선택사항)

음성 녹음 앱이나 온라인 도구를 사용해서 직접 소리를 만들 수도 있습니다:

### 점프 소리 아이디어
- "냐옹!" 소리 녹음
- 입으로 "뿅!" 소리 내기
- 휘파람 소리

### 게임 오버 소리 아이디어
- 물에 뛰어드는 소리 "첨벙!"
- 슬픈 "야옹~" 소리
- "앗!" 하는 놀란 소리

### 온라인 사운드 생성 도구
- **BFXR** (https://www.bfxr.net/) - 8비트 게임 효과음 생성기
- **ChipTone** (https://sfbgames.itch.io/chiptone) - 레트로 사운드 생성

## 🔧 트러블슈팅

### 소리가 안 나요!

1. **파일 이름 확인**
   - `jump_sound.mp3`
   - `game_over_sound.mp3`
   - 정확한 이름인지 확인

2. **파일 위치 확인**
   - `app/src/main/res/raw/` 폴더에 있는지 확인

3. **파일 형식 확인**
   - 지원 형식: .mp3, .wav, .ogg
   - 파일이 손상되지 않았는지 확인

4. **권한 확인**
   - 기기 볼륨이 켜져 있는지 확인
   - 무음 모드가 아닌지 확인

5. **프로젝트 재빌드**
   ```
   Build > Clean Project
   Build > Rebuild Project
   ```

### 소리가 없어도 게임은 작동하나요?

네! 소리 파일이 없어도 게임은 정상적으로 작동합니다. 
단지 소리만 나지 않을 뿐입니다. 에러는 발생하지 않습니다.

## 📝 파일 형식별 특징

| 형식 | 장점 | 단점 |
|------|------|------|
| .mp3 | 작은 파일 크기 | 약간의 음질 손실 |
| .wav | 최고 음질 | 큰 파일 크기 |
| .ogg | 좋은 압축률 | 일부 구형 기기에서 미지원 |

**권장**: 짧은 효과음은 .mp3나 .ogg 형식 추천 (작은 파일 크기)

## ✨ 추가 팁

1. **파일 크기**: 효과음은 1~3초 정도로 짧게 (파일 크기 50KB 이하 권장)
2. **볼륨**: 너무 크지 않게 조절 (귀가 아프지 않게)
3. **형식 통일**: 모든 사운드를 같은 형식으로 (.mp3 또는 .ogg)

---

소리를 추가하고 게임을 즐기세요! 🎮🐱💧


