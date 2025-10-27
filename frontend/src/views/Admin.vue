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
        <button 
          :class="['nav-item', { active: activeSection === 'download-files' }]"
          @click="activeSection = 'download-files'">
          Download Files
        </button>
      </nav>
    </aside>

    <!-- 메인 콘텐츠 영역 -->
    <main class="admin-content">
      <!-- Dashboard -->
      <div v-if="activeSection === 'dashboard'" class="content-section">
        <h1 class="section-title">Dashboard</h1>
        
        <!-- 통계 카드들 -->
        <div class="dashboard-stats">
          <div class="stat-card">
            <div class="stat-icon">🎵</div>
            <div class="stat-content">
              <h3>콘서트</h3>
              <p class="stat-number">{{ concerts.length }}</p>
              <p class="stat-label">총 콘서트 수</p>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon">📸</div>
            <div class="stat-content">
              <h3>사진 그룹</h3>
              <p class="stat-number">{{ photoGroups.length }}</p>
              <p class="stat-label">총 사진 그룹 수</p>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon">🎬</div>
            <div class="stat-content">
              <h3>미디어</h3>
              <p class="stat-number">{{ musicList.length + videos.length + newsList.length + equipmentList.length }}</p>
              <p class="stat-label">총 미디어 수</p>
            </div>
          </div>
        </div>

        <!-- 최근 활동 -->
        <div class="dashboard-section">
          <h2>최근 활동</h2>
          <div class="activity-list">
            <div class="activity-item">
              <div class="activity-icon">🎵</div>
              <div class="activity-content">
                <h4>콘서트 관리</h4>
                <p>예정된 콘서트: {{ concerts.filter(c => !c.isPast).length }}개</p>
                <p>지난 콘서트: {{ concerts.filter(c => c.isPast).length }}개</p>
              </div>
            </div>
            
            <div class="activity-item">
              <div class="activity-icon">📸</div>
              <div class="activity-content">
                <h4>사진 관리</h4>
                <p>총 사진 그룹: {{ photoGroups.length }}개</p>
                <p>총 사진 수: {{ photoGroups.reduce((total, group) => total + (group.photos ? group.photos.length : 0), 0) }}개</p>
              </div>
            </div>
            
            <div class="activity-item">
              <div class="activity-icon">🎬</div>
              <div class="activity-content">
                <h4>미디어 관리</h4>
                <p>음악: {{ musicList.length }}개</p>
                <p>비디오: {{ videos.length }}개</p>
                <p>사진 그룹: {{ photoGroups.length }}개</p>
                <p>뉴스: {{ newsList.length }}개</p>
                <p>장비: {{ equipmentList.length }}개</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 빠른 작업 -->
        <div class="dashboard-section">
          <h2>빠른 작업</h2>
          <div class="quick-actions">
            <button class="quick-action-btn" @click="activeSection = 'concerts'">
              <div class="quick-action-icon">🎵</div>
              <span>새 콘서트 추가</span>
            </button>
            
            <button class="quick-action-btn" @click="activeSection = 'media'; mediaTab = 'photos'">
              <div class="quick-action-icon">📸</div>
              <span>사진 업로드</span>
            </button>
            
            <button class="quick-action-btn" @click="activeSection = 'media'; mediaTab = 'music'">
              <div class="quick-action-icon">🎬</div>
              <span>음악 추가</span>
            </button>
          </div>
        </div>

        <!-- 시스템 상태 -->
        <div class="dashboard-section">
          <h2>시스템 상태</h2>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">데이터베이스 연결</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
            <div class="status-item">
              <span class="status-label">파일 업로드</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
            <div class="status-item">
              <span class="status-label">API 서버</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Concerts 관리 -->
      <div v-if="activeSection === 'concerts'" class="content-section">
        <h1 class="section-title">Concerts 관리</h1>
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

        <!-- 콘서트 목록 -->
        <div class="concerts-list">
          <div class="concerts-header">
            <h2>전체 목록</h2>
            <button class="btn-auto-move" @click="triggerAutoMove">
              자동 이동 실행
            </button>
          </div>
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
                    <button 
                      v-if="!concert.isPast" 
                      class="btn-move-past" 
                      @click="moveToPastEvent(concert.id)"
                    >
                      Past Event로 이동
                    </button>
                    <button 
                      v-if="concert.isPast" 
                      class="btn-move-upcoming" 
                      @click="moveToUpcomingEvent(concert.id)"
                    >
                      Upcoming으로 이동
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
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
                <label>커버 이미지 *</label>
                <div class="file-upload-container">
                  <input type="file" @change="handleMusicFileUpload($event)" accept="image/*" class="file-input" id="musicCover">
                  <label for="musicCover" class="file-upload-btn">이미지 업로드</label>
                  <div v-if="musicForm.coverUrl" class="image-preview">
                    <img :src="musicForm.coverUrl" alt="커버 이미지 미리보기" />
                  </div>
                </div>
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

          <!-- 비디오 등록/수정 폼 -->
          <div class="video-form-section">
            <h2>{{ editingVideo ? '비디오 수정' : '새 비디오 등록' }}</h2>
            <form @submit.prevent="saveVideo" class="video-form">
              <div class="form-group">
                <label>제목 *</label>
                <input v-model="videoForm.title" required />
              </div>

              <div class="form-group">
                <label>YouTube Embed URL * (iframe 코드 또는 URL)</label>
                <textarea v-model="videoForm.embedUrl" rows="3" required placeholder="https://www.youtube.com/embed/... 또는 iframe 전체 코드"></textarea>
                <small style="color: #666;">※ iframe 전체 코드를 붙여넣으면 자동으로 URL을 추출합니다</small>
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
                    <label>사진 파일</label>
                    <div class="file-upload-container">
                      <input type="file" @change="handlePhotoEditFileUpload($event)" accept="image/*" class="file-input" id="photoEditFile">
                      <label for="photoEditFile" class="file-upload-btn">이미지 변경</label>
                      <div v-if="photoEditForm.imageUrl" class="image-preview">
                        <img :src="toAbsoluteUrl(photoEditForm.imageUrl)" alt="사진 미리보기" />
                      </div>
                    </div>
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
                  <img :src="toAbsoluteUrl(photo.imageUrl)" :alt="photo.title" />
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
                  <label>사진 파일 * (여러 개 선택 가능)</label>
                  <div class="file-upload-container">
                    <input type="file" @change="handleMultiplePhotoFileUpload($event)" accept="image/*" class="file-input" id="photoFile" multiple required>
                    <label for="photoFile" class="file-upload-btn">이미지 업로드 (여러 개 선택 가능)</label>
                    <div v-if="selectedPhotoFiles.length > 0" class="selected-files-info">
                      <p>선택된 파일: {{ selectedPhotoFiles.length }}개</p>
                      <div class="multiple-image-preview">
                        <div v-for="(preview, index) in photoPreviewUrls" :key="index" class="preview-item">
                          <img :src="preview" :alt="`사진 미리보기 ${index + 1}`" />
                          <button type="button" class="btn-remove-preview" @click="removePhotoFile(index)">×</button>
                        </div>
                      </div>
                    </div>
                  </div>
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
                <button type="submit" class="btn-save" :disabled="isUploadingPhotos">
                  {{ isUploadingPhotos ? '업로드 중...' : '사진 추가' }}
                </button>
                <button type="button" class="btn-cancel" @click="resetPhotoAddForm">초기화</button>
              </div>
            </form>
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

          <!-- 장비 등록/수정 폼 -->
          <div class="equipment-form-section">
            <h2>{{ editingEquipment ? '장비 수정' : '새 장비 등록' }}</h2>
            <form @submit.prevent="saveEquipment" class="equipment-form">
              <div class="form-group">
                <label>이름 *</label>
                <input v-model="equipmentForm.name" required />
              </div>

              <div class="form-group">
                <label>이미지</label>
                <div class="file-upload-container">
                  <input type="file" @change="handleEquipmentFileUpload($event)" accept="image/*" class="file-input" id="equipmentImage">
                  <label for="equipmentImage" class="file-upload-btn">이미지 업로드</label>
                  <div v-if="equipmentForm.imageUrl" class="image-preview">
                    <img :src="equipmentForm.imageUrl" alt="장비 이미지 미리보기" />
                  </div>
                </div>
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
        </div>
      </div>

      <!-- Contact 관리 -->
      <div v-if="activeSection === 'contact'" class="content-section">
        <h1 class="section-title">Contact 관리</h1>
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

          <!-- 연락처 등록/수정 폼 -->
        <div class="contact-form-section">
          <h2>{{ editingContact ? '연락처 수정' : '새 연락처 등록' }}</h2>
          <form @submit.prevent="handleContactSubmit" class="contact-form">
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
              <label>표시 순서</label>
              <input type="number" v-model="contactForm.displayOrder" />
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-save">{{ editingContact ? '수정' : '등록' }}</button>
              <button type="button" class="btn-cancel" @click="cancelContactEdit" v-if="editingContact">취소</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Download Files 관리 -->
      <div v-if="activeSection === 'download-files'" class="content-section">
        <h1 class="section-title">Download Files 관리</h1>
        
        <!-- 검색/필터 섹션 -->
        <div class="search-section">
          <div class="search-filters">
            <div class="filter-group">
              <label>파일명</label>
              <input v-model="downloadFileSearchFilters.name" placeholder="파일명을 입력하세요" />
            </div>
            <div class="filter-actions">
              <button class="btn-reset" @click="resetDownloadFileFilters">초기화</button>
              <button class="btn-search" @click="searchDownloadFiles">검색</button>
            </div>
          </div>
        </div>

        <!-- 다운로드 파일 목록 -->
        <div class="download-files-list">
          <h2>전체 목록</h2>
          <div class="download-files-table">
            <table>
              <thead>
                <tr>
                  <th>No</th>
                  <th>파일명</th>
                  <th>파일 URL</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(file, index) in downloadFileList" :key="file.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ file.name }}</td>
                  <td>{{ file.fileUrl }}</td>
                  <td>
                    <button class="btn-edit" @click="editDownloadFile(file)">수정</button>
                    <button class="btn-delete" @click="deleteDownloadFile(file.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 다운로드 파일 등록/수정 폼 -->
        <div class="download-file-form-section">
          <h2>{{ editingDownloadFile ? '다운로드 파일 수정' : '새 다운로드 파일 등록' }}</h2>
          <form @submit.prevent="handleDownloadFileSubmit" class="download-file-form">
            <div class="form-group">
              <label>표시 제목 * (사용자에게 보여질 제목)</label>
              <input v-model="downloadFileForm.name" required placeholder="예: 공연 포스터, 악보 등" />
            </div>

            <div class="form-group">
              <label>파일 업로드 *</label>
              <div class="file-upload-container">
                <input type="file" @change="handleDownloadFileUpload($event)" accept=".pdf" class="file-input" id="downloadFile">
                <label for="downloadFile" class="file-upload-btn">PDF 파일 업로드</label>
                <div v-if="downloadFileForm.fileUrl" class="file-preview">
                  <p>업로드된 파일: {{ downloadFileForm.name }}</p>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>표시 순서</label>
              <input type="number" v-model="downloadFileForm.displayOrder" />
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-save">{{ editingDownloadFile ? '수정' : '등록' }}</button>
              <button type="button" class="btn-cancel" @click="cancelDownloadFileEdit" v-if="editingDownloadFile">취소</button>
            </div>
          </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'
