package com.twiiiins.controller;

import com.twiiiins.entity.Music;
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
    public ResponseEntity<List<Music>> getAllMusic() {
        return ResponseEntity.ok(musicService.getAllMusic());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Music> getMusicById(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusicById(id));
    }
    
    @PostMapping
    public ResponseEntity<Music> createMusic(@RequestBody Music music) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(musicService.createMusic(music));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Music> updateMusic(
            @PathVariable Long id,
            @RequestBody Music music) {
        return ResponseEntity.ok(musicService.updateMusic(id, music));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        musicService.deleteMusic(id);
        return ResponseEntity.noContent().build();
    }
}
