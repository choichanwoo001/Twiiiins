package com.twiiiins.service;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.entity.Video;
import com.twiiiins.exception.ResourceNotFoundException;
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
    
    public List<VideoDto> getAllVideos() {
        return videoRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public List<VideoDto> getVideosWithFilters(String title) {
        return videoRepository.findVideosWithFilters(title)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public VideoDto getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return convertToDto(video);
    }
    
    public VideoDto createVideo(VideoDto videoDto) {
        Video video = convertToEntity(videoDto);
        Video savedVideo = videoRepository.save(video);
        return convertToDto(savedVideo);
    }
    
    public VideoDto updateVideo(Long id, VideoDto videoDto) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        
        video.setTitle(videoDto.getTitle());
        video.setEmbedUrl(videoDto.getEmbedUrl());
        video.setDisplayOrder(videoDto.getDisplayOrder());
        
        Video savedVideo = videoRepository.save(video);
        return convertToDto(savedVideo);
    }
    
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
    
    private VideoDto convertToDto(Video video) {
        return new VideoDto(
            video.getId(),
            video.getTitle(),
            video.getEmbedUrl(),
            video.getDisplayOrder()
        );
    }
    
    private Video convertToEntity(VideoDto videoDto) {
        Video video = new Video();
        video.setTitle(videoDto.getTitle());
        video.setEmbedUrl(videoDto.getEmbedUrl());
        video.setDisplayOrder(videoDto.getDisplayOrder());
        return video;
    }
}