import {
  createConcertSearchFilters,
  createConcertForm,
  resetConcertSearchFilters,
  resetConcertForm,
  createMusicSearchFilters,
  createMusicForm,
  resetMusicSearchFilters,
  resetMusicForm,
  createVideoSearchFilters,
  createVideoForm,
  resetVideoSearchFilters,
  resetVideoForm,
  createPhotoGroupSearchFilters,
  createPhotoGroupForm,
  resetPhotoGroupSearchFilters,
  resetPhotoGroupForm,
  createPhotoAddForm,
  resetPhotoAddFormData,
  createPhotoEditForm,
  resetPhotoEditForm,
  createNewsSearchFilters,
  createNewsForm,
  resetNewsSearchFilters,
  resetNewsForm,
  createEquipmentSearchFilters,
  createEquipmentForm,
  resetEquipmentSearchFilters,
  resetEquipmentForm,
  createContactSearchFilters,
  createContactForm,
  resetContactSearchFilters,
  resetContactForm,
  createDownloadFileSearchFilters,
  createDownloadFileForm,
  resetDownloadFileSearchFilters,
  resetDownloadFileForm
} from '../types/dto'

// 백엔드 절대 URL 유틸
const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
const toAbsoluteUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

// 활성 섹션
const activeSection = ref('dashboard')
const mediaTab = ref('music')

