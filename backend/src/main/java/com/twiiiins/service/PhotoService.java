package com.twiiiins.service;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.PhotoGroupDto;
import com.twiiiins.entity.Photo;
import com.twiiiins.entity.PhotoGroup;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.PhotoGroupMapper;
import com.twiiiins.mapper.PhotoMapper;
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
    private final PhotoGroupMapper photoGroupMapper;
    private final PhotoMapper photoMapper;
    
    // PhotoGroup 관련
    public List<PhotoGroupDto> getAllPhotoGroups() {
        return photoGroupRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapPhotoGroupWithPhotos)
                .toList();
    }
    
    public List<PhotoGroupDto> getPhotoGroupsWithFilters(String title) {
        return photoGroupRepository.findPhotoGroupsWithFilters(title)
                .stream()
                .map(this::mapPhotoGroupWithPhotos)
                .toList();
    }
    
    public PhotoGroupDto getPhotoGroupById(Long id) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        return mapPhotoGroupWithPhotos(photoGroup);
    }
    
    public PhotoGroupDto createPhotoGroup(PhotoGroupDto photoGroupDto) {
        PhotoGroup photoGroup = photoGroupMapper.toEntity(photoGroupDto);
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(photoGroup);
        return mapPhotoGroupWithPhotos(savedPhotoGroup);
    }
    
    public PhotoGroupDto updatePhotoGroup(Long id, PhotoGroupDto photoGroupDto) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        
        photoGroupMapper.updateEntityFromDto(photoGroupDto, photoGroup);
        
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(photoGroup);
        return mapPhotoGroupWithPhotos(savedPhotoGroup);
    }
    
    public void deletePhotoGroup(Long id) {
        photoGroupRepository.deleteById(id);
    }
    
    // Photo 관련
    public List<PhotoDto> getPhotosByGroupId(Long groupId) {
        return photoRepository.findByPhotoGroupId(groupId)
                .stream()
                .map(photoMapper::toDto)
                .toList();
    }
    
    public PhotoDto getPhotoById(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        return photoMapper.toDto(photo);
    }
    
    public PhotoDto createPhoto(Long groupId, PhotoDto photoDto) {
        PhotoGroup photoGroup = photoGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + groupId));
        
        Photo photo = photoMapper.toEntity(photoDto);
        photo.setPhotoGroup(photoGroup);
        
        Photo savedPhoto = photoRepository.save(photo);
        return photoMapper.toDto(savedPhoto);
    }
    
    public PhotoDto updatePhoto(Long id, PhotoDto photoDto) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        
        photoMapper.updateEntityFromDto(photoDto, photo);
        
        Photo savedPhoto = photoRepository.save(photo);
        return photoMapper.toDto(savedPhoto);
    }
    
    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }
    
    private PhotoGroupDto mapPhotoGroupWithPhotos(PhotoGroup photoGroup) {
        PhotoGroupDto dto = photoGroupMapper.toDto(photoGroup);
        List<PhotoDto> photos = photoRepository.findByPhotoGroupId(photoGroup.getId())
                .stream()
                .map(photoMapper::toDto)
                .toList();
        dto.setPhotos(photos);
        return dto;
    }
}
