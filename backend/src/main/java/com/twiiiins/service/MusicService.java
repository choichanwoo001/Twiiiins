package com.twiiiins.service;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.dto.request.MusicCreateRequest;
import com.twiiiins.dto.request.MusicUpdateRequest;
import com.twiiiins.entity.Music;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.MusicMapper;
import com.twiiiins.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MusicService {
    
    private final MusicRepository musicRepository;
    private final MusicMapper musicMapper;
    
    public List<MusicDto> getAllMusic() {
        log.debug("모든 음악 목록 조회 시작");
        List<Music> musicList = musicRepository.findAllByOrderByDisplayOrderAsc();
        log.debug("데이터베이스에서 {} 개의 음악 조회됨", musicList.size());
        
        List<MusicDto> result = musicList.stream()
                .map(musicMapper::toDto)
                .toList();
        
        log.debug("음악 목록 DTO 변환 완료: {} 개 항목", result.size());
        return result;
    }
    
    public List<MusicDto> getMusicWithFilters(String title, String artist) {
        log.debug("음악 검색 시작: title={}, artist={}", title, artist);
        List<Music> musicList = musicRepository.findMusicWithFilters(title, artist);
        log.debug("데이터베이스에서 {} 개의 음악 조회됨", musicList.size());
        
        List<MusicDto> result = musicList.stream()
                .map(musicMapper::toDto)
                .toList();
        
        log.debug("음악 검색 결과: {} 개 항목", result.size());
        return result;
    }
    
    public MusicDto getMusicById(@NonNull Long id) {
        log.debug("음악 조회 시작: ID = {}", id);
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("음악을 찾을 수 없음: ID = {}", id);
                    return new ResourceNotFoundException("Music not found with id: " + id);
                });
        
        log.debug("음악 조회 성공: ID = {}, 제목 = {}", music.getId(), music.getTitle());
        return musicMapper.toDto(music);
    }
    
    @Transactional
    public MusicDto createMusic(@NonNull MusicCreateRequest request) {
        log.info("[음악 생성] 시작 - 제목: {}, 아티스트: {}", request.getTitle(), request.getArtist());
        Music music = Objects.requireNonNull(
                musicMapper.toEntity(request),
                "MusicMapper.toEntity returned null"
        );
        Music savedMusic = musicRepository.save(music);
        log.info("[음악 생성] 완료 - ID: {}, 제목: {}", savedMusic.getId(), savedMusic.getTitle());
        return musicMapper.toDto(savedMusic);
    }
    
    @Transactional
    public MusicDto updateMusic(@NonNull Long id, @NonNull MusicUpdateRequest request) {
        log.info("[음악 수정] 시작 - ID: {}, 제목: {}", id, request.getTitle());
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id: " + id));
        musicMapper.updateEntityFromUpdateRequest(request, music);
        Music savedMusic = musicRepository.save(music);
        log.info("[음악 수정] 완료 - ID: {}, 제목: {}", savedMusic.getId(), savedMusic.getTitle());
        return musicMapper.toDto(savedMusic);
    }
    
    @Transactional
    public void deleteMusic(@NonNull Long id) {
        log.info("[음악 삭제] 시작 - ID: {}", id);
        if (!musicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Music not found with id: " + id);
        }
        musicRepository.deleteById(id);
        log.info("[음악 삭제] 완료 - ID: {}", id);
    }
    
}
