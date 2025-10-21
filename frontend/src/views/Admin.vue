<template>
  <div class="admin">
    <!-- 왼쪽 사이드바 -->
    <aside class="admin-sidebar">
      <h2 class="sidebar-title">ADMIN</h2>
      <nav class="sidebar-nav">
        <button 
          :class="['nav-item', { active: activeSection === 'dashboard' }]"
          @click="activeSection = 'dashboard'">
          Dashboard
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'projects' }]"
          @click="activeSection = 'projects'">
          Projects
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'concerts' }]"
          @click="activeSection = 'concerts'">
          Concerts
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'media' }]"
          @click="activeSection = 'media'">
          Media
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'contact' }]"
          @click="activeSection = 'contact'">
          Contact
        </button>
      </nav>
    </aside>

    <!-- 메인 콘텐츠 영역 -->
    <main class="admin-content">
      <!-- Dashboard -->
      <div v-if="activeSection === 'dashboard'" class="content-section">
        <h1 class="section-title">Dashboard</h1>
        <div class="dashboard-card">
          <h3>방문자 통계</h3>
          <p>Google Analytics를 사용하여 방문자 통계를 확인할 수 있습니다.</p>
          <p>Google Analytics 대시보드: <a href="https://analytics.google.com" target="_blank">https://analytics.google.com</a></p>
          <div class="stats-placeholder">
            <p>GA4 추적 코드를 index.html에 추가하세요.</p>
          </div>
        </div>
      </div>

      <!-- Projects 관리 -->
      <div v-if="activeSection === 'projects'" class="content-section">
        <!-- 검색/필터 섹션 -->
        <div class="search-section">
          <div class="search-filters">
            <div class="filter-group">
              <label>프로젝트명</label>
              <input v-model="searchFilters.title" placeholder="프로젝트명을 입력하세요" />
            </div>
            <div class="filter-group">
              <label>장소</label>
              <input v-model="searchFilters.location" placeholder="장소를 입력하세요" />
            </div>
            <div class="filter-group">
              <label>날짜 범위</label>
              <div class="date-range">
                <input type="date" v-model="searchFilters.startDate" />
                <span>~</span>
                <input type="date" v-model="searchFilters.endDate" />
              </div>
            </div>
            <div class="filter-actions">
              <button class="btn-reset" @click="resetFilters">초기화</button>
              <button class="btn-search" @click="searchProjects">검색</button>
            </div>
          </div>
        </div>

        <!-- 프로젝트 등록/수정 폼 -->
        <div class="project-form-section">
          <h2>{{ editingProject ? '프로젝트 수정' : '새 프로젝트 등록' }}</h2>
          <form @submit.prevent="saveProject" class="project-form">
            <div class="form-row">
              <div class="form-group">
                <label>제목 *</label>
                <input v-model="projectForm.title" required />
              </div>
              <div class="form-group">
                <label>부제목</label>
                <input v-model="projectForm.subtitle" />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>날짜 *</label>
                <input type="date" v-model="projectForm.premiereDate" required />
              </div>
              <div class="form-group">
                <label>장소 *</label>
                <input v-model="projectForm.location" required />
              </div>
            </div>

            <div class="form-group">
              <label>설명 1</label>
              <textarea v-model="projectForm.description1" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label>설명 2</label>
              <textarea v-model="projectForm.description2" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label>설명 3</label>
              <textarea v-model="projectForm.description3" rows="3"></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>메인 이미지 URL</label>
                <input v-model="projectForm.mainImageUrl" />
              </div>
              <div class="form-group">
                <label>커버 이미지 URL</label>
                <input v-model="projectForm.coverImageUrl" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>가로 이미지 1 URL</label>
                <input v-model="projectForm.horizontal1ImageUrl" />
              </div>
              <div class="form-group">
                <label>가로 이미지 2 URL</label>
                <input v-model="projectForm.horizontal2ImageUrl" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>세로 이미지 1 URL</label>
                <input v-model="projectForm.vertical1ImageUrl" />
              </div>
              <div class="form-group">
                <label>세로 이미지 2 URL</label>
                <input v-model="projectForm.vertical2ImageUrl" />
              </div>
            </div>

            <div class="form-group">
              <label>더보기 URL</label>
              <input v-model="projectForm.moreInfoUrl" />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>리뷰 1 텍스트</label>
                <textarea v-model="projectForm.review1Text" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>리뷰 1 출처</label>
                <input v-model="projectForm.review1Source" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>리뷰 2 텍스트</label>
                <textarea v-model="projectForm.review2Text" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>리뷰 2 출처</label>
                <input v-model="projectForm.review2Source" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>URL Slug</label>
                <input v-model="projectForm.urlSlug" />
              </div>
              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="projectForm.displayOrder" />
              </div>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-save">{{ editingProject ? '수정' : '등록' }}</button>
              <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingProject">취소</button>
            </div>
          </form>
        </div>

        <!-- 프로젝트 목록 -->
        <div class="projects-list">
          <h2>전체 목록</h2>
          <div class="projects-table">
            <table>
              <thead>
                <tr>
                  <th>No</th>
                  <th>프로젝트</th>
                  <th>장소</th>
                  <th>날짜</th>
                  <th>상태</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(project, index) in projects" :key="project.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ project.title }}</td>
                  <td>{{ project.location }}</td>
                  <td>{{ formatDate(project.premiereDate) }}</td>
                  <td>
                    <span class="status-badge" :class="{ active: project.displayOrder > 0 }">
                      {{ project.displayOrder > 0 ? '활성' : '비활성' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn-edit" @click="editProject(project)">수정</button>
                    <button class="btn-delete" @click="deleteProject(project.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Concerts 관리 -->
      <div v-if="activeSection === 'concerts'" class="content-section">
        <!-- 검색/필터 섹션 -->
        <div class="search-section">
          <div class="search-filters">
            <div class="filter-group">
              <label>콘서트명</label>
              <input v-model="concertSearchFilters.name" placeholder="콘서트명을 입력하세요" />
            </div>
            <div class="filter-group">
              <label>장소</label>
              <input v-model="concertSearchFilters.location" placeholder="장소를 입력하세요" />
            </div>
            <div class="filter-group">
              <label>날짜 범위</label>
              <div class="date-range">
                <input type="date" v-model="concertSearchFilters.startDate" />
                <span>~</span>
                <input type="date" v-model="concertSearchFilters.endDate" />
              </div>
            </div>
            <div class="filter-actions">
              <button class="btn-reset" @click="resetConcertFilters">초기화</button>
              <button class="btn-search" @click="searchConcerts">검색</button>
            </div>
          </div>
        </div>

        <!-- 콘서트 등록/수정 폼 -->
        <div class="concert-form-section">
          <h2>{{ editingConcert ? '콘서트 수정' : '새 콘서트 등록' }}</h2>
          <form @submit.prevent="saveConcert" class="concert-form">
            <div class="form-row">
              <div class="form-group">
                <label>날짜 *</label>
                <input type="date" v-model="concertForm.date" required />
              </div>
              <div class="form-group">
                <label>장소 *</label>
                <input v-model="concertForm.location" required />
              </div>
            </div>

            <div class="form-group">
              <label>이름 *</label>
              <input v-model="concertForm.name" required />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>시작 시간</label>
                <input v-model="concertForm.startTime" />
              </div>
              <div class="form-group">
                <label>티켓 정보 URL</label>
                <input v-model="concertForm.ticketInfo" />
              </div>
            </div>

            <div class="form-group">
              <label>전체 주소</label>
              <input v-model="concertForm.fullLocation" />
            </div>

            <div class="form-group">
              <label>구글맵 URL</label>
              <input v-model="concertForm.googleMapUrl" />
            </div>

            <div class="form-group">
              <label>협업 정보</label>
              <input v-model="concertForm.collaborationInfo" />
            </div>

            <div class="form-group">
              <label>
                <input type="checkbox" v-model="concertForm.isPast" />
                지난 공연
              </label>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-save">{{ editingConcert ? '수정' : '등록' }}</button>
              <button type="button" class="btn-cancel" @click="cancelConcertEdit" v-if="editingConcert">취소</button>
            </div>
          </form>
        </div>

        <!-- 콘서트 목록 -->
        <div class="concerts-list">
          <h2>전체 목록</h2>
          <div class="concerts-table">
            <table>
              <thead>
                <tr>
                  <th>No</th>
                  <th>콘서트</th>
                  <th>장소</th>
                  <th>날짜</th>
                  <th>상태</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(concert, index) in concerts" :key="concert.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ concert.name }}</td>
                  <td>{{ concert.location }}</td>
                  <td>{{ formatDate(concert.date) }}</td>
                  <td>
                    <span class="status-badge" :class="{ past: concert.isPast }">
                      {{ concert.isPast ? '지난 공연' : '예정' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn-edit" @click="editConcert(concert)">수정</button>
                    <button class="btn-delete" @click="deleteConcert(concert.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Media 관리 -->
      <div v-if="activeSection === 'media'" class="content-section">
        <h1 class="section-title">Media 관리</h1>
        
        <!-- Media 탭 -->
        <div class="tabs">
          <button 
            :class="['tab', { active: mediaTab === 'music' }]"
            @click="mediaTab = 'music'">
            Music
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'videos' }]"
            @click="mediaTab = 'videos'">
            Videos
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'photos' }]"
            @click="mediaTab = 'photos'">
            Photos
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'news' }]"
            @click="mediaTab = 'news'">
            News
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'equipment' }]"
            @click="mediaTab = 'equipment'">
            Equipment
          </button>
        </div>

        <!-- Music 탭 -->
        <div v-if="mediaTab === 'music'" class="tab-content">
          <!-- 검색/필터 섹션 -->
          <div class="search-section">
            <div class="search-filters">
              <div class="filter-group">
                <label>제목</label>
                <input v-model="musicSearchFilters.title" placeholder="제목을 입력하세요" />
              </div>
              <div class="filter-group">
                <label>아티스트</label>
                <input v-model="musicSearchFilters.artist" placeholder="아티스트를 입력하세요" />
              </div>
              <div class="filter-actions">
                <button class="btn-reset" @click="resetMusicFilters">초기화</button>
                <button class="btn-search" @click="searchMusic">검색</button>
              </div>
            </div>
          </div>

          <!-- 음악 등록/수정 폼 -->
          <div class="music-form-section">
            <h2>{{ editingMusic ? '음악 수정' : '새 음악 등록' }}</h2>
            <form @submit.prevent="saveMusic" class="music-form">
              <div class="form-row">
                <div class="form-group">
                  <label>제목 *</label>
                  <input v-model="musicForm.title" required />
                </div>
                <div class="form-group">
                  <label>아티스트 *</label>
                  <input v-model="musicForm.artist" required />
                </div>
              </div>

              <div class="form-group">
                <label>커버 이미지 URL *</label>
                <input v-model="musicForm.coverUrl" required />
              </div>

              <div class="form-group">
                <label>링크 URL</label>
                <input v-model="musicForm.linkUrl" />
              </div>

              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="musicForm.displayOrder" />
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">{{ editingMusic ? '수정' : '등록' }}</button>
                <button type="button" class="btn-cancel" @click="cancelMusicEdit" v-if="editingMusic">취소</button>
              </div>
            </form>
          </div>

          <!-- 음악 목록 -->
          <div class="music-list">
            <h2>전체 목록</h2>
            <div class="music-table">
              <table>
                <thead>
                  <tr>
                    <th>No</th>
                    <th>제목</th>
                    <th>아티스트</th>
                    <th>커버 URL</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(music, index) in musicList" :key="music.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ music.title }}</td>
                    <td>{{ music.artist }}</td>
                    <td>{{ music.coverUrl }}</td>
                    <td>
                      <button class="btn-edit" @click="editMusic(music)">수정</button>
                      <button class="btn-delete" @click="deleteMusic(music.id)">삭제</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Videos 탭 -->
        <div v-if="mediaTab === 'videos'" class="tab-content">
          <!-- 검색/필터 섹션 -->
          <div class="search-section">
            <div class="search-filters">
              <div class="filter-group">
                <label>제목</label>
                <input v-model="videoSearchFilters.title" placeholder="제목을 입력하세요" />
              </div>
              <div class="filter-actions">
                <button class="btn-reset" @click="resetVideoFilters">초기화</button>
                <button class="btn-search" @click="searchVideos">검색</button>
              </div>
            </div>
          </div>

          <!-- 비디오 등록/수정 폼 -->
          <div class="video-form-section">
            <h2>{{ editingVideo ? '비디오 수정' : '새 비디오 등록' }}</h2>
            <form @submit.prevent="saveVideo" class="video-form">
              <div class="form-group">
                <label>제목 *</label>
                <input v-model="videoForm.title" required />
              </div>

              <div class="form-group">
                <label>YouTube Embed URL *</label>
                <input v-model="videoForm.embedUrl" required />
              </div>

              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="videoForm.displayOrder" />
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">{{ editingVideo ? '수정' : '등록' }}</button>
                <button type="button" class="btn-cancel" @click="cancelVideoEdit" v-if="editingVideo">취소</button>
              </div>
            </form>
          </div>

          <!-- 비디오 목록 -->
          <div class="videos-list">
            <h2>전체 목록</h2>
            <div class="videos-table">
              <table>
                <thead>
                  <tr>
                    <th>No</th>
                    <th>제목</th>
                    <th>Embed URL</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(video, index) in videos" :key="video.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ video.title }}</td>
                    <td>{{ video.embedUrl }}</td>
                    <td>
                      <button class="btn-edit" @click="editVideo(video)">수정</button>
                      <button class="btn-delete" @click="deleteVideo(video.id)">삭제</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Photos 탭 -->
        <div v-if="mediaTab === 'photos'" class="tab-content">
          <!-- 검색/필터 섹션 -->
          <div class="search-section">
            <div class="search-filters">
              <div class="filter-group">
                <label>제목</label>
                <input v-model="photoGroupSearchFilters.title" placeholder="제목을 입력하세요" />
              </div>
              <div class="filter-actions">
                <button class="btn-reset" @click="resetPhotoGroupFilters">초기화</button>
                <button class="btn-search" @click="searchPhotoGroups">검색</button>
              </div>
            </div>
          </div>

          <!-- 사진 그룹 등록/수정 폼 -->
          <div class="photo-group-form-section">
            <h2>{{ editingPhotoGroup ? '사진 그룹 수정' : '새 사진 그룹 등록' }}</h2>
            <form @submit.prevent="savePhotoGroup" class="photo-group-form">
              <div class="form-group">
                <label>제목 *</label>
                <input v-model="photoGroupForm.title" required />
              </div>

              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="photoGroupForm.displayOrder" />
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">{{ editingPhotoGroup ? '수정' : '등록' }}</button>
                <button type="button" class="btn-cancel" @click="cancelPhotoGroupEdit" v-if="editingPhotoGroup">취소</button>
              </div>
            </form>
          </div>

          <!-- 사진 추가 섹션 -->
          <div class="photo-add-section">
            <h2>사진 추가</h2>
            <form @submit.prevent="addPhotoToGroup" class="photo-add-form">
              <div class="form-row">
                <div class="form-group">
                  <label>사진 그룹 선택 *</label>
                  <select v-model="photoAddForm.photoGroupId" required>
                    <option value="">그룹을 선택하세요</option>
                    <option v-for="group in photoGroups" :key="group.id" :value="group.id">
                      {{ group.title }}
                    </option>
                  </select>
                </div>
                <div class="form-group">
                  <label>사진 URL *</label>
                  <input v-model="photoAddForm.imageUrl" placeholder="이미지 URL을 입력하세요" required />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>사진 제목</label>
                  <input v-model="photoAddForm.title" placeholder="사진 제목을 입력하세요" />
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="photoAddForm.displayOrder" />
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">사진 추가</button>
                <button type="button" class="btn-cancel" @click="resetPhotoAddForm">초기화</button>
              </div>
            </form>
          </div>

          <!-- 사진 그룹 목록 -->
          <div class="photo-groups-list">
            <h2>전체 목록</h2>
            <div class="photo-groups-table">
              <table>
                <thead>
                  <tr>
                    <th>No</th>
                    <th>제목</th>
                    <th>사진 수</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(group, index) in photoGroups" :key="group.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ group.title }}</td>
                    <td>{{ group.photos ? group.photos.length : 0 }}</td>
                    <td>
                      <button class="btn-edit" @click="editPhotoGroup(group)">수정</button>
                      <button class="btn-delete" @click="deletePhotoGroup(group.id)">삭제</button>
                      <button class="btn-manage" @click="togglePhotoGroupDetails(group.id)">사진 관리</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 사진 그룹 상세 (사진 목록) -->
          <div v-if="selectedPhotoGroup" class="photo-group-details">
            <h2>{{ selectedPhotoGroup.title }} - 사진 목록</h2>
            
            <!-- 사진 수정 폼 -->
            <div v-if="editingPhoto" class="photo-edit-section">
              <h3>사진 수정</h3>
              <form @submit.prevent="savePhoto" class="photo-edit-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>사진 URL *</label>
                    <input v-model="photoEditForm.imageUrl" required />
                  </div>
                  <div class="form-group">
                    <label>사진 제목</label>
                    <input v-model="photoEditForm.title" />
                  </div>
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="photoEditForm.displayOrder" />
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-save">저장</button>
                  <button type="button" class="btn-cancel" @click="cancelPhotoEdit">취소</button>
                </div>
              </form>
            </div>
            
            <div class="photo-list">
              <div v-for="photo in selectedPhotoGroup.photos" :key="photo.id" class="photo-item">
                <div class="photo-preview">
                  <img :src="photo.imageUrl" :alt="photo.title" />
                </div>
                <div class="photo-info">
                  <h4>{{ photo.title || '제목 없음' }}</h4>
                  <p>순서: {{ photo.displayOrder }}</p>
                </div>
                <div class="photo-actions">
                  <button class="btn-edit" @click="editPhoto(photo)">수정</button>
                  <button class="btn-delete" @click="deletePhoto(photo.id)">삭제</button>
                </div>
              </div>
            </div>
            <button class="btn-close" @click="closePhotoGroupDetails">닫기</button>
          </div>
        </div>

        <!-- News 탭 -->
        <div v-if="mediaTab === 'news'" class="tab-content">
          <!-- 검색/필터 섹션 -->
          <div class="search-section">
            <div class="search-filters">
              <div class="filter-group">
                <label>제목</label>
                <input v-model="newsSearchFilters.title" placeholder="제목을 입력하세요" />
              </div>
              <div class="filter-group">
                <label>날짜 범위</label>
                <div class="date-range">
                  <input type="date" v-model="newsSearchFilters.startDate" />
                  <span>~</span>
                  <input type="date" v-model="newsSearchFilters.endDate" />
                </div>
              </div>
              <div class="filter-actions">
                <button class="btn-reset" @click="resetNewsFilters">초기화</button>
                <button class="btn-search" @click="searchNews">검색</button>
              </div>
            </div>
          </div>

          <!-- 뉴스 등록/수정 폼 -->
          <div class="news-form-section">
            <h2>{{ editingNews ? '뉴스 수정' : '새 뉴스 등록' }}</h2>
            <form @submit.prevent="saveNews" class="news-form">
              <div class="form-row">
                <div class="form-group">
                  <label>날짜 *</label>
                  <input type="date" v-model="newsForm.date" required />
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="newsForm.displayOrder" />
                </div>
              </div>

              <div class="form-group">
                <label>제목 *</label>
                <input v-model="newsForm.title" required />
              </div>

              <div class="form-group">
                <label>설명</label>
                <textarea v-model="newsForm.description" rows="4"></textarea>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">{{ editingNews ? '수정' : '등록' }}</button>
                <button type="button" class="btn-cancel" @click="cancelNewsEdit" v-if="editingNews">취소</button>
              </div>
            </form>
          </div>

          <!-- 뉴스 목록 -->
          <div class="news-list">
            <h2>전체 목록</h2>
            <div class="news-table">
              <table>
                <thead>
                  <tr>
                    <th>No</th>
                    <th>날짜</th>
                    <th>제목</th>
                    <th>설명</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(news, index) in newsList" :key="news.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ formatDate(news.date) }}</td>
                    <td>{{ news.title }}</td>
                    <td>{{ news.description?.substring(0, 50) }}...</td>
                    <td>
                      <button class="btn-edit" @click="editNews(news)">수정</button>
                      <button class="btn-delete" @click="deleteNews(news.id)">삭제</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Equipment 탭 -->
        <div v-if="mediaTab === 'equipment'" class="tab-content">
          <!-- 검색/필터 섹션 -->
          <div class="search-section">
            <div class="search-filters">
              <div class="filter-group">
                <label>이름</label>
                <input v-model="equipmentSearchFilters.name" placeholder="이름을 입력하세요" />
              </div>
              <div class="filter-actions">
                <button class="btn-reset" @click="resetEquipmentFilters">초기화</button>
                <button class="btn-search" @click="searchEquipment">검색</button>
              </div>
            </div>
          </div>

          <!-- 장비 등록/수정 폼 -->
          <div class="equipment-form-section">
            <h2>{{ editingEquipment ? '장비 수정' : '새 장비 등록' }}</h2>
            <form @submit.prevent="saveEquipment" class="equipment-form">
              <div class="form-group">
                <label>이름 *</label>
                <input v-model="equipmentForm.name" required />
              </div>

              <div class="form-group">
                <label>이미지 URL</label>
                <input v-model="equipmentForm.imageUrl" />
              </div>

              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="equipmentForm.displayOrder" />
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">{{ editingEquipment ? '수정' : '등록' }}</button>
                <button type="button" class="btn-cancel" @click="cancelEquipmentEdit" v-if="editingEquipment">취소</button>
              </div>
            </form>
          </div>

          <!-- 장비 목록 -->
          <div class="equipment-list">
            <h2>전체 목록</h2>
            <div class="equipment-table">
              <table>
                <thead>
                  <tr>
                    <th>No</th>
                    <th>이름</th>
                    <th>이미지 URL</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(equipment, index) in equipmentList" :key="equipment.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ equipment.name }}</td>
                    <td>{{ equipment.imageUrl }}</td>
                    <td>
                      <button class="btn-edit" @click="editEquipment(equipment)">수정</button>
                      <button class="btn-delete" @click="deleteEquipment(equipment.id)">삭제</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Contact 관리 -->
      <div v-if="activeSection === 'contact'" class="content-section">
        <!-- 검색/필터 섹션 -->
        <div class="search-section">
          <div class="search-filters">
            <div class="filter-group">
              <label>이름</label>
              <input v-model="contactSearchFilters.name" placeholder="이름을 입력하세요" />
            </div>
            <div class="filter-group">
              <label>역할</label>
              <input v-model="contactSearchFilters.role" placeholder="역할을 입력하세요" />
            </div>
            <div class="filter-group">
              <label>이메일</label>
              <input v-model="contactSearchFilters.email" placeholder="이메일을 입력하세요" />
            </div>
            <div class="filter-actions">
              <button class="btn-reset" @click="resetContactFilters">초기화</button>
              <button class="btn-search" @click="searchContacts">검색</button>
            </div>
          </div>
        </div>

        <!-- 연락처 등록/수정 폼 -->
        <div class="contact-form-section">
          <h2>{{ editingContact ? '연락처 수정' : '새 연락처 등록' }}</h2>
          <form @submit.prevent="saveContact" class="contact-form">
            <div class="form-row">
              <div class="form-group">
                <label>이름 *</label>
                <input v-model="contactForm.name" required />
              </div>
              <div class="form-group">
                <label>역할</label>
                <input v-model="contactForm.role" />
              </div>
            </div>

            <div class="form-group">
              <label>이메일 *</label>
              <input type="email" v-model="contactForm.email" required />
            </div>

            <div class="form-group">
              <label>안내 문구</label>
              <textarea v-model="contactForm.introText" rows="3"></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Portfolio EN URL</label>
                <input v-model="contactForm.portfolioEnUrl" />
              </div>
              <div class="form-group">
                <label>Portfolio DE URL</label>
                <input v-model="contactForm.portfolioDeUrl" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Portfolio KR URL</label>
                <input v-model="contactForm.portfolioKrUrl" />
              </div>
              <div class="form-group">
                <label>Stage Rider URL</label>
                <input v-model="contactForm.stageRiderUrl" />
              </div>
            </div>

            <div class="form-group">
              <label>CV URL</label>
              <input v-model="contactForm.cvUrl" />
            </div>

            <div class="form-group">
              <label>표시 순서</label>
              <input type="number" v-model="contactForm.displayOrder" />
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-save">{{ editingContact ? '수정' : '등록' }}</button>
              <button type="button" class="btn-cancel" @click="cancelContactEdit" v-if="editingContact">취소</button>
            </div>
          </form>
        </div>

        <!-- 연락처 목록 -->
        <div class="contacts-list">
          <h2>전체 목록</h2>
          <div class="contacts-table">
            <table>
              <thead>
                <tr>
                  <th>No</th>
                  <th>이름</th>
                  <th>역할</th>
                  <th>이메일</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(contact, index) in contactList" :key="contact.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ contact.name }}</td>
                  <td>{{ contact.role }}</td>
                  <td>{{ contact.email }}</td>
                  <td>
                    <button class="btn-edit" @click="editContact(contact)">수정</button>
                    <button class="btn-delete" @click="deleteContact(contact.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'

