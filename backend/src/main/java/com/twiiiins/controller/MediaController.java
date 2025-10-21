package com.twiiiins.controller;

import com.twiiiins.entity.*;
import com.twiiiins.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    
    private final MediaService mediaService;
    
    // Music endpoints
    @GetMapping("/music")
    public ResponseEntity<List<Music>> getAllMusic() {
        return ResponseEntity.ok(mediaService.getAllMusic());
    }
    
    @GetMapping("/music/{id}")
    public ResponseEntity<Music> getMusicById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMusicById(id));
    }
    
    @PostMapping("/music")
    public ResponseEntity<Music> createMusic(@RequestBody Music music) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createMusic(music));
    }
    
    @PutMapping("/music/{id}")
    public ResponseEntity<Music> updateMusic(
            @PathVariable Long id,
            @RequestBody Music music) {
        return ResponseEntity.ok(mediaService.updateMusic(id, music));
    }
    
    @DeleteMapping("/music/{id}")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        mediaService.deleteMusic(id);
        return ResponseEntity.noContent().build();
    }
    
    // Video endpoints
    @GetMapping("/videos")
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(mediaService.getAllVideos());
    }
    
    @GetMapping("/videos/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getVideoById(id));
    }
    
    @PostMapping("/videos")
    public ResponseEntity<Video> createVideo(@RequestBody Video video) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createVideo(video));
    }
    
    @PutMapping("/videos/{id}")
    public ResponseEntity<Video> updateVideo(
            @PathVariable Long id,
            @RequestBody Video video) {
        return ResponseEntity.ok(mediaService.updateVideo(id, video));
    }
    
    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        mediaService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
    
    // PhotoGroup endpoints
    @GetMapping("/photo-groups")
    public ResponseEntity<List<PhotoGroup>> getAllPhotoGroups() {
        return ResponseEntity.ok(mediaService.getAllPhotoGroups());
    }
    
    @GetMapping("/photo-groups/{id}")
    public ResponseEntity<PhotoGroup> getPhotoGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getPhotoGroupById(id));
    }
    
    @PostMapping("/photo-groups")
    public ResponseEntity<PhotoGroup> createPhotoGroup(@RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createPhotoGroup(photoGroup));
    }
    
    @PutMapping("/photo-groups/{id}")
    public ResponseEntity<PhotoGroup> updatePhotoGroup(
            @PathVariable Long id,
            @RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.ok(mediaService.updatePhotoGroup(id, photoGroup));
    }
    
    @DeleteMapping("/photo-groups/{id}")
    public ResponseEntity<Void> deletePhotoGroup(@PathVariable Long id) {
        mediaService.deletePhotoGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // Photo endpoints
    @GetMapping("/photo-groups/{groupId}/photos")
    public ResponseEntity<List<Photo>> getPhotosByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(mediaService.getPhotosByGroupId(groupId));
    }
    
    @GetMapping("/photos/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getPhotoById(id));
    }
    
    @PostMapping("/photo-groups/{groupId}/photos")
    public ResponseEntity<Photo> createPhoto(
            @PathVariable Long groupId,
            @RequestBody Photo photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createPhoto(groupId, photo));
    }
    
    @PutMapping("/photos/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @PathVariable Long id,
            @RequestBody Photo photo) {
        return ResponseEntity.ok(mediaService.updatePhoto(id, photo));
    }
    
    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        mediaService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
    
    // News endpoints
    @GetMapping("/news")
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(mediaService.getAllNews());
    }
    
    @GetMapping("/news/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getNewsById(id));
    }
    
    @PostMapping("/news")
    public ResponseEntity<News> createNews(@RequestBody News news) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createNews(news));
    }
    
    @PutMapping("/news/{id}")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @RequestBody News news) {
        return ResponseEntity.ok(mediaService.updateNews(id, news));
    }
    
    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        mediaService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
    
    // Equipment endpoints
    @GetMapping("/equipment")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(mediaService.getAllEquipment());
    }
    
    @GetMapping("/equipment/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getEquipmentById(id));
    }
    
    @PostMapping("/equipment")
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.createEquipment(equipment));
    }
    
    @PutMapping("/equipment/{id}")
    public ResponseEntity<Equipment> updateEquipment(
            @PathVariable Long id,
            @RequestBody Equipment equipment) {
        return ResponseEntity.ok(mediaService.updateEquipment(id, equipment));
    }
    
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        mediaService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}

