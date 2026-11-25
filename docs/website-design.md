# TWIIIINS 공개 웹사이트 디자인 가이드

공개 페이지를 추가하거나 콘텐츠를 옮길 때는 같은 역할을 하는 기존 화면의 글꼴, 크기, 굵기, 색, 여백을 먼저 적용한다.

## 글꼴과 색

공개 사이트의 기본 글꼴은 Jost이고, 본문의 기본 줄 높이는 `1.6`이다. [공통 스타일](../frontend/src/style.css)

| 요소 | 기준 |
| --- | --- |
| 왼쪽 페이지 제목 | Jost Regular(`400`), 대문자, 자간 `0.12em`, 줄 높이 `1`. 데스크톱 `clamp(2.5rem, 6vw, 4.5rem)`, 모바일 기준 `2.5rem` |
| 상단 메뉴 | Jost Regular, 대문자, 줄 높이 `1.5rem`. 언어 선택기는 인접 메뉴와 같은 글꼴 크기를 사용 |
| News의 기사 제목 | Jost Medium(`500`), `#555`. 데스크톱 `1.1rem`과 줄 높이 `1.6`, 모바일 `1.125rem`과 줄 높이 `1.4` |

왼쪽 제목의 크기와 자간은 About, Projects, Concert, Contact, Shop을 참고한다. 각 페이지의 제목색을 사용한다. About은 `#8B4513`, Projects는 `#5B8793`, Concert는 `#CEC0A3`, Contact는 `#815D47`, Shop은 `#60867E`, News는 홈 공연 섹션과 같은 벽돌색 `#943C31`이다. [About](../frontend/src/views/About.vue) · [Projects](../frontend/src/views/Projects.vue) · [Concert](../frontend/src/views/Concert.vue) · [Contact](../frontend/src/views/Contact.vue) · [Shop](../frontend/src/views/Shop.vue) · [News](../frontend/src/views/News.vue) · [홈 공연 섹션](../frontend/src/views/Home.vue)

| 역할 | 공통 CSS 값 |
| --- | --- |
| 기본 배경과 글자 | `--color-background` `#FFF`, `--color-text` `#1E1D1D` |
| 보조 글자와 설명 | `--color-text-secondary` `#555`, `--color-text-tertiary` `#666` |
| 중립 배경과 구분선 | `--color-background-soft` `#F8F8F8`, `--color-background-mute` `#F5F5F5`, `--color-border` `#E0E0E0` |
| Media 제목과 공개 화면의 링크·버튼 | Media는 `--color-accent` `#FBCE7B`. 뉴스레터·개인정보 등에서 쓰는 링크와 공통 버튼은 `#815D47` |

## 페이지 배치와 반응형

- 제목과 본문이 나뉘는 화면은 데스크톱에서 왼쪽 제목, 오른쪽 본문을 사용한다. Projects·Contact·Shop의 기준은 약 `1 : 2.2`의 열 비율, `4rem` 열 간격, 왼쪽 `2rem`과 오른쪽 `4rem` 여백이다. 헤더 아래 `5rem` 지점에서 제목과 본문을 시작한다. [Projects](../frontend/src/views/Projects.vue) · [Contact](../frontend/src/views/Contact.vue) · [Shop](../frontend/src/views/Shop.vue)
- `48rem` 이하에서는 제목과 본문을 위아래로 배치한다. About·Projects·Concert·Contact·Shop·News는 일반적으로 상단 `4rem`, 좌우 `1.5rem` 여백을 사용한다. 긴 제목과 본문은 가로 넘침을 피하고, 주 콘텐츠는 화면 세로 스크롤로 읽을 수 있게 둔다. News는 독일어 제목 길이에 맞게 데스크톱 왼쪽 열이 넓어지며, `22.5rem` 이하에서 제목 크기를 줄인다. [공통 스타일](../frontend/src/style.css) · [News](../frontend/src/views/News.vue)
- 고정 헤더 높이는 `--navbar-height`의 데스크톱 `7rem`, 모바일 `5rem`을 사용한다. 본문 맨 아래는 `--website-footer-space`로 공간을 확보하고, 모바일에서는 기기의 안전 영역과 하단 링크가 줄바꿈되는 높이를 고려한다. [공통 스타일](../frontend/src/style.css)

## 헤더와 하단 링크

