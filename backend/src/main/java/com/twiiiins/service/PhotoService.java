package com.twiiiins.service;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.PhotoGroupDto;
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
    public List<PhotoGroupDto> getAllPhotoGroups() {
        return photoGroupRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public PhotoGroupDto getPhotoGroupById(Long id) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        return convertToDto(photoGroup);
    }
    
    public PhotoGroupDto createPhotoGroup(PhotoGroupDto photoGroupDto) {
        PhotoGroup photoGroup = convertToEntity(photoGroupDto);
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(photoGroup);
        return convertToDto(savedPhotoGroup);
    }
    
    public PhotoGroupDto updatePhotoGroup(Long id, PhotoGroupDto photoGroupDto) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        
        photoGroup.setTitle(photoGroupDto.getTitle());
        photoGroup.setDisplayOrder(photoGroupDto.getDisplayOrder());
        
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(photoGroup);
        return convertToDto(savedPhotoGroup);
    }
    
    public void deletePhotoGroup(Long id) {
        photoGroupRepository.deleteById(id);
    }
    
    // Photo 관련
    public List<PhotoDto> getPhotosByGroupId(Long groupId) {
        return photoRepository.findByPhotoGroupId(groupId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public PhotoDto getPhotoById(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        return convertToDto(photo);
    }
    
    public PhotoDto createPhoto(Long groupId, PhotoDto photoDto) {
        PhotoGroup photoGroup = photoGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + groupId));
        
        Photo photo = convertToEntity(photoDto);
        photo.setPhotoGroup(photoGroup);
        
        Photo savedPhoto = photoRepository.save(photo);
        return convertToDto(savedPhoto);
    }
    
    public PhotoDto updatePhoto(Long id, PhotoDto photoDto) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        
        photo.setImageUrl(photoDto.getImageUrl());
        photo.setThumbnailUrl(photoDto.getThumbnailUrl());
        photo.setAltText(photoDto.getAltText());
        
        Photo savedPhoto = photoRepository.save(photo);
        return convertToDto(savedPhoto);
    }
    
    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }
    
    private PhotoGroupDto convertToDto(PhotoGroup photoGroup) {
        List<PhotoDto> photos = photoRepository.findByPhotoGroupId(photoGroup.getId())
                .stream()
                .map(this::convertToDto)
                .toList();
        
        return new PhotoGroupDto(
            photoGroup.getId(),
            photoGroup.getTitle(),
            photoGroup.getDisplayOrder(),
            photos
        );
    }
    
    private PhotoGroup convertToEntity(PhotoGroupDto photoGroupDto) {
        PhotoGroup photoGroup = new PhotoGroup();
        photoGroup.setTitle(photoGroupDto.getTitle());
        photoGroup.setDisplayOrder(photoGroupDto.getDisplayOrder());
        return photoGroup;
    }
    
    private PhotoDto convertToDto(Photo photo) {
        return new PhotoDto(
            photo.getId(),
            photo.getImageUrl(),
            photo.getThumbnailUrl(),
            photo.getAltText(),
            photo.getPhotoGroup() != null ? photo.getPhotoGroup().getId() : null
        );
    }
    
    private Photo convertToEntity(PhotoDto photoDto) {
        Photo photo = new Photo();
        photo.setImageUrl(photoDto.getImageUrl());
        photo.setThumbnailUrl(photoDto.getThumbnailUrl());
        photo.setAltText(photoDto.getAltText());
        return photo;
    }
}
