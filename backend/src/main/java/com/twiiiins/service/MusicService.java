package com.twiiiins.service;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.entity.Music;
import com.twiiiins.exception.ResourceNotFoundException;
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
    
    public List<MusicDto> getAllMusic() {
        log.debug("모든 음악 목록 조회 시작");
        List<Music> musicList = musicRepository.findAllByOrderByDisplayOrderAsc();
        log.debug("데이터베이스에서 {} 개의 음악 조회됨", musicList.size());
        
        List<MusicDto> result = musicList.stream()
                .map(this::convertToDto)
                .toList();
        
        log.debug("음악 목록 DTO 변환 완료: {} 개 항목", result.size());
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
        return convertToDto(music);
    }
    
    public MusicDto createMusic(MusicDto musicDto) {
        log.info("음악 생성 시작: 제목 = {}, 아티스트 = {}", musicDto.getTitle(), musicDto.getArtist());
        
        Music music = convertToEntity(musicDto);
        Music savedMusic = musicRepository.save(music);
        
        log.info("음악 생성 완료: ID = {}, 제목 = {}", savedMusic.getId(), savedMusic.getTitle());
        return convertToDto(savedMusic);
    }
    
    public MusicDto updateMusic(Long id, MusicDto musicDto) {
        log.info("음악 수정 시작: ID = {}, 제목 = {}", id, musicDto.getTitle());
        
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("수정할 음악을 찾을 수 없음: ID = {}", id);
                    return new ResourceNotFoundException("Music not found with id: " + id);
                });
        
        log.debug("음악 수정 - 기존 정보: 제목 = {}, 아티스트 = {}", music.getTitle(), music.getArtist());
        
        music.setTitle(musicDto.getTitle());
        music.setArtist(musicDto.getArtist());
        music.setCoverUrl(musicDto.getCoverUrl());
        music.setLinkUrl(musicDto.getLinkUrl());
        music.setDisplayOrder(musicDto.getDisplayOrder());
        
        Music savedMusic = musicRepository.save(music);
        log.info("음악 수정 완료: ID = {}, 제목 = {}", savedMusic.getId(), savedMusic.getTitle());
        
        return convertToDto(savedMusic);
    }
    
    public void deleteMusic(Long id) {
        log.info("음악 삭제 시작: ID = {}", id);
        
        if (!musicRepository.existsById(id)) {
            log.warn("삭제할 음악을 찾을 수 없음: ID = {}", id);
            throw new ResourceNotFoundException("Music not found with id: " + id);
        }
        
        musicRepository.deleteById(id);
        log.info("음악 삭제 완료: ID = {}", id);
    }
    
    private MusicDto convertToDto(Music music) {
        return new MusicDto(
            music.getId(),
            music.getTitle(),
            music.getArtist(),
            music.getCoverUrl(),
            music.getLinkUrl(),
            music.getDisplayOrder()
        );
    }
    
    private Music convertToEntity(MusicDto musicDto) {
        Music music = new Music();
        music.setTitle(musicDto.getTitle());
        music.setArtist(musicDto.getArtist());
        music.setCoverUrl(musicDto.getCoverUrl());
        music.setLinkUrl(musicDto.getLinkUrl());
        music.setDisplayOrder(musicDto.getDisplayOrder());
        return music;
    }
}