// Concerts
const concerts = ref([])
const editingConcert = ref(null)
const concertSearchFilters = ref(createConcertSearchFilters())
const concertForm = ref(createConcertForm())

// Music
const musicList = ref([])
const editingMusic = ref(null)
const musicSearchFilters = ref(createMusicSearchFilters())
const musicForm = ref(createMusicForm())

// Videos
const videos = ref([])
const editingVideo = ref(null)
const videoSearchFilters = ref(createVideoSearchFilters())
const videoForm = ref(createVideoForm())

// PhotoGroups
const photoGroups = ref([])
const editingPhotoGroup = ref(null)
const photoGroupSearchFilters = ref(createPhotoGroupSearchFilters())
const photoGroupForm = ref(createPhotoGroupForm())

// Photos
const selectedPhotoGroup = ref(null)
const photoAddForm = ref(createPhotoAddForm())
const selectedPhotoFiles = ref([])
const photoPreviewUrls = ref([])
const isUploadingPhotos = ref(false)
const editingPhoto = ref(null)
const photoEditForm = ref(createPhotoEditForm())

// News
const newsList = ref([])
const editingNews = ref(null)
const newsSearchFilters = ref(createNewsSearchFilters())
const newsForm = ref(createNewsForm())

// Equipment
const equipmentList = ref([])
const editingEquipment = ref(null)
const equipmentSearchFilters = ref(createEquipmentSearchFilters())
const equipmentForm = ref(createEquipmentForm())

// Contact
const contactList = ref([])
const editingContact = ref(null)
const contactSearchFilters = ref(createContactSearchFilters())
const contactForm = ref(createContactForm())

// Download Files
const downloadFileList = ref([])
const editingDownloadFile = ref(null)
const downloadFileSearchFilters = ref(createDownloadFileSearchFilters())
const downloadFileForm = ref(createDownloadFileForm())

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
    // 콘서트 로드 실패
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
    // 콘서트 검색 실패
  }
}

// 콘서트 필터 초기화
const resetConcertFilters = () => {
  resetConcertSearchFilters(concertSearchFilters.value)
  loadConcerts()
}

const editConcert = (concert) => {
  editingConcert.value = concert
  concertForm.value = { ...concert }
}

// 콘서트 수정 취소
const cancelConcertEdit = () => {
  editingConcert.value = null
  resetConcertForm(concertForm.value)
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
    // 콘서트 저장 실패
  }
}

