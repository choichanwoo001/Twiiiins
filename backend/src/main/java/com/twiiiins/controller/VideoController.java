package com.twiiiins.controller;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {
    
    private final VideoService videoService;
    
    @GetMapping
    public ResponseEntity<List<VideoDto>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }
    
    @PostMapping
    public ResponseEntity<VideoDto> createVideo(@RequestBody VideoDto videoDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(videoService.createVideo(videoDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VideoDto> updateVideo(
            @PathVariable Long id,
            @RequestBody VideoDto videoDto) {
        return ResponseEntity.ok(videoService.updateVideo(id, videoDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