// 활성 섹션
const activeSection = ref('dashboard')
const mediaTab = ref('music')

// Projects
const projects = ref([])
const editingProject = ref(null)
const searchFilters = ref({
  title: '',
  location: '',
  startDate: '',
  endDate: ''
})
const projectForm = ref({
  title: '',
  subtitle: '',
  premiereDate: '',
  location: '',
  description1: '',
  description2: '',
  description3: '',
  mainImageUrl: '',
  horizontal1ImageUrl: '',
  horizontal2ImageUrl: '',
  vertical1ImageUrl: '',
  vertical2ImageUrl: '',
  coverImageUrl: '',
  moreInfoUrl: '',
  review1Text: '',
  review1Source: '',
  review2Text: '',
  review2Source: '',
  urlSlug: '',
  displayOrder: 0
})

// Concerts
const concerts = ref([])
const editingConcert = ref(null)
const concertSearchFilters = ref({
  name: '',
  location: '',
  startDate: '',
  endDate: ''
})
const concertForm = ref({
  date: '',
  location: '',
  name: '',
  startTime: '',
  ticketInfo: '',
  fullLocation: '',
  googleMapUrl: '',
  collaborationInfo: '',
  isPast: false
})

// Music
const musicList = ref([])
const editingMusic = ref(null)
const musicSearchFilters = ref({
  title: '',
  artist: ''
})
const musicForm = ref({
  title: '',
  artist: '',
  coverUrl: '',
  linkUrl: '',
  displayOrder: 0
})