const deleteConcert = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/concerts/${id}`)
      loadConcerts()
    } catch (error) {
      // 콘서트 삭제 실패
    }
  }
}

// Past Event로 이동
const moveToPastEvent = async (id) => {
  if (confirm('이 콘서트를 Past Event로 이동하시겠습니까?')) {
    try {
      await axios.put(`/api/concerts/${id}/move-to-past`)
      loadConcerts()
      alert('Past Event로 이동되었습니다.')
    } catch (error) {
      // Past Event 이동 실패
      alert('이동에 실패했습니다.')
    }
  }
}

// Upcoming Event로 이동
const moveToUpcomingEvent = async (id) => {
  if (confirm('이 콘서트를 Upcoming Event로 이동하시겠습니까?')) {
    try {
      await axios.put(`/api/concerts/${id}/move-to-upcoming`)
      loadConcerts()
      alert('Upcoming Event로 이동되었습니다.')
    } catch (error) {
      // Upcoming Event 이동 실패
      alert('이동에 실패했습니다.')
    }
  }
}

const loadMusic = async () => {
  try {
    const response = await axios.get('/api/media/music')
    musicList.value = response.data
  } catch (error) {
    // 음악 로드 실패
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
    // 음악 검색 실패
  }
}

// 음악 필터 초기화
const resetMusicFilters = () => {
  resetMusicSearchFilters(musicSearchFilters.value)
  loadMusic()
}

const editMusic = (music) => {
  editingMusic.value = music
  musicForm.value = { ...music }
}

// 음악 수정 취소
const cancelMusicEdit = () => {
  editingMusic.value = null
  resetMusicForm(musicForm.value)
}

const saveMusic = async () => {
  try {
    let response
    if (editingMusic.value) {
      response = await axios.put(`/api/media/music/${editingMusic.value.id}`, musicForm.value)
    } else {
      response = await axios.post('/api/media/music', musicForm.value)
    }
    
    cancelMusicEdit()
    loadMusic()
  } catch (error) {
    alert('음악 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteMusic = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/music/${id}`)
      loadMusic()
    } catch (error) {
      // 음악 삭제 실패
    }
  }
}

const loadVideos = async () => {
  try {
    const response = await axios.get('/api/media/videos')
    videos.value = response.data
  } catch (error) {
    // 비디오 로드 실패
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
    // 비디오 검색 실패
  }
}

// 비디오 필터 초기화
const resetVideoFilters = () => {
  resetVideoSearchFilters(videoSearchFilters.value)
  loadVideos()
}

const editVideo = (video) => {
  editingVideo.value = video
  videoForm.value = { ...video }
}

// 비디오 수정 취소
const cancelVideoEdit = () => {
  editingVideo.value = null
  resetVideoForm(videoForm.value)
}

const saveVideo = async () => {
  try {
    // iframe 코드에서 URL 추출
    const extractedUrl = extractEmbedUrl(videoForm.value.embedUrl)
    
    const videoData = {
      ...videoForm.value,
      embedUrl: extractedUrl
    }
    
    if (editingVideo.value) {
      await axios.put(`/api/media/videos/${editingVideo.value.id}`, videoData)
    } else {
      await axios.post('/api/media/videos', videoData)
    }
    cancelVideoEdit()
    loadVideos()
  } catch (error) {
    // 비디오 저장 실패
  }
}

// iframe 코드에서 embed URL 추출
const extractEmbedUrl = (input) => {
  if (!input) return ''
  
  // 이미 순수 URL인 경우
  if (input.startsWith('https://') && !input.includes('<iframe')) {
    return input.trim()
  }
  
  // iframe 코드에서 src 추출
  const srcMatch = input.match(/src=["']([^"']+)["']/)
  if (srcMatch && srcMatch[1]) {
    return srcMatch[1]
  }
  
  return input.trim()
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/videos/${id}`)
      loadVideos()
    } catch (error) {
      // 비디오 삭제 실패
    }
  }
}

const loadPhotoGroups = async () => {
  try {
    const response = await axios.get('/api/media/photo-groups')
    photoGroups.value = response.data
  } catch (error) {
    // 사진 그룹 로드 실패
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
    // 사진 그룹 검색 실패
  }
}

// 사진 그룹 필터 초기화
const resetPhotoGroupFilters = () => {
  resetPhotoGroupSearchFilters(photoGroupSearchFilters.value)
  loadPhotoGroups()
}

const editPhotoGroup = (group) => {
  editingPhotoGroup.value = group
  photoGroupForm.value = { title: group.title, displayOrder: group.displayOrder }
}

// 사진 그룹 수정 취소
const cancelPhotoGroupEdit = () => {
  editingPhotoGroup.value = null
  resetPhotoGroupForm(photoGroupForm.value)
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
    // 사진 그룹 저장 실패
  }
}

const deletePhotoGroup = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photo-groups/${id}`)
      loadPhotoGroups()
    } catch (error) {
      // 사진 그룹 삭제 실패
    }
  }
}

// 사진 그룹 상세 보기 토글
const togglePhotoGroupDetails = async (groupId) => {
  if (selectedPhotoGroup.value && selectedPhotoGroup.value.id === groupId) {
    selectedPhotoGroup.value = null
  } else {
    try {
      // 사진 그룹 정보와 사진 목록을 함께 로드
      const [groupResponse, photosResponse] = await Promise.all([
        axios.get(`/api/media/photo-groups/${groupId}`),
        axios.get(`/api/media/photo-groups/${groupId}/photos`)
      ])
      
      selectedPhotoGroup.value = {
        ...groupResponse.data,
        photos: photosResponse.data
      }
    } catch (error) {
      alert('사진 그룹 정보를 불러오는데 실패했습니다.')
    }
  }
}

