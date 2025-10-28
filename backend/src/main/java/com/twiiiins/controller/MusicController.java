package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.MusicDto;
import com.twiiiins.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
@Slf4j
public class MusicController {
    
    private final MusicService musicService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<MusicDto>>> getAllMusic() {
        log.info("모든 음악 목록 조회 요청");
        List<MusicDto> musicList = musicService.getAllMusic();
        log.info("음악 목록 조회 완료: {} 개 항목", musicList.size());
        return ResponseEntity.ok(ApiResponse.success(musicList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicDto>> getMusicById(@PathVariable Long id) {
        log.info("음악 조회 요청: ID = {}", id);
        MusicDto music = musicService.getMusicById(id);
        log.info("음악 조회 완료: {}", music.getTitle());
        return ResponseEntity.ok(ApiResponse.success(music));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<MusicDto>> createMusic(@RequestBody MusicDto musicDto) {
        log.info("음악 생성 요청: {}", musicDto.getTitle());
        MusicDto createdMusic = musicService.createMusic(musicDto);
        log.info("음악 생성 완료: ID = {}, 제목 = {}", createdMusic.getId(), createdMusic.getTitle());
        return ResponseEntity.ok(ApiResponse.created(createdMusic));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicDto>> updateMusic(
            @PathVariable Long id,
            @RequestBody MusicDto musicDto) {
        log.info("음악 수정 요청: ID = {}, 제목 = {}", id, musicDto.getTitle());
        MusicDto updatedMusic = musicService.updateMusic(id, musicDto);
        log.info("음악 수정 완료: ID = {}, 제목 = {}", updatedMusic.getId(), updatedMusic.getTitle());
        return ResponseEntity.ok(ApiResponse.success(updatedMusic, "음악이 성공적으로 수정되었습니다."));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(@PathVariable Long id) {
        log.info("음악 삭제 요청: ID = {}", id);
        musicService.deleteMusic(id);
        log.info("음악 삭제 완료: ID = {}", id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
