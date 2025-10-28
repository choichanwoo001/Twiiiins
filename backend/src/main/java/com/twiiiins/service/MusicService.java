package com.twiiiins.service;

import com.twiiiins.entity.Music;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicService {
    
    private final MusicRepository musicRepository;
    
    public List<Music> getAllMusic() {
        return musicRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Music getMusicById(Long id) {
        return musicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id: " + id));
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
}