// 사진 그룹 상세 보기 닫기
const closePhotoGroupDetails = () => {
  selectedPhotoGroup.value = null
}

// 사진 추가 폼 초기화
const resetPhotoAddForm = () => {
  resetPhotoAddFormData(photoAddForm.value)
  selectedPhotoFiles.value = []
  photoPreviewUrls.value = []
  // 파일 input 초기화
  const fileInput = document.getElementById('photoFile')
  if (fileInput) {
    fileInput.value = ''
  }
}

// 사진 추가 (여러 개)
const addPhotoToGroup = async () => {
  if (selectedPhotoFiles.value.length === 0) {
    alert('업로드할 사진을 선택해주세요.')
    return
  }

  if (!photoAddForm.value.photoGroupId) {
    alert('사진 그룹을 선택해주세요.')
    return
  }

  isUploadingPhotos.value = true
  let successCount = 0
  let failCount = 0

  try {
    // 각 파일을 순차적으로 업로드
    for (let i = 0; i < selectedPhotoFiles.value.length; i++) {
      const file = selectedPhotoFiles.value[i]
      
      try {
        // 파일 업로드
            const imageUrl = await uploadImage(file)
        
            if (imageUrl) {
          // 사진 정보 저장
          await axios.post(`/api/media/photo-groups/${photoAddForm.value.photoGroupId}/photos`, {
            imageUrl: imageUrl,
            title: photoAddForm.value.title ? `${photoAddForm.value.title} ${i + 1}` : file.name,
            displayOrder: photoAddForm.value.displayOrder + i
          })
          successCount++
        } else {
          failCount++
        }
      } catch (error) {
        failCount++
      }
    }

    // 결과 메시지 (그룹 단위 1회)
    if (successCount > 0) {
      alert(`사진 그룹에 ${successCount}개의 사진이 추가되었습니다.${failCount > 0 ? ` (${failCount}개 실패)` : ''}`)
      resetPhotoAddForm()
      loadPhotoGroups()
      
      // 선택된 그룹이 있으면 해당 그룹의 사진 목록도 새로고침
      if (selectedPhotoGroup.value) {
        const updatedGroup = photoGroups.value.find(g => g.id === selectedPhotoGroup.value.id)
        if (updatedGroup) {
          selectedPhotoGroup.value = updatedGroup
        }
      }
    } else {
      alert('모든 사진 추가에 실패했습니다.')
    }
  } catch (error) {
    alert('사진 추가 중 오류가 발생했습니다.')
  } finally {
    isUploadingPhotos.value = false
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
  resetPhotoEditForm(photoEditForm.value)
}

// 사진 저장
const savePhoto = async () => {
  try {
    await axios.put(`/api/media/photos/${photoEditForm.value.id}`, photoEditForm.value)
    cancelPhotoEdit()
    loadPhotoGroups()
    
    // 선택된 그룹이 있으면 해당 그룹의 사진 목록도 새로고침
    if (selectedPhotoGroup.value) {
      try {
        const photosResponse = await axios.get(`/api/media/photo-groups/${selectedPhotoGroup.value.id}/photos`)
        selectedPhotoGroup.value.photos = photosResponse.data
      } catch (error) {
        // 사진 목록 새로고침 실패
      }
    }
    alert('사진이 수정되었습니다.')
  } catch (error) {
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
        try {
          const photosResponse = await axios.get(`/api/media/photo-groups/${selectedPhotoGroup.value.id}/photos`)
          selectedPhotoGroup.value.photos = photosResponse.data
        } catch (error) {
          // 사진 목록 새로고침 실패
        }
      }
      alert('사진이 삭제되었습니다.')
    } catch (error) {
      alert('사진 삭제에 실패했습니다.')
    }
  }
}

const loadNews = async () => {
  try {
    const response = await axios.get('/api/media/news')
    newsList.value = response.data
  } catch (error) {
    // 뉴스 로드 실패
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
    // 뉴스 검색 실패
  }
}

// 뉴스 필터 초기화
const resetNewsFilters = () => {
  resetNewsSearchFilters(newsSearchFilters.value)
  loadNews()
}

const editNews = (news) => {
  editingNews.value = news
  newsForm.value = { ...news }
}

// 뉴스 수정 취소
const cancelNewsEdit = () => {
  editingNews.value = null
  resetNewsForm(newsForm.value)
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
    // 뉴스 저장 실패
  }
}

const deleteNews = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/news/${id}`)
      loadNews()
    } catch (error) {
      // 뉴스 삭제 실패
    }
  }
}

const loadEquipment = async () => {
  try {
    const response = await axios.get('/api/media/equipment')
    equipmentList.value = response.data
  } catch (error) {
    // 장비 로드 실패
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
    // 장비 검색 실패
  }
}

// 장비 필터 초기화
const resetEquipmentFilters = () => {
  resetEquipmentSearchFilters(equipmentSearchFilters.value)
  loadEquipment()
}

const editEquipment = (equipment) => {
  editingEquipment.value = equipment
  equipmentForm.value = { ...equipment }
}

// 장비 수정 취소
const cancelEquipmentEdit = () => {
  editingEquipment.value = null
  resetEquipmentForm(equipmentForm.value)
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
    // 장비 저장 실패
  }
}

const deleteEquipment = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/equipment/${id}`)
      loadEquipment()
    } catch (error) {
      // 장비 삭제 실패
    }
  }
}

