package com.twiiiins.controller;

import com.twiiiins.entity.Photo;
import com.twiiiins.entity.PhotoGroup;
import com.twiiiins.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {
    
    private final PhotoService photoService;
    
    // PhotoGroup endpoints
    @GetMapping("/groups")
    public ResponseEntity<List<PhotoGroup>> getAllPhotoGroups() {
        return ResponseEntity.ok(photoService.getAllPhotoGroups());
    }
    
    @GetMapping("/groups/{id}")
    public ResponseEntity<PhotoGroup> getPhotoGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoGroupById(id));
    }
    
    @PostMapping("/groups")
    public ResponseEntity<PhotoGroup> createPhotoGroup(@RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhotoGroup(photoGroup));
    }
    
    @PutMapping("/groups/{id}")
    public ResponseEntity<PhotoGroup> updatePhotoGroup(
            @PathVariable Long id,
            @RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.ok(photoService.updatePhotoGroup(id, photoGroup));
    }
    
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deletePhotoGroup(@PathVariable Long id) {
        photoService.deletePhotoGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // Photo endpoints
    @GetMapping("/groups/{groupId}/photos")
    public ResponseEntity<List<Photo>> getPhotosByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(photoService.getPhotosByGroupId(groupId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }
    
    @PostMapping("/groups/{groupId}/photos")
    public ResponseEntity<Photo> createPhoto(
            @PathVariable Long groupId,
            @RequestBody Photo photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhoto(groupId, photo));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @PathVariable Long id,
            @RequestBody Photo photo) {
        return ResponseEntity.ok(photoService.updatePhoto(id, photo));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
