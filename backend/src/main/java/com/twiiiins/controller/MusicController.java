package com.twiiiins.controller;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {
    
    private final MusicService musicService;
    
    @GetMapping
    public ResponseEntity<List<MusicDto>> getAllMusic() {
        return ResponseEntity.ok(musicService.getAllMusic());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MusicDto> getMusicById(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusicById(id));
    }
    
    @PostMapping
    public ResponseEntity<MusicDto> createMusic(@RequestBody MusicDto musicDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(musicService.createMusic(musicDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MusicDto> updateMusic(
            @PathVariable Long id,
            @RequestBody MusicDto musicDto) {
        return ResponseEntity.ok(musicService.updateMusic(id, musicDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        musicService.deleteMusic(id);
        return ResponseEntity.noContent().build();
    }
}