// Videos
const videos = ref([])
const editingVideo = ref(null)
const videoSearchFilters = ref({
  title: ''
})
const videoForm = ref({
  title: '',
  embedUrl: '',
  displayOrder: 0
})

// PhotoGroups
const photoGroups = ref([])
const editingPhotoGroup = ref(null)
const photoGroupSearchFilters = ref({
  title: ''
})
const photoGroupForm = ref({
  title: '',
  displayOrder: 0
})

// Photos
const selectedPhotoGroup = ref(null)
const photoAddForm = ref({
  photoGroupId: '',
  imageUrl: '',
  title: '',
  displayOrder: 0
})
const editingPhoto = ref(null)
const photoEditForm = ref({
  id: null,
  imageUrl: '',
  title: '',
  displayOrder: 0
})

// News
const newsList = ref([])
const editingNews = ref(null)
const newsSearchFilters = ref({
  title: '',
  startDate: '',
  endDate: ''
})
const newsForm = ref({
  date: '',
  title: '',
  description: '',
  displayOrder: 0
})

// Equipment
const equipmentList = ref([])
const editingEquipment = ref(null)
const equipmentSearchFilters = ref({
  name: ''
})
const equipmentForm = ref({
  name: '',
  imageUrl: '',
  displayOrder: 0
})

