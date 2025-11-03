package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.MusicDto;
import com.twiiiins.service.MusicService;
import com.twiiiins.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/music")
@RequiredArgsConstructor
@Slf4j
public class MusicController {
    
    private final MusicService musicService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<MusicDto>>> getAllMusic(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist) {
        log.info("음악 목록 조회 요청: title={}, artist={}", title, artist);
        
        List<MusicDto> musicList;
        if (title != null || artist != null) {
            musicList = musicService.getMusicWithFilters(title, artist);
        } else {
            musicList = musicService.getAllMusic();
        }
        
        log.info("음악 목록 조회 완료: {} 개 항목", musicList.size());
        return ResponseUtil.listSuccess(musicList);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicDto>> getMusicById(@PathVariable Long id) {
        log.info("음악 조회 요청: ID = {}", id);
        MusicDto music = musicService.getMusicById(id);
        log.info("음악 조회 완료: {}", music.getTitle());
        return ResponseUtil.success(music);
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<MusicDto>> createMusic(@RequestBody MusicDto musicDto) {
        log.info("음악 생성 요청: {}", musicDto.getTitle());
        MusicDto createdMusic = musicService.createMusic(musicDto);
        log.info("음악 생성 완료: ID = {}, 제목 = {}", createdMusic.getId(), createdMusic.getTitle());
        return ResponseUtil.created(createdMusic, "음악이 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicDto>> updateMusic(
            @PathVariable Long id,
            @RequestBody MusicDto musicDto) {
        log.info("음악 수정 요청: ID = {}, 제목 = {}", id, musicDto.getTitle());
        MusicDto updatedMusic = musicService.updateMusic(id, musicDto);
        log.info("음악 수정 완료: ID = {}, 제목 = {}", updatedMusic.getId(), updatedMusic.getTitle());
        return ResponseUtil.success(updatedMusic, "음악이 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(@PathVariable Long id) {
        log.info("음악 삭제 요청: ID = {}", id);
        musicService.deleteMusic(id);
        log.info("음악 삭제 완료: ID = {}", id);
        return ResponseUtil.deleted("음악이 성공적으로 삭제되었습니다.");
    }
}
