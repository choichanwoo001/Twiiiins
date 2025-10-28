package com.twiiiins.controller;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.PhotoGroupDto;
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
    public ResponseEntity<List<PhotoGroupDto>> getAllPhotoGroups() {
        return ResponseEntity.ok(photoService.getAllPhotoGroups());
    }
    
    @GetMapping("/groups/{id}")
    public ResponseEntity<PhotoGroupDto> getPhotoGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoGroupById(id));
    }
    
    @PostMapping("/groups")
    public ResponseEntity<PhotoGroupDto> createPhotoGroup(@RequestBody PhotoGroupDto photoGroupDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhotoGroup(photoGroupDto));
    }
    
    @PutMapping("/groups/{id}")
    public ResponseEntity<PhotoGroupDto> updatePhotoGroup(
            @PathVariable Long id,
            @RequestBody PhotoGroupDto photoGroupDto) {
        return ResponseEntity.ok(photoService.updatePhotoGroup(id, photoGroupDto));
    }
    
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deletePhotoGroup(@PathVariable Long id) {
        photoService.deletePhotoGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // Photo endpoints
    @GetMapping("/groups/{groupId}/photos")
    public ResponseEntity<List<PhotoDto>> getPhotosByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(photoService.getPhotosByGroupId(groupId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PhotoDto> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }
    
    @PostMapping("/groups/{groupId}/photos")
    public ResponseEntity<PhotoDto> createPhoto(
            @PathVariable Long groupId,
            @RequestBody PhotoDto photoDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhoto(groupId, photoDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PhotoDto> updatePhoto(
            @PathVariable Long id,
            @RequestBody PhotoDto photoDto) {
        return ResponseEntity.ok(photoService.updatePhoto(id, photoDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