// Contact
const contactList = ref([])
const editingContact = ref(null)
const contactSearchFilters = ref({
  name: '',
  role: '',
  email: ''
})
const contactForm = ref({
  name: '',
  role: '',
  email: '',
  introText: '',
  portfolioEnUrl: '',
  portfolioDeUrl: '',
  portfolioKrUrl: '',
  stageRiderUrl: '',
  cvUrl: '',
  displayOrder: 0
})

// API 호출 함수들
const loadProjects = async () => {
  try {
    const response = await axios.get('/api/projects')
    projects.value = response.data
  } catch (error) {
    console.error('프로젝트 로드 실패:', error)
  }
}

// 프로젝트 검색
const searchProjects = async () => {
  try {
    const params = new URLSearchParams()
    if (searchFilters.value.title) params.append('title', searchFilters.value.title)
    if (searchFilters.value.location) params.append('location', searchFilters.value.location)
    if (searchFilters.value.startDate) params.append('startDate', searchFilters.value.startDate)
    if (searchFilters.value.endDate) params.append('endDate', searchFilters.value.endDate)
    
    const response = await axios.get(`/api/projects?${params.toString()}`)
    projects.value = response.data
  } catch (error) {
    console.error('프로젝트 검색 실패:', error)
  }
}

// 필터 초기화
const resetFilters = () => {
  searchFilters.value = {
    title: '',
    location: '',
    startDate: '',
    endDate: ''
  }
  loadProjects()
}

