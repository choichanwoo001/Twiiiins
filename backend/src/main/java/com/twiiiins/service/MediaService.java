package com.twiiiins.service;

import com.twiiiins.entity.*;
import com.twiiiins.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {
    
    private final MusicRepository musicRepository;
    private final VideoRepository videoRepository;
    private final PhotoGroupRepository photoGroupRepository;
    private final PhotoRepository photoRepository;
    private final NewsRepository newsRepository;
    private final EquipmentRepository equipmentRepository;
    
    // Music 관련
    public List<Music> getAllMusic() {
        return musicRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Music getMusicById(Long id) {
        return musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Music not found with id: " + id));
    }
    
    public Music createMusic(Music music) {
        return musicRepository.save(music);
    }
    
    public Music updateMusic(Long id, Music musicDetails) {
        Music music = getMusicById(id);
        music.setTitle(musicDetails.getTitle());
        music.setArtist(musicDetails.getArtist());
        music.setCoverUrl(musicDetails.getCoverUrl());
        music.setLinkUrl(musicDetails.getLinkUrl());
        music.setDisplayOrder(musicDetails.getDisplayOrder());
        return musicRepository.save(music);
    }
    
    public void deleteMusic(Long id) {
        musicRepository.deleteById(id);
    }
    
    // Video 관련
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
    }
    
    public Video createVideo(Video video) {
        return videoRepository.save(video);
    }
    
    public Video updateVideo(Long id, Video videoDetails) {
        Video video = getVideoById(id);
        video.setTitle(videoDetails.getTitle());
        video.setEmbedUrl(videoDetails.getEmbedUrl());
        video.setDisplayOrder(videoDetails.getDisplayOrder());
        return videoRepository.save(video);
    }
    
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
    
    // PhotoGroup 관련
    public List<PhotoGroup> getAllPhotoGroups() {
        return photoGroupRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public PhotoGroup getPhotoGroupById(Long id) {
        return photoGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PhotoGroup not found with id: " + id));
    }
    
    public PhotoGroup createPhotoGroup(PhotoGroup photoGroup) {
        return photoGroupRepository.save(photoGroup);
    }
    
    public PhotoGroup updatePhotoGroup(Long id, PhotoGroup photoGroupDetails) {
        PhotoGroup photoGroup = getPhotoGroupById(id);
        photoGroup.setTitle(photoGroupDetails.getTitle());
        photoGroup.setDisplayOrder(photoGroupDetails.getDisplayOrder());
        return photoGroupRepository.save(photoGroup);
    }
    
    public void deletePhotoGroup(Long id) {
        photoGroupRepository.deleteById(id);
    }
    
    // Photo 관련
    public List<Photo> getPhotosByGroupId(Long groupId) {
        return photoRepository.findByPhotoGroupId(groupId);
    }
    
    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + id));
    }
    
    public Photo createPhoto(Long groupId, Photo photo) {
        PhotoGroup photoGroup = getPhotoGroupById(groupId);
        photo.setPhotoGroup(photoGroup);
        return photoRepository.save(photo);
    }
    
    public Photo updatePhoto(Long id, Photo photoDetails) {
        Photo photo = getPhotoById(id);
        photo.setImageUrl(photoDetails.getImageUrl());
        photo.setAltText(photoDetails.getAltText());
        return photoRepository.save(photo);
    }
    
    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }
    
    // News 관련
    public List<News> getAllNews() {
        return newsRepository.findAllByOrderByDateDesc();
    }
    
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
    }
    
    public News createNews(News news) {
        return newsRepository.save(news);
    }
    
    public News updateNews(Long id, News newsDetails) {
        News news = getNewsById(id);
        news.setDate(newsDetails.getDate());
        news.setTitle(newsDetails.getTitle());
        news.setDescription(newsDetails.getDescription());
        news.setDisplayOrder(newsDetails.getDisplayOrder());
        return newsRepository.save(news);
    }
    
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
    
    // Equipment 관련
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
    }
    
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }
    
    public Equipment updateEquipment(Long id, Equipment equipmentDetails) {
        Equipment equipment = getEquipmentById(id);
        equipment.setName(equipmentDetails.getName());
        equipment.setImageUrl(equipmentDetails.getImageUrl());
        equipment.setDisplayOrder(equipmentDetails.getDisplayOrder());
        return equipmentRepository.save(equipment);
    }
    
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
}

