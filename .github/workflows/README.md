# GitHub Actions 워크플로우

## 자동 빌드 및 릴리스

이 프로젝트는 `app/build.gradle.kts` 파일의 버전 정보가 변경될 때마다 자동으로 APK를 빌드하고 GitHub Release를 생성합니다.

### 릴리스 방법

1. **버전 업데이트**
   ```kotlin
   // app/build.gradle.kts
   defaultConfig {
       versionCode = 2  // 빌드 번호 증가
       versionName = "1.1.0"  // 버전 이름 변경
   }
   ```

2. **변경사항 커밋 및 푸시**
   ```bash
   git add app/build.gradle.kts
   git commit -m "chore: bump version to 1.1.0"
   git push origin main
   ```

3. **자동 실행**
   - GitHub Actions가 자동으로 실행됩니다
   - APK가 빌드됩니다
   - Release 노트가 자동 생성됩니다
   - GitHub Release가 생성되고 APK가 업로드됩니다

### 워크플로우 동작

- **트리거**: `app/build.gradle.kts` 파일이 main/master 브랜치에 푸시될 때
- **빌드**: Release APK 생성
- **릴리스**: versionName을 태그로 사용하여 GitHub Release 생성
- **중복 방지**: 동일한 버전의 Release가 이미 존재하면 건너뜀

### 릴리스 확인

릴리스는 다음 URL에서 확인할 수 있습니다:
```
https://github.com/hanjungwoo3/dinodash/releases
```

### 버전 관리 가이드

- **Major (X.0.0)**: 주요 기능 추가 또는 호환성 깨지는 변경
- **Minor (x.X.0)**: 새로운 기능 추가
- **Patch (x.x.X)**: 버그 수정 및 소규모 개선

### 예시

```
1.0.0 → 초기 릴리스
1.1.0 → 골드 스킨 추가
1.2.0 → 해골 스킨 및 코드 시스템 추가
1.2.1 → 업적 알림 버그 수정
```