const loadContact = async () => {
  try {
    const response = await axios.get('/api/media/contacts')
    contactList.value = response.data
  } catch (error) {
    // 연락처 로드 실패
  }
}

// 연락처 검색
const searchContacts = async () => {
  try {
    const params = new URLSearchParams()
    if (contactSearchFilters.value.name) params.append('name', contactSearchFilters.value.name)
    if (contactSearchFilters.value.role) params.append('role', contactSearchFilters.value.role)
    if (contactSearchFilters.value.email) params.append('email', contactSearchFilters.value.email)
    
    const response = await axios.get(`/api/media/contacts?${params.toString()}`)
    contactList.value = response.data
  } catch (error) {
    // 연락처 검색 실패
  }
}

// 연락처 필터 초기화
const resetContactFilters = () => {
  resetContactSearchFilters(contactSearchFilters.value)
  loadContact()
}

const editContact = (contact) => {
  editingContact.value = contact
  contactForm.value = { ...contact }
}

// 연락처 수정 취소
const cancelContactEdit = () => {
  editingContact.value = null
  resetContactForm(contactForm.value)
}

// Contact 폼 제출 핸들러
const handleContactSubmit = (event) => {
  saveContact()
}

const saveContact = async () => {
  try {
    
    // 필수 필드 검증
    if (!contactForm.value.name || !contactForm.value.email) {
      alert('이름과 이메일은 필수 입력 항목입니다.')
      return
    }
    
    // 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(contactForm.value.email)) {
      alert('올바른 이메일 형식을 입력해주세요.')
      return
    }
    
    if (editingContact.value) {
      await axios.put(`/api/media/contacts/${editingContact.value.id}`, contactForm.value)
    } else {
      await axios.post('/api/media/contacts', contactForm.value)
    }
    
    cancelContactEdit()
    loadContact()
    alert('연락처가 성공적으로 저장되었습니다.')
  } catch (error) {
    alert('연락처 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteContact = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/contacts/${id}`)
      loadContact()
    } catch (error) {
      // 연락처 삭제 실패
    }
  }
}

// 자동으로 Past Event로 이동하는 함수
const autoMovePastEvents = async () => {
  try {
    const today = new Date()
    today.setHours(0, 0, 0, 0) // 오늘 자정으로 설정
    
    // 어제까지의 콘서트들을 Past Event로 이동
    const response = await axios.put('/api/concerts/auto-move-past', {
      currentDate: today.toISOString().split('T')[0]
    })
    
    if (response.data.movedCount > 0) {
      loadConcerts() // 목록 새로고침
    }
  } catch (error) {
    // 자동 이동 실패
  }
}

// 수동으로 자동 이동 실행
const triggerAutoMove = async () => {
  if (confirm('어제까지의 모든 콘서트를 Past Event로 자동 이동하시겠습니까?')) {
    await autoMovePastEvents()
    alert('자동 이동이 완료되었습니다.')
  }
}

// 파일 업로드 처리 (공통)
const uploadImage = async (file) => {
  // 파일 크기 확인 (15MB 제한)
  if (file.size > 15 * 1024 * 1024) {
    alert('파일 크기는 15MB를 초과할 수 없습니다.')
    return null
  }
  
  // 이미지 파일인지 확인
  if (!file.type.startsWith('image/')) {
    alert('이미지 파일만 업로드 가능합니다.')
    return null
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await axios.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    // 성공 시 알림은 그룹 단위로만 표시하기 위해 여기서는 반환만 합니다
    return response.data.url
  } catch (error) {
    return null
  }
}

// 일반 파일 업로드 처리 (PDF, 이미지)
const uploadFile = async (file) => {
  // 파일 크기 확인 (15MB 제한)
  if (file.size > 15 * 1024 * 1024) {
    alert('파일 크기는 15MB를 초과할 수 없습니다.')
    return null
  }
  
  // 허용된 파일 타입 확인 (PDF, 이미지)
  if (!file.type.startsWith('image/') && file.type !== 'application/pdf') {
    alert('이미지 파일 또는 PDF 파일만 업로드 가능합니다.')
    return null
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await axios.post('/api/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data.url
  } catch (error) {
    alert('파일 업로드에 실패했습니다: ' + (error.response?.data || error.message))
    return null
  }
}

// Music 커버 이미지 업로드
const handleMusicFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const url = await uploadImage(file)
  if (url) {
    musicForm.value.coverUrl = toAbsoluteUrl(url)
  }
}

// Photo 추가 파일 업로드 (여러 개)
const handleMultiplePhotoFileUpload = (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return
  
  selectedPhotoFiles.value = files
  
  // 미리보기 URL 생성
  photoPreviewUrls.value = files.map(file => URL.createObjectURL(file))
}

