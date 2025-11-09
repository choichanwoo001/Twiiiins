package com.twiiiins.service;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.dto.request.VideoCreateRequest;
import com.twiiiins.dto.request.VideoUpdateRequest;
import com.twiiiins.entity.Video;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.VideoMapper;
import com.twiiiins.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    
    public VideoDto getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return videoMapper.toDto(video);
    }
    
    public VideoDto createVideo(VideoCreateRequest request) {
        Video video = videoMapper.toEntity(request);
        Video savedVideo = videoRepository.save(video);
        return videoMapper.toDto(savedVideo);
    }
    
    public VideoDto updateVideo(Long id, VideoUpdateRequest request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        
        videoMapper.updateEntityFromUpdateRequest(request, video);
        
        Video savedVideo = videoRepository.save(video);
        return videoMapper.toDto(savedVideo);
    }
    
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
}
