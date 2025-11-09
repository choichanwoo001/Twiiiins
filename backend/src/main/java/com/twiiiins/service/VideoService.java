package com.twiiiins.service;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.dto.request.VideoCreateRequest;
import com.twiiiins.dto.request.VideoUpdateRequest;
import com.twiiiins.entity.Video;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.VideoMapper;
import com.twiiiins.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {
    
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    
    public List<VideoDto> getAllVideos() {
        return videoRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(videoMapper::toDto)
                .toList();
    }
    
    public List<VideoDto> getVideosWithFilters(String title) {
        return videoRepository.findVideosWithFilters(title)
                .stream()
                .map(videoMapper::toDto)
                .toList();
    }
    
    public VideoDto getVideoById(@NonNull Long id) {
        final Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return videoMapper.toDto(video);
    }
    
    public VideoDto createVideo(@NonNull VideoCreateRequest request) {
        final Video video = Objects.requireNonNull(
                videoMapper.toEntity(request),
                "VideoMapper.toEntity returned null"
        );
        final Video savedVideo = videoRepository.save(Objects.requireNonNull(video, "Video must not be null"));
        return videoMapper.toDto(savedVideo);
    }
    
    public VideoDto updateVideo(@NonNull Long id, @NonNull VideoUpdateRequest request) {
        final Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        
        videoMapper.updateEntityFromUpdateRequest(request, video);
        
        final Video savedVideo = videoRepository.save(Objects.requireNonNull(video, "Video must not be null"));
        return videoMapper.toDto(savedVideo);
    }
    
    public void deleteVideo(@NonNull Long id) {
        videoRepository.deleteById(id);
    }
}
