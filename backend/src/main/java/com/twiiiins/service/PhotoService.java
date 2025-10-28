package com.twiiiins.service;

import com.twiiiins.entity.Photo;
import com.twiiiins.entity.PhotoGroup;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.PhotoGroupRepository;
import com.twiiiins.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {
    
    private final PhotoGroupRepository photoGroupRepository;
    private final PhotoRepository photoRepository;
    
    // PhotoGroup 관련
    public List<PhotoGroup> getAllPhotoGroups() {
        return photoGroupRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public PhotoGroup getPhotoGroupById(Long id) {
        return photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
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
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
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
}
