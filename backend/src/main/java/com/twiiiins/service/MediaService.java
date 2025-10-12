package com.twiiiins.service;

import com.twiiiins.entity.Media;
import com.twiiiins.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private final MediaRepository mediaRepository;

    public List<Media> getAllPublishedMedia() {
        return mediaRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<Media> getMediaByType(Media.MediaType type) {
        return mediaRepository.findByTypeAndPublishedTrueOrderByCreatedAtDesc(type);
    }

    public List<Media> getMediaByCategory(String category) {
        return mediaRepository.findByCategoryAndPublishedTrueOrderByCreatedAtDesc(category);
    }

    public Media getMediaById(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("미디어를 찾을 수 없습니다"));
    }

    @Transactional
    public Media createMedia(Media media) {
        return mediaRepository.save(media);
    }

    @Transactional
    public Media updateMedia(Long id, Media mediaDetails) {
        Media media = getMediaById(id);
        
        media.setTitle(mediaDetails.getTitle());
        media.setDescription(mediaDetails.getDescription());
        media.setUrl(mediaDetails.getUrl());
        media.setThumbnailUrl(mediaDetails.getThumbnailUrl());
        media.setCategory(mediaDetails.getCategory());
        media.setTags(mediaDetails.getTags());
        media.setPublished(mediaDetails.getPublished());

        return mediaRepository.save(media);
    }

    @Transactional
    public void deleteMedia(Long id) {
        mediaRepository.deleteById(id);
    }
}

