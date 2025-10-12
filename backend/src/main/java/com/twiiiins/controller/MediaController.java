package com.twiiiins.controller;

import com.twiiiins.entity.Media;
import com.twiiiins.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {
        
        if (type != null && !type.isEmpty()) {
            Media.MediaType mediaType = Media.MediaType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(mediaService.getMediaByType(mediaType));
        }
        
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(mediaService.getMediaByCategory(category));
        }
        
        return ResponseEntity.ok(mediaService.getAllPublishedMedia());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMedia(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }
}

