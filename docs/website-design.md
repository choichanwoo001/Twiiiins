# TWIIIINS 공개 웹사이트 디자인 가이드

페이지를 추가하거나 콘텐츠를 다른 화면으로 옮길 때는 기존 사이트에서 같은 역할을 하는 요소를 기준으로 글꼴, 크기, 굵기, 색, 여백을 맞춘다.

## 글꼴과 색

- 공개 화면은 공통 글꼴인 Jost를 사용한다. 기본 본문 줄 높이는 `1.6`이다. [공통 스타일](../frontend/src/style.css)
- 흰색 배경은 `--color-background`, 기본 글자는 `--color-text`(`#1E1D1D`), 뉴스 제목과 같은 보조 글자는 `--color-text-secondary`(`#555`)를 사용한다. 섹션 제목을 수정할 때는 Projects, Concert, Contact처럼 해당 페이지에 지정된 색을 따른다. [공통 색상](../frontend/src/style.css)

## 상단 메뉴와 뉴스 목록

- 상단 메뉴는 대문자 텍스트를 사용하고 현재 페이지 링크에 밑줄을 표시한다. 넓은 화면의 언어 선택기도 인접 메뉴와 같은 크기와 색을 사용한다. [상단 메뉴](../frontend/src/App.vue) · [언어 선택기](../frontend/src/components/common/LanguageSwitch.vue)
- 뉴스 제목은 기존 Media의 News 목록을 기준으로 한다. Jost Medium(`500`)과 `--color-text-secondary`를 사용한다. 데스크톱은 `1.1rem`(17.6px)과 줄 높이 `1.6`, 모바일은 `1.125rem`(18px)과 줄 높이 `1.4`다. 목록의 이미지, 날짜, 제목은 흰 배경 위에 테두리 없이 배치한다. [기존 운영 뉴스](https://twiiiins.com/media?section=news) · [News 화면](../frontend/src/views/News.vue)

## 화면 확인

데스크톱과 모바일에서 기존 화면의 같은 요소와 비교한다. 화면이나 브라우저의 확대율이 다를 때는 CSS의 실제 계산값으로 크기와 색을 확인한다. 영어와 독일어 모두에서 줄바꿈과 메뉴 폭을 확인한다.
