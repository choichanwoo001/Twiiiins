# PR Title
feat: 모바일 반응형 최적화 및 폰트 시스템 개선

# PR Description

## 📱 주요 변경사항

### 1. 모바일 반응형 UI 구현
- **햄버거 메뉴 네비게이션**: 모바일 환경에서 햄버거 버튼을 통한 전체 화면 메뉴 제공
- **반응형 레이아웃**: 모든 주요 페이지에 모바일 최적화 레이아웃 적용
  - About 페이지: 모바일 스크롤 처리 개선
  - Concert 페이지: 단일 컬럼 레이아웃 및 날짜 표시 개선
  - Projects 페이지: 모바일 친화적 프로젝트 카드 레이아웃
  - Media 페이지: 반응형 미디어 그리드 및 섹션 네비게이션
  - Contact 페이지: 모바일 최적화 폼 레이아웃
  - Shop 페이지: 모바일 상품 디스플레이 개선

### 2. 폰트 시스템 개선
- **폰트 변경**: Josefin Sans → Jost
- **폰트 웨이트 최적화**: 100-900까지 전체 웨이트 제공
- **로딩 성능 개선**: font-display: swap 적용으로 FOUT 방지

### 3. UI/UX 개선
- 모바일 전용 홈 이미지 추가 (`mobile_home.png`)
- SNS 링크 표시 로직 개선
- 스크롤 처리 및 바닥 도달 감지 기능 추가
- 모바일에서 텍스트 가독성 향상 (wrapping, spacing 개선)

### 4. 관리자 페이지 개선
- 이미지 최적화 옵션 추가
- 뉴스/사진 관리 개선

## 🔧 기술적 변경사항

### 파일 변경 통계
- **37개 파일 변경**
- **+1,551 추가**, **-111 삭제**

### 주요 파일 변경
- `frontend/src/App.vue`: 햄버거 메뉴 및 모바일 네비게이션 구현
- `frontend/src/style.css`: 모바일 반응형 스타일 추가
- `frontend/src/hamburger_styles.css`: 햄버거 메뉴 전용 스타일 (신규)
- `frontend/src/views/*`: 모든 뷰 컴포넌트 모바일 최적화
- `frontend/src/assets/fonts/*`: Jost 폰트 파일 추가

### 반응형 브레이크포인트
- 모바일: ~768px
- 태블릿: 768px~1024px
- 데스크탑: 1024px+

## 📊 커밋 히스토리
1. `feat: optimize Concert page for mobile with responsive single-column layout and stacked date formatting`
2. `style: refine mobile layout for Concert page with single-line date and stacked details`
3. `style: revert date formatting to stacked and enable text wrapping for better mobile legibility`
4. `feat: 1차 모바일 최적화 진행`
5. `feat: 1차 mobile 최적화`
6. `feat: 2차 모바일 최적화 및 1차 배포 적용`

## ✅ 테스트 완료 항목
- [x] 모바일 반응형 레이아웃 동작 확인
- [x] 햄버거 메뉴 네비게이션 동작 확인
- [x] 모든 페이지 모바일 뷰 확인
- [x] 폰트 로딩 및 표시 확인
- [x] 데스크탑 레이아웃 정상 동작 확인

## 🎯 배포 영향
- **1차 배포 준비 완료**: 모바일 최적화가 적용된 상태로 배포 가능
- **성능 개선**: 폰트 최적화로 초기 로딩 성능 개선
- **사용자 경험 향상**: 모바일 사용자를 위한 전용 UI 제공

## 📝 참고사항
- 기존 데스크탑 레이아웃은 모두 유지
- 반응형 디자인으로 다양한 화면 크기 지원
- 모바일 우선 접근 방식 적용