// 선택된 파일 제거
const removePhotoFile = (index) => {
  // 미리보기 URL 해제
  URL.revokeObjectURL(photoPreviewUrls.value[index])
  
  // 배열에서 제거
  selectedPhotoFiles.value.splice(index, 1)
  photoPreviewUrls.value.splice(index, 1)
  
  // 파일 input 업데이트 (선택된 파일들로 재설정)
  const fileInput = document.getElementById('photoFile')
  if (fileInput && selectedPhotoFiles.value.length === 0) {
    fileInput.value = ''
  }
}

// Photo 수정 파일 업로드
const handlePhotoEditFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const url = await uploadImage(file)
  if (url) {
    photoEditForm.value.imageUrl = toAbsoluteUrl(url)
  }
}

// Equipment 이미지 업로드
const handleEquipmentFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const url = await uploadImage(file)
  if (url) {
    equipmentForm.value.imageUrl = url
  }
}

// Download Files 관련 함수들
const loadDownloadFiles = async () => {
  try {
    const response = await axios.get('/api/media/download-files')
    // displayOrder 순으로 정렬
    downloadFileList.value = response.data.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0))
  } catch (error) {
    // 다운로드 파일 로드 실패
  }
}

const searchDownloadFiles = async () => {
  try {
    const params = new URLSearchParams()
    if (downloadFileSearchFilters.value.name) params.append('name', downloadFileSearchFilters.value.name)
    
    const response = await axios.get(`/api/media/download-files?${params.toString()}`)
    downloadFileList.value = response.data
  } catch (error) {
    // 다운로드 파일 검색 실패
  }
}

const resetDownloadFileFilters = () => {
  resetDownloadFileSearchFilters(downloadFileSearchFilters.value)
  loadDownloadFiles()
}

const editDownloadFile = (file) => {
  editingDownloadFile.value = file
  downloadFileForm.value = { ...file }
}

const cancelDownloadFileEdit = () => {
  editingDownloadFile.value = null
  resetDownloadFileForm(downloadFileForm.value)
}

// Download File 폼 제출 핸들러
const handleDownloadFileSubmit = (event) => {
  saveDownloadFile()
}

const saveDownloadFile = async () => {
  try {
    
    // 필수 필드 검증
    if (!downloadFileForm.value.name || !downloadFileForm.value.fileUrl) {
      alert('파일명과 파일 업로드는 필수 항목입니다.')
      return
    }
    
    if (editingDownloadFile.value) {
      await axios.put(`/api/media/download-files/${editingDownloadFile.value.id}`, downloadFileForm.value)
    } else {
      await axios.post('/api/media/download-files', downloadFileForm.value)
    }
    
    cancelDownloadFileEdit()
    loadDownloadFiles()
    alert('다운로드 파일이 성공적으로 저장되었습니다.')
  } catch (error) {
    alert('다운로드 파일 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteDownloadFile = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/download-files/${id}`)
      loadDownloadFiles()
    } catch (error) {
      // 다운로드 파일 삭제 실패
    }
  }
}

const handleDownloadFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // PDF 파일인지 확인
  if (file.type !== 'application/pdf') {
    alert('PDF 파일만 업로드 가능합니다.')
    return
  }
  
  const url = await uploadFile(file)
  if (url) {
    downloadFileForm.value.fileUrl = toAbsoluteUrl(url)
    // 파일명이 비어있을 때만 파일명으로 설정 (사용자가 입력한 제목을 유지)
    if (!downloadFileForm.value.name) {
      downloadFileForm.value.name = file.name.replace('.pdf', '')
    }
    alert('PDF 파일이 성공적으로 업로드되었습니다.')
  }
}

onMounted(() => {
  loadConcerts()
  loadMusic()
  loadVideos()
  loadPhotoGroups()
  loadNews()
  loadEquipment()
  loadContact()
  loadDownloadFiles()
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
  border-radius: 0.25rem;
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
  border-radius: 0.5rem;
  padding: 2rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,0.1);
}

.section-title {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

/* section-header 스타일은 필요시 추가 */

/* Dashboard */
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15.625rem, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 0.125rem 0.5rem rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-0.125rem);
  box-shadow: 0 0.25rem 1rem rgba(0,0,0,0.15);
}

.stat-icon {
  font-size: 2.5rem;
  width: 3.75rem;
  height: 3.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 0.75rem;
}

.stat-content h3 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #3498db;
  margin: 0;
  line-height: 1;
}

.stat-label {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.dashboard-section {
  background: white;
  border-radius: 0.75rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 0.125rem 0.5rem rgba(0,0,0,0.1);
}

.dashboard-section h2 {
  margin: 0 0 1.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  border-bottom: 0.125rem solid #ecf0f1;
  padding-bottom: 0.5rem;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(18.75rem, 1fr));
  gap: 1.5rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
  border-left: 0.25rem solid #3498db;
}

.activity-icon {
  font-size: 1.5rem;
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 0.5rem;
  flex-shrink: 0;
}

.activity-content h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.activity-content p {
  margin: 0.25rem 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12.5rem, 1fr));
  gap: 1rem;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border: 0.125rem solid #e9ecef;
  border-radius: 0.75rem;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.quick-action-btn:hover {
  background: #3498db;
  color: white;
  border-color: #3498db;
  transform: translateY(-0.125rem);
}