const editProject = (project) => {
  editingProject.value = project
  projectForm.value = { ...project }
}

// 수정 취소
const cancelEdit = () => {
  editingProject.value = null
  projectForm.value = {
    title: '',
    subtitle: '',
    premiereDate: '',
    location: '',
    description1: '',
    description2: '',
    description3: '',
    mainImageUrl: '',
    horizontal1ImageUrl: '',
    horizontal2ImageUrl: '',
    vertical1ImageUrl: '',
    vertical2ImageUrl: '',
    coverImageUrl: '',
    moreInfoUrl: '',
    review1Text: '',
    review1Source: '',
    review2Text: '',
    review2Source: '',
    urlSlug: '',
    displayOrder: 0
  }
}

const saveProject = async () => {
  try {
    if (editingProject.value) {
      await axios.put(`/api/projects/${editingProject.value.id}`, projectForm.value)
    } else {
      await axios.post('/api/projects', projectForm.value)
    }
    cancelEdit()
    loadProjects()
  } catch (error) {
    console.error('프로젝트 저장 실패:', error)
  }
}

const deleteProject = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/projects/${id}`)
      loadProjects()
    } catch (error) {
      console.error('프로젝트 삭제 실패:', error)
    }
  }
}

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const loadConcerts = async () => {
  try {
    const response = await axios.get('/api/concerts')
    concerts.value = response.data
  } catch (error) {
    console.error('콘서트 로드 실패:', error)
  }
}

// 콘서트 검색
const searchConcerts = async () => {
  try {
    const params = new URLSearchParams()
    if (concertSearchFilters.value.name) params.append('name', concertSearchFilters.value.name)
    if (concertSearchFilters.value.location) params.append('location', concertSearchFilters.value.location)
    if (concertSearchFilters.value.startDate) params.append('startDate', concertSearchFilters.value.startDate)
    if (concertSearchFilters.value.endDate) params.append('endDate', concertSearchFilters.value.endDate)
    
    const response = await axios.get(`/api/concerts?${params.toString()}`)
    concerts.value = response.data
  } catch (error) {
    console.error('콘서트 검색 실패:', error)
  }
}

// 콘서트 필터 초기화
const resetConcertFilters = () => {
  concertSearchFilters.value = {
    name: '',
    location: '',
    startDate: '',
    endDate: ''
  }
  loadConcerts()
}

const editConcert = (concert) => {
  editingConcert.value = concert
  concertForm.value = { ...concert }
}

// 콘서트 수정 취소
const cancelConcertEdit = () => {
  editingConcert.value = null
  concertForm.value = {
    date: '',
    location: '',
    name: '',
    startTime: '',
    ticketInfo: '',
    fullLocation: '',
    googleMapUrl: '',
    collaborationInfo: '',
    isPast: false
  }
}

const saveConcert = async () => {
  try {
    if (editingConcert.value) {
      await axios.put(`/api/concerts/${editingConcert.value.id}`, concertForm.value)
    } else {
      await axios.post('/api/concerts', concertForm.value)
    }
    cancelConcertEdit()
    loadConcerts()
  } catch (error) {
    console.error('콘서트 저장 실패:', error)
  }
}

const deleteConcert = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/concerts/${id}`)
      loadConcerts()
    } catch (error) {
      console.error('콘서트 삭제 실패:', error)
    }
  }
}

