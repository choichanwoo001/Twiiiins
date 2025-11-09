package com.twiiiins.service;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.PhotoGroupDto;
import com.twiiiins.dto.request.PhotoCreateRequest;
import com.twiiiins.dto.request.PhotoGroupCreateRequest;
import com.twiiiins.dto.request.PhotoGroupUpdateRequest;
import com.twiiiins.dto.request.PhotoUpdateRequest;
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
import org.springframework.lang.NonNull;

import java.util.List;
import static java.util.Objects.requireNonNull;

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
    
    public PhotoGroupDto getPhotoGroupById(@NonNull Long id) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        return mapPhotoGroupWithPhotos(photoGroup);
    }
    
    public PhotoGroupDto createPhotoGroup(PhotoGroupCreateRequest request) {
        PhotoGroup photoGroup = photoGroupMapper.toEntity(request);
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(requireNonNull(photoGroup));
        return mapPhotoGroupWithPhotos(savedPhotoGroup);
    }
    
    public PhotoGroupDto updatePhotoGroup(@NonNull Long id, PhotoGroupUpdateRequest request) {
        PhotoGroup photoGroup = photoGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + id));
        
        photoGroupMapper.updateEntityFromUpdateRequest(request, photoGroup);
        
        PhotoGroup savedPhotoGroup = photoGroupRepository.save(requireNonNull(photoGroup));
        return mapPhotoGroupWithPhotos(savedPhotoGroup);
    }
    
    public void deletePhotoGroup(@NonNull Long id) {
        photoGroupRepository.deleteById(id);
    }
    
    // Photo 관련
    public List<PhotoDto> getPhotosByGroupId(@NonNull Long groupId) {
        return photoRepository.findByPhotoGroupId(groupId)
                .stream()
                .map(photoMapper::toDto)
                .toList();
    }
    
    public PhotoDto getPhotoById(@NonNull Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        return photoMapper.toDto(photo);
    }
    
    public PhotoDto createPhoto(@NonNull Long groupId, PhotoCreateRequest request) {
        PhotoGroup photoGroup = photoGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoGroup not found with id: " + groupId));
        
        Photo photo = photoMapper.toEntity(request);
        photo.setPhotoGroup(photoGroup);
        
        Photo savedPhoto = photoRepository.save(requireNonNull(photo));
        return photoMapper.toDto(savedPhoto);
    }
    
    public PhotoDto updatePhoto(@NonNull Long id, PhotoUpdateRequest request) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        
        photoMapper.updateEntityFromUpdateRequest(request, photo);
        
        Photo savedPhoto = photoRepository.save(requireNonNull(photo));
        return photoMapper.toDto(savedPhoto);
    }
    
    public void deletePhoto(@NonNull Long id) {
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
