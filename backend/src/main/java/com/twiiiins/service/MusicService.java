package com.twiiiins.service;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.entity.Music;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.MusicMapper;
import com.twiiiins.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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
    
    public MusicDto getMusicById(Long id) {
        log.debug("음악 조회 시작: ID = {}", id);
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("음악을 찾을 수 없음: ID = {}", id);
                    return new ResourceNotFoundException("Music not found with id: " + id);
                });
        
        log.debug("음악 조회 성공: ID = {}, 제목 = {}", music.getId(), music.getTitle());
        return musicMapper.toDto(music);
    }
    
    public MusicDto createMusic(MusicDto musicDto) {
        log.info("[음악 생성] 시작 - 제목: {}, 아티스트: {}", musicDto.getTitle(), musicDto.getArtist());
        
        try {
            Music music = musicMapper.toEntity(musicDto);
            Music savedMusic = musicRepository.save(music);
            
            log.info("[음악 생성] 완료 - ID: {}, 제목: {}", savedMusic.getId(), savedMusic.getTitle());
            return musicMapper.toDto(savedMusic);
        } catch (Exception e) {
            log.error("[음악 생성] 실패 - 제목: {}, 아티스트: {}, 오류: {}", 
                    musicDto.getTitle(), musicDto.getArtist(), e.getMessage(), e);
            throw e;
        }
    }
    
    public MusicDto updateMusic(Long id, MusicDto musicDto) {
        log.info("[음악 수정] 시작 - ID: {}, 제목: {}", id, musicDto.getTitle());
        
        try {
            Music music = musicRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("[음악 수정] 리소스 없음 - ID: {}", id);
                        return new ResourceNotFoundException("Music not found with id: " + id);
                    });
            
            log.debug("[음악 수정] 기존 정보 - 제목: {}, 아티스트: {}", music.getTitle(), music.getArtist());
            
            musicMapper.updateEntityFromDto(musicDto, music);
            
            Music savedMusic = musicRepository.save(music);
            log.info("[음악 수정] 완료 - ID: {}, 제목: {}", savedMusic.getId(), savedMusic.getTitle());
            
            return musicMapper.toDto(savedMusic);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[음악 수정] 실패 - ID: {}, 제목: {}, 오류: {}", 
                    id, musicDto.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
    
    public void deleteMusic(Long id) {
        log.info("[음악 삭제] 시작 - ID: {}", id);
        
        try {
            if (!musicRepository.existsById(id)) {
                log.warn("[음악 삭제] 리소스 없음 - ID: {}", id);
                throw new ResourceNotFoundException("Music not found with id: " + id);
            }
            
            musicRepository.deleteById(id);
            log.info("[음악 삭제] 완료 - ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[음악 삭제] 실패 - ID: {}, 오류: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
}