const loadMusic = async () => {
  try {
    const response = await axios.get('/api/media/music')
    musicList.value = response.data
  } catch (error) {
    console.error('음악 로드 실패:', error)
  }
}

// 음악 검색
const searchMusic = async () => {
  try {
    const params = new URLSearchParams()
    if (musicSearchFilters.value.title) params.append('title', musicSearchFilters.value.title)
    if (musicSearchFilters.value.artist) params.append('artist', musicSearchFilters.value.artist)
    
    const response = await axios.get(`/api/media/music?${params.toString()}`)
    musicList.value = response.data
  } catch (error) {
    console.error('음악 검색 실패:', error)
  }
}

// 음악 필터 초기화
const resetMusicFilters = () => {
  musicSearchFilters.value = {
    title: '',
    artist: ''
  }
  loadMusic()
}

const editMusic = (music) => {
  editingMusic.value = music
  musicForm.value = { ...music }
}

// 음악 수정 취소
const cancelMusicEdit = () => {
  editingMusic.value = null
  musicForm.value = {
    title: '',
    artist: '',
    coverUrl: '',
    linkUrl: '',
    displayOrder: 0
  }
}

const saveMusic = async () => {
  try {
    if (editingMusic.value) {
      await axios.put(`/api/media/music/${editingMusic.value.id}`, musicForm.value)
    } else {
      await axios.post('/api/media/music', musicForm.value)
    }
    cancelMusicEdit()
    loadMusic()
  } catch (error) {
    console.error('음악 저장 실패:', error)
  }
}

const deleteMusic = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/music/${id}`)
      loadMusic()
    } catch (error) {
      console.error('음악 삭제 실패:', error)
    }
  }
}

const loadVideos = async () => {
  try {
    const response = await axios.get('/api/media/videos')
    videos.value = response.data
  } catch (error) {
    console.error('비디오 로드 실패:', error)
  }
}

// 비디오 검색
const searchVideos = async () => {
  try {
    const params = new URLSearchParams()
    if (videoSearchFilters.value.title) params.append('title', videoSearchFilters.value.title)
    
    const response = await axios.get(`/api/media/videos?${params.toString()}`)
    videos.value = response.data
  } catch (error) {
    console.error('비디오 검색 실패:', error)
  }
}

// 비디오 필터 초기화
const resetVideoFilters = () => {
  videoSearchFilters.value = {
    title: ''
  }
  loadVideos()
}

const editVideo = (video) => {
  editingVideo.value = video
  videoForm.value = { ...video }
}

// 비디오 수정 취소
const cancelVideoEdit = () => {
  editingVideo.value = null
  videoForm.value = {
    title: '',
    embedUrl: '',
    displayOrder: 0
  }
}

const saveVideo = async () => {
  try {
    if (editingVideo.value) {
      await axios.put(`/api/media/videos/${editingVideo.value.id}`, videoForm.value)
    } else {
      await axios.post('/api/media/videos', videoForm.value)
    }
    cancelVideoEdit()
    loadVideos()
  } catch (error) {
    console.error('비디오 저장 실패:', error)
  }
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/videos/${id}`)
      loadVideos()
    } catch (error) {
      console.error('비디오 삭제 실패:', error)
    }
  }
}

const loadPhotoGroups = async () => {
  try {
    const response = await axios.get('/api/media/photo-groups')
    photoGroups.value = response.data
  } catch (error) {
    console.error('사진 그룹 로드 실패:', error)
  }
}

// 사진 그룹 검색
const searchPhotoGroups = async () => {
  try {
    const params = new URLSearchParams()
    if (photoGroupSearchFilters.value.title) params.append('title', photoGroupSearchFilters.value.title)
    
    const response = await axios.get(`/api/media/photo-groups?${params.toString()}`)
    photoGroups.value = response.data
  } catch (error) {
    console.error('사진 그룹 검색 실패:', error)
  }
}

// 사진 그룹 필터 초기화
const resetPhotoGroupFilters = () => {
  photoGroupSearchFilters.value = {
    title: ''
  }
  loadPhotoGroups()
}

const editPhotoGroup = (group) => {
  editingPhotoGroup.value = group
  photoGroupForm.value = { title: group.title, displayOrder: group.displayOrder }
}

// 사진 그룹 수정 취소
const cancelPhotoGroupEdit = () => {
  editingPhotoGroup.value = null
  photoGroupForm.value = {
    title: '',
    displayOrder: 0
  }
}

const savePhotoGroup = async () => {
  try {
    if (editingPhotoGroup.value) {
      await axios.put(`/api/media/photo-groups/${editingPhotoGroup.value.id}`, photoGroupForm.value)
    } else {
      await axios.post('/api/media/photo-groups', photoGroupForm.value)
    }
    cancelPhotoGroupEdit()
    loadPhotoGroups()
  } catch (error) {
    console.error('사진 그룹 저장 실패:', error)
  }
}

