package com.twiiiins.controller;

import com.twiiiins.entity.*;
import com.twiiiins.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    
    private final MusicService musicService;
    private final VideoService videoService;
    private final PhotoService photoService;
    private final NewsService newsService;
    private final EquipmentService equipmentService;
    private final ContactEntityService contactEntityService;
    private final DownloadFileService downloadFileService;
    
    // Music endpoints
    @GetMapping("/music")
    public ResponseEntity<List<Music>> getAllMusic() {
        return ResponseEntity.ok(musicService.getAllMusic());
    }
    
    @GetMapping("/music/{id}")
    public ResponseEntity<Music> getMusicById(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusicById(id));
    }
    
    @PostMapping("/music")
    public ResponseEntity<Music> createMusic(@RequestBody Music music) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(musicService.createMusic(music));
    }
    
    @PutMapping("/music/{id}")
    public ResponseEntity<Music> updateMusic(
            @PathVariable Long id,
            @RequestBody Music music) {
        return ResponseEntity.ok(musicService.updateMusic(id, music));
    }
    
    @DeleteMapping("/music/{id}")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        musicService.deleteMusic(id);
        return ResponseEntity.noContent().build();
    }
    
    // Video endpoints
    @GetMapping("/videos")
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }
    
    @GetMapping("/videos/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }
    
    @PostMapping("/videos")
    public ResponseEntity<Video> createVideo(@RequestBody Video video) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(videoService.createVideo(video));
    }
    
    @PutMapping("/videos/{id}")
    public ResponseEntity<Video> updateVideo(
            @PathVariable Long id,
            @RequestBody Video video) {
        return ResponseEntity.ok(videoService.updateVideo(id, video));
    }
    
    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
    
    // PhotoGroup endpoints
    @GetMapping("/photo-groups")
    public ResponseEntity<List<PhotoGroup>> getAllPhotoGroups() {
        return ResponseEntity.ok(photoService.getAllPhotoGroups());
    }
    
    @GetMapping("/photo-groups/{id}")
    public ResponseEntity<PhotoGroup> getPhotoGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoGroupById(id));
    }
    
    @PostMapping("/photo-groups")
    public ResponseEntity<PhotoGroup> createPhotoGroup(@RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhotoGroup(photoGroup));
    }
    
    @PutMapping("/photo-groups/{id}")
    public ResponseEntity<PhotoGroup> updatePhotoGroup(
            @PathVariable Long id,
            @RequestBody PhotoGroup photoGroup) {
        return ResponseEntity.ok(photoService.updatePhotoGroup(id, photoGroup));
    }
    
    @DeleteMapping("/photo-groups/{id}")
    public ResponseEntity<Void> deletePhotoGroup(@PathVariable Long id) {
        photoService.deletePhotoGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // Photo endpoints
    @GetMapping("/photo-groups/{groupId}/photos")
    public ResponseEntity<List<Photo>> getPhotosByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(photoService.getPhotosByGroupId(groupId));
    }
    
    @GetMapping("/photos/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }
    
    @PostMapping("/photo-groups/{groupId}/photos")
    public ResponseEntity<Photo> createPhoto(
            @PathVariable Long groupId,
            @RequestBody Photo photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.createPhoto(groupId, photo));
    }
    
    @PutMapping("/photos/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @PathVariable Long id,
            @RequestBody Photo photo) {
        return ResponseEntity.ok(photoService.updatePhoto(id, photo));
    }
    
    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
    
    // News endpoints
    @GetMapping("/news")
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }
    
    @GetMapping("/news/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }
    
    @PostMapping("/news")
    public ResponseEntity<News> createNews(@RequestBody News news) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.createNews(news));
    }
    
    @PutMapping("/news/{id}")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @RequestBody News news) {
        return ResponseEntity.ok(newsService.updateNews(id, news));
    }
    
    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
    
    // Equipment endpoints
    @GetMapping("/equipment")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }
    
    @GetMapping("/equipment/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
    
    @PostMapping("/equipment")
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.createEquipment(equipment));
    }
    
    @PutMapping("/equipment/{id}")
    public ResponseEntity<Equipment> updateEquipment(
            @PathVariable Long id,
            @RequestBody Equipment equipment) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
    }
    
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
    
    // Contact endpoints
    @GetMapping("/contacts")
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactEntityService.getAllContacts());
    }
    
    @GetMapping("/contacts/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactEntityService.getContactById(id));
    }
    
    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactEntityService.createContact(contact));
    }
    
    @PutMapping("/contacts/{id}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @RequestBody Contact contact) {
        return ResponseEntity.ok(contactEntityService.updateContact(id, contact));
    }
    
    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactEntityService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
    
    // DownloadFile endpoints
    @GetMapping("/download-files")
    public ResponseEntity<List<DownloadFile>> getAllDownloadFiles() {
        return ResponseEntity.ok(downloadFileService.getAllDownloadFiles());
    }
    
    @GetMapping("/download-files/{id}")
    public ResponseEntity<DownloadFile> getDownloadFileById(@PathVariable Long id) {
        return ResponseEntity.ok(downloadFileService.getDownloadFileById(id));
    }
    
    @PostMapping("/download-files")
    public ResponseEntity<DownloadFile> createDownloadFile(@RequestBody DownloadFile downloadFile) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(downloadFileService.createDownloadFile(downloadFile));
    }
    
    @PutMapping("/download-files/{id}")
    public ResponseEntity<DownloadFile> updateDownloadFile(
            @PathVariable Long id,
            @RequestBody DownloadFile downloadFile) {
        return ResponseEntity.ok(downloadFileService.updateDownloadFile(id, downloadFile));
    }
    
    @DeleteMapping("/download-files/{id}")
    public ResponseEntity<Void> deleteDownloadFile(@PathVariable Long id) {
        downloadFileService.deleteDownloadFile(id);
        return ResponseEntity.noContent().build();
    }
}