.quick-action-icon {
  font-size: 2rem;
}

.quick-action-btn span {
  font-weight: 600;
  font-size: 0.9rem;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.status-label {
  flex: 1;
  font-weight: 500;
  color: #2c3e50;
}

.status-indicator {
  font-size: 1.2rem;
}

.status-indicator.success {
  color: #27ae60;
}

/* status-indicator warning/error 스타일은 필요시 추가 */

.status-text {
  color: #7f8c8d;
  font-size: 0.9rem;
}

/* 탭 */
.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 0.125rem solid #ecf0f1;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  border-bottom: 0.1875rem solid transparent;
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
  border-bottom: 0.0625rem solid #ecf0f1;
}

tbody tr:hover {
  background: #f8f9fa;
}

/* 버튼 기본 스타일은 각 버튼별로 정의 */

.btn-edit {
  padding: 0.5rem 1rem;
  background: #f39c12;
  color: white;
  border: none;
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
  cursor: pointer;
  margin-right: 0.5rem;
  transition: background 0.3s;
}

.btn-delete:hover {
  background: #c0392b;
}

/* 모달 */
/* 모달 스타일은 필요시 추가 */

/* 검색 섹션 */
.search-section {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border: 0.0625rem solid #ddd;
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border: 0.0625rem solid #ddd;
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.75rem;
  font-size: 0.8rem;
  font-weight: 500;
  background: #e74c3c;
  color: white;
}

/* status-badge.active 스타일은 필요시 추가 */

.status-badge.past {
  background: #95a5a6;
}

/* 콘서트 폼 섹션 */
.concert-form-section {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
}

.concerts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.concerts-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.btn-auto-move {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 1rem;
}

.btn-auto-move:hover {
  background: #138496;
}

.concerts-table {
  overflow-x: auto;
}

.btn-move-past {
  background: #ffc107;
  color: #212529;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.9rem;
  margin-left: 0.5rem;
}

.btn-move-past:hover {
  background: #e0a800;
}

.btn-move-upcoming {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.9rem;
  margin-left: 0.5rem;
}

.btn-move-upcoming:hover {
  background: #218838;
}

/* 연락처 폼 섹션 */
.contact-form-section {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  border: 0.0625rem solid #e9ecef;
}

.contact-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

/* Download Files 관련 스타일 */
.download-files-list {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
}

.download-files-list h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.download-files-table {
  overflow-x: auto;
}

.download-file-form-section {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  border: 0.0625rem solid #e9ecef;
}

.download-file-form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

.file-preview {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: #e9ecef;
  border-radius: 0.25rem;
  font-size: 0.9rem;
  color: #666;
}

.contact-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 연락처 목록 */
.contacts-list {
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #e9ecef;
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
  border-radius: 0.5rem;
  padding: 2rem;
  margin-top: 2rem;
  border: 0.0625rem solid #e9ecef;
}

.photo-group-details h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-size: 1.5rem;
}

/* 사진 수정 섹션 */
.photo-edit-section {
  background: #fff;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 0.0625rem solid #dee2e6;
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
  grid-template-columns: repeat(auto-fill, minmax(18.75rem, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.photo-item {
  background: #fff;
  border-radius: 0.5rem;
  padding: 1rem;
  border: 0.0625rem solid #dee2e6;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.photo-preview {
  width: 100%;
  height: 12.5rem;
  overflow: hidden;
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
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
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.9rem;
}

/* 사진 그룹 액션 버튼 크기 통일 */
.photo-groups-table .btn-edit,
.photo-groups-table .btn-delete,
.photo-groups-table .btn-manage {
  min-width: 5.5rem;
  text-align: center;
}

.btn-manage:hover {
  opacity: 0.8;
}

.btn-close {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 1rem;
}

.btn-close:hover {
  opacity: 0.8;
}

/* 파일 업로드 스타일 */
.file-upload-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.file-input {
  display: none;
}

.file-upload-btn {
  padding: 0.5rem 1rem;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  text-align: center;
  font-size: 0.9rem;
  transition: background 0.3s;
}

.file-upload-btn:hover {
  background: #2980b9;
}

.url-input {
  padding: 0.75rem;
  border: 0.0625rem solid #ddd;
  border-radius: 0.25rem;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.url-input:focus {
  outline: none;
  border-color: #3498db;
}

.image-preview {
  margin-top: 0.5rem;
}

.image-preview img {
  max-width: 12.5rem;
  max-height: 9.375rem;
  object-fit: cover;
  border-radius: 0.25rem;
  border: 0.0625rem solid #ddd;
}

/* 여러 파일 선택 정보 */
.selected-files-info {
  margin-top: 1rem;
}

.selected-files-info p {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-weight: 500;
}

/* 여러 이미지 미리보기 */
.multiple-image-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(7.5rem, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.preview-item {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 0.5rem;
  overflow: hidden;
  border: 0.125rem solid #e9ecef;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-remove-preview {
  position: absolute;
  top: 0.25rem;
  right: 0.25rem;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: rgba(231, 76, 60, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 1.125rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.btn-remove-preview:hover {
  background: rgba(192, 57, 43, 1);
}

</style>