const deletePhotoGroup = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photo-groups/${id}`)
      loadPhotoGroups()
    } catch (error) {
      console.error('사진 그룹 삭제 실패:', error)
    }
  }
}

// 사진 그룹 상세 보기 토글
const togglePhotoGroupDetails = (groupId) => {
  if (selectedPhotoGroup.value && selectedPhotoGroup.value.id === groupId) {
    selectedPhotoGroup.value = null
  } else {
    const group = photoGroups.value.find(g => g.id === groupId)
    if (group) {
      selectedPhotoGroup.value = group
    }
  }
}

// 사진 그룹 상세 보기 닫기
const closePhotoGroupDetails = () => {
  selectedPhotoGroup.value = null
}

// 사진 추가 폼 초기화
const resetPhotoAddForm = () => {
  photoAddForm.value = {
    photoGroupId: '',
    imageUrl: '',
    title: '',
    displayOrder: 0
  }
}

// 사진 추가
const addPhotoToGroup = async () => {
  try {
    await axios.post('/api/media/photos', photoAddForm.value)
    resetPhotoAddForm()
    loadPhotoGroups()
    // 선택된 그룹이 있으면 해당 그룹의 사진 목록도 새로고침
    if (selectedPhotoGroup.value) {
      const updatedGroup = photoGroups.value.find(g => g.id === selectedPhotoGroup.value.id)
      if (updatedGroup) {
        selectedPhotoGroup.value = updatedGroup
      }
    }
    alert('사진이 추가되었습니다.')
  } catch (error) {
    console.error('사진 추가 실패:', error)
    alert('사진 추가에 실패했습니다.')
  }
}

// 사진 수정
const editPhoto = (photo) => {
  editingPhoto.value = photo
  photoEditForm.value = {
    id: photo.id,
    imageUrl: photo.imageUrl,
    title: photo.title,
    displayOrder: photo.displayOrder
  }
}

// 사진 수정 취소
const cancelPhotoEdit = () => {
  editingPhoto.value = null
  photoEditForm.value = {
    id: null,
    imageUrl: '',
    title: '',
    displayOrder: 0
  }
}

// 사진 저장
const savePhoto = async () => {
  try {
    await axios.put(`/api/media/photos/${photoEditForm.value.id}`, photoEditForm.value)
    cancelPhotoEdit()
    loadPhotoGroups()
    // 선택된 그룹이 있으면 해당 그룹의 사진 목록도 새로고침
    if (selectedPhotoGroup.value) {
      const updatedGroup = photoGroups.value.find(g => g.id === selectedPhotoGroup.value.id)
      if (updatedGroup) {
        selectedPhotoGroup.value = updatedGroup
      }
    }
    alert('사진이 수정되었습니다.')
  } catch (error) {
    console.error('사진 수정 실패:', error)
    alert('사진 수정에 실패했습니다.')
  }
}

// 사진 삭제
const deletePhoto = async (photoId) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photos/${photoId}`)
      loadPhotoGroups()
      // 선택된 그룹이 있으면 해당 그룹의 사진 목록도 새로고침
      if (selectedPhotoGroup.value) {
        const updatedGroup = photoGroups.value.find(g => g.id === selectedPhotoGroup.value.id)
        if (updatedGroup) {
          selectedPhotoGroup.value = updatedGroup
        }
      }
      alert('사진이 삭제되었습니다.')
    } catch (error) {
      console.error('사진 삭제 실패:', error)
      alert('사진 삭제에 실패했습니다.')
    }
  }
}

const loadNews = async () => {
  try {
    const response = await axios.get('/api/media/news')
    newsList.value = response.data
  } catch (error) {
    console.error('뉴스 로드 실패:', error)
  }
}

// 뉴스 검색
const searchNews = async () => {
  try {
    const params = new URLSearchParams()
    if (newsSearchFilters.value.title) params.append('title', newsSearchFilters.value.title)
    if (newsSearchFilters.value.startDate) params.append('startDate', newsSearchFilters.value.startDate)
    if (newsSearchFilters.value.endDate) params.append('endDate', newsSearchFilters.value.endDate)
    
    const response = await axios.get(`/api/media/news?${params.toString()}`)
    newsList.value = response.data
  } catch (error) {
    console.error('뉴스 검색 실패:', error)
  }
}

// 뉴스 필터 초기화
const resetNewsFilters = () => {
  newsSearchFilters.value = {
    title: '',
    startDate: '',
    endDate: ''
  }
  loadNews()
}

const editNews = (news) => {
  editingNews.value = news
  newsForm.value = { ...news }
}

// 뉴스 수정 취소
const cancelNewsEdit = () => {
  editingNews.value = null
  newsForm.value = {
    date: '',
    title: '',
    description: '',
    displayOrder: 0
  }
}

const saveNews = async () => {
  try {
    if (editingNews.value) {
      await axios.put(`/api/media/news/${editingNews.value.id}`, newsForm.value)
    } else {
      await axios.post('/api/media/news', newsForm.value)
    }
    cancelNewsEdit()
    loadNews()
  } catch (error) {
    console.error('뉴스 저장 실패:', error)
  }
}

const deleteNews = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/news/${id}`)
      loadNews()
    } catch (error) {
      console.error('뉴스 삭제 실패:', error)
    }
  }
}

const loadEquipment = async () => {
  try {
    const response = await axios.get('/api/media/equipment')
    equipmentList.value = response.data
  } catch (error) {
    console.error('장비 로드 실패:', error)
  }
}

// 장비 검색
const searchEquipment = async () => {
  try {
    const params = new URLSearchParams()
    if (equipmentSearchFilters.value.name) params.append('name', equipmentSearchFilters.value.name)
    
    const response = await axios.get(`/api/media/equipment?${params.toString()}`)
    equipmentList.value = response.data
  } catch (error) {
    console.error('장비 검색 실패:', error)
  }
}

// 장비 필터 초기화
const resetEquipmentFilters = () => {
  equipmentSearchFilters.value = {
    name: ''
  }
  loadEquipment()
}

const editEquipment = (equipment) => {
  editingEquipment.value = equipment
  equipmentForm.value = { ...equipment }
}

// 장비 수정 취소
const cancelEquipmentEdit = () => {
  editingEquipment.value = null
  equipmentForm.value = {
    name: '',
    imageUrl: '',
    displayOrder: 0
  }
}

const saveEquipment = async () => {
  try {
    if (editingEquipment.value) {
      await axios.put(`/api/media/equipment/${editingEquipment.value.id}`, equipmentForm.value)
    } else {
      await axios.post('/api/media/equipment', equipmentForm.value)
    }
    cancelEquipmentEdit()
    loadEquipment()
  } catch (error) {
    console.error('장비 저장 실패:', error)
  }
}

const deleteEquipment = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/equipment/${id}`)
      loadEquipment()
    } catch (error) {
      console.error('장비 삭제 실패:', error)
    }
  }
}

const loadContact = async () => {
  try {
    const response = await axios.get('/api/contact')
    contactList.value = response.data
  } catch (error) {
    console.error('연락처 로드 실패:', error)
  }
}

// 연락처 검색
const searchContacts = async () => {
  try {
    const params = new URLSearchParams()
    if (contactSearchFilters.value.name) params.append('name', contactSearchFilters.value.name)
    if (contactSearchFilters.value.role) params.append('role', contactSearchFilters.value.role)
    if (contactSearchFilters.value.email) params.append('email', contactSearchFilters.value.email)
    
    const response = await axios.get(`/api/contact?${params.toString()}`)
    contactList.value = response.data
  } catch (error) {
    console.error('연락처 검색 실패:', error)
  }
}

