package com.twiiiins.service;

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
    
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
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
}