- 공통 헤더는 왼쪽 로고, 오른쪽 메뉴와 언어 선택기로 구성한다. 현재 페이지 메뉴는 밑줄로 표시한다. 좁은 화면에서는 햄버거 메뉴와 현재 언어 코드가 오른쪽에 표시된다. 데스크톱에서는 클릭하거나 커서를 올려 언어를 선택하고, 터치 화면에서는 탭으로 선택한다. [상단 메뉴](../frontend/src/App.vue) · [언어 선택기](../frontend/src/components/common/LanguageSwitch.vue)
- 로고는 글자로 표시하며 굵기 `350`, 자간 `0.125rem`을 사용한다. 넓은 데스크톱에서는 홈 `5rem`, 내부 페이지 `2.5rem`을 사용하고, 모바일에서는 `2rem`을 사용한다. [로고 스타일](../frontend/src/style.css)
- 홈 첫 화면에는 헤더를 표시하고, 두 번째 공연 섹션에서는 로고와 언어 선택기를 포함한 헤더 전체를 숨긴다. SNS 링크는 공통 헤더와 별도로 배치한다. 홈 바깥의 공개 페이지에서는 개인정보·이용약관과 쿠키 설정을 공통 하단 영역에서 찾을 수 있다. [홈](../frontend/src/views/Home.vue) · [공통 스타일](../frontend/src/style.css)
- 상단 순서는 Media → News → Concert다. Media의 하위 메뉴는 Music, Videos, Photos를 표시한다. 사이트 표시 언어는 페이지 이동 후에도 유지하며, 글 제목처럼 두 언어에서 동일한 고유명은 해당 콘텐츠의 표기를 사용한다. [상단 메뉴](../frontend/src/App.vue) · [언어 동작](./website-language.md)

## 버튼, 폼과 대화상자

- 선으로 둘러싼 공개 버튼은 `.website-button`을 사용한다. 흰색 계열 배경 위에 갈색 글자·테두리를 사용하고, 최소 높이는 `2.75rem`이다. 키보드 포커스는 보이는 외곽선으로 표시한다. [공통 스타일](../frontend/src/style.css)
- 폼의 라벨은 입력칸 위에 표시한다. 이메일과 수신 언어처럼 짧은 필드는 공간이 충분하면 나란히 두고, 좁은 화면에서는 줄을 바꾼다. 가입 상태와 오류는 폼 가까이에서 안내한다. [뉴스레터 가입 폼](../frontend/src/components/common/NewsletterSignup.vue)
- 공개 대화상자는 `WebsiteDialog`의 흰 배경·중앙 배치·닫기 버튼을 사용한다. Escape로 닫을 수 있고, 닫힌 뒤에는 이전 요소로 포커스가 돌아간다. 쿠키 설정의 외부 미디어 허용 상태는 영상 표시에도 반영한다. [공개 대화상자](../frontend/src/components/common/WebsiteDialog.vue) · [쿠키 설정](../frontend/src/components/common/CookieControls.vue)

## 이미지와 뉴스 목록

- 목록과 갤러리 이미지는 화면 폭에 맞는 크기를 제공한다. 이미지가 전달하는 정보는 대체 텍스트로 설명한다. 같은 링크나 버튼 안에서 제목을 이미 읽을 수 있는 장식용 썸네일은 빈 대체 텍스트를 사용한다. 상세 이미지 갤러리는 `ImageGrid`를 참고한다. [반응형 이미지](../frontend/src/utils/responsiveImages.js) · [이미지 갤러리](../frontend/src/components/common/ImageGrid.vue)
- News의 오른쪽 본문은 Stay Connected 신청란과 뉴스 목록 순서다. 기사 미리보기는 흰 배경에 바깥 테두리 없이 첫 이미지, 날짜, 제목을 표시한다. 날짜는 영어·독일어 모두 `DD.MM.YYYY`이며, 제목 행을 누르면 상세가 펼쳐진다. [News](../frontend/src/views/News.vue)

## 화면 확인

기존 화면의 같은 요소와 CSS 계산값을 비교한다. 데스크톱과 모바일, `48rem` 전후, 좁은 휴대폰에서 영어·독일어 제목과 메뉴의 줄바꿈을 확인한다. 제목 계층, 가로 스크롤, 고정 헤더·하단 링크와 본문 겹침, 키보드로 메뉴를 열고 닫는 동작도 확인한다.