// 연락처 필터 초기화
const resetContactFilters = () => {
  contactSearchFilters.value = {
    name: '',
    role: '',
    email: ''
  }
  loadContact()
}

const editContact = (contact) => {
  editingContact.value = contact
  contactForm.value = { ...contact }
}

// 연락처 수정 취소
const cancelContactEdit = () => {
  editingContact.value = null
  contactForm.value = {
    name: '',
    role: '',
    email: '',
    introText: '',
    portfolioEnUrl: '',
    portfolioDeUrl: '',
    portfolioKrUrl: '',
    stageRiderUrl: '',
    cvUrl: '',
    displayOrder: 0
  }
}

const saveContact = async () => {
  try {
    if (editingContact.value) {
      await axios.put(`/api/contact/${editingContact.value.id}`, contactForm.value)
    } else {
      await axios.post('/api/contact', contactForm.value)
    }
    cancelContactEdit()
    loadContact()
  } catch (error) {
    console.error('연락처 저장 실패:', error)
  }
}

const deleteContact = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/contact/${id}`)
      loadContact()
    } catch (error) {
      console.error('연락처 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  loadProjects()
  loadConcerts()
  loadMusic()
  loadVideos()
  loadPhotoGroups()
  loadNews()
  loadEquipment()
  loadContact()
})
</script>

<style scoped>
.admin {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
  overflow: hidden;
}

/* 사이드바 */
.admin-sidebar {
  background: #2c3e50;
  color: white;
  padding: 2rem;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 2rem;
  color: #ecf0f1;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  color: #bdc3c7;
  text-align: left;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  font-size: 1rem;
}

.nav-item:hover {
  background: #34495e;
  color: white;
}

.nav-item.active {
  background: #3498db;
  color: white;
}

/* 메인 콘텐츠 */
.admin-content {
  flex: 1;
  padding: 2rem;
  height: 100vh;
  overflow-y: auto;
}

.content-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

/* Dashboard */
.dashboard-card {
  background: #ecf0f1;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.dashboard-card h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.stats-placeholder {
  background: white;
  padding: 2rem;
  border-radius: 4px;
  text-align: center;
  color: #7f8c8d;
}

/* 탭 */
.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #ecf0f1;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  font-size: 1rem;
}

.tab:hover {
  color: #2c3e50;
}

.tab.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.tab-content {
  margin-top: 2rem;
}

/* 테이블 */
.data-table {
  overflow-x: auto;
  margin-top: 1rem;
}

table {
  border-collapse: collapse;
}

thead {
  background: #ecf0f1;
}

th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #ecf0f1;
}

tbody tr:hover {
  background: #f8f9fa;
}

/* 버튼 */
.btn-primary {
  padding: 0.75rem 1.5rem;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-edit {
  padding: 0.5rem 1rem;
  background: #f39c12;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
  transition: background 0.3s;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-delete:hover {
  background: #c0392b;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow-y: auto;
  padding: 2rem;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

/* 검색 섹션 */
.search-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.search-filters {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 0.9rem;
}

.filter-group input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-range input {
  flex: 1;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-reset, .btn-search {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-reset {
  background: #95a5a6;
  color: white;
}

.btn-reset:hover {
  background: #7f8c8d;
}

.btn-search {
  background: #3498db;
  color: white;
}

.btn-search:hover {
  background: #2980b9;
}

/* 프로젝트 폼 섹션 */
.project-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.project-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.project-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 0.9rem;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
}

.form-group input[type="checkbox"] {
  margin-right: 0.5rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-save {
  padding: 0.75rem 2rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-save:hover {
  background: #229954;
}

.btn-cancel {
  padding: 0.75rem 2rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

/* 프로젝트 목록 */
.projects-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.projects-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.projects-table {
  overflow-x: auto;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  background: #e74c3c;
  color: white;
}

.status-badge.active {
  background: #27ae60;
}

.status-badge.past {
  background: #95a5a6;
}

/* 콘서트 폼 섹션 */
.concert-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.concert-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.concert-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 콘서트 목록 */
.concerts-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.concerts-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.concerts-table {
  overflow-x: auto;
}

/* 연락처 폼 섹션 */
.contact-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.contact-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.contact-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 연락처 목록 */
.contacts-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.contacts-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.contacts-table {
  overflow-x: auto;
}

/* 음악 폼 섹션 */
.music-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.music-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.music-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 음악 목록 */
.music-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.music-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.music-table {
  overflow-x: auto;
}

/* 비디오 폼 섹션 */
.video-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.video-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.video-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 비디오 목록 */
.videos-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.videos-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.videos-table {
  overflow-x: auto;
}

/* 사진 그룹 폼 섹션 */
.photo-group-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.photo-group-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.photo-group-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 사진 그룹 목록 */
.photo-groups-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.photo-groups-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.photo-groups-table {
  overflow-x: auto;
}

/* 뉴스 폼 섹션 */
.news-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.news-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.news-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 뉴스 목록 */
.news-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.news-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.news-table {
  overflow-x: auto;
}

/* 장비 폼 섹션 */
.equipment-form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.equipment-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.equipment-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 장비 목록 */
.equipment-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  border: 1px solid #e9ecef;
}

.equipment-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.equipment-table {
  overflow-x: auto;
}

/* 사진 추가 섹션 */
.photo-add-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.photo-add-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.photo-add-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 사진 그룹 상세 */
.photo-group-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 2rem;
  margin-top: 2rem;
  border: 1px solid #e9ecef;
}

.photo-group-details h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

/* 사진 수정 섹션 */
.photo-edit-section {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #dee2e6;
}

.photo-edit-section h3 {
  margin-bottom: 1rem;
  color: #495057;
  font-size: 1.2rem;
}

.photo-edit-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 사진 목록 */
.photo-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.photo-item {
  background: #fff;
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid #dee2e6;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.photo-preview {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
}

.photo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-info h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.photo-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.photo-actions {
  display: flex;
  gap: 0.5rem;
}

.photo-actions button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.photo-actions .btn-edit {
  background: #007bff;
  color: white;
}

.photo-actions .btn-delete {
  background: #dc3545;
  color: white;
}

.photo-actions button:hover {
  opacity: 0.8;
}

.btn-manage {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-manage:hover {
  opacity: 0.8;
}

.btn-close {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-close:hover {
  opacity: 0.8;
}

/* 반응형 */
@media (max-width: 768px) {
  .search-filters {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .filter-actions {
    justify-content: stretch;
  }
  
  .btn-reset, .btn-search {
    flex: 1;
  }
}
</style>
