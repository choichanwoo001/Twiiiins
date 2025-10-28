package com.twiiiins.service;

import com.twiiiins.dto.MusicDto;
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
    
    public List<MusicDto> getAllMusic() {
        return musicRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public MusicDto getMusicById(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id: " + id));
        return convertToDto(music);
    }
    
    public MusicDto createMusic(MusicDto musicDto) {
        Music music = convertToEntity(musicDto);
        Music savedMusic = musicRepository.save(music);
        return convertToDto(savedMusic);
    }
    
    public MusicDto updateMusic(Long id, MusicDto musicDto) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id: " + id));
        
        music.setTitle(musicDto.getTitle());
        music.setArtist(musicDto.getArtist());
        music.setCoverUrl(musicDto.getCoverUrl());
        music.setLinkUrl(musicDto.getLinkUrl());
        music.setDisplayOrder(musicDto.getDisplayOrder());
        
        Music savedMusic = musicRepository.save(music);
        return convertToDto(savedMusic);
    }
    
    public void deleteMusic(Long id) {
        musicRepository.deleteById(id);
    }
    
    private MusicDto convertToDto(Music music) {
        return new MusicDto(
            music.getId(),
            music.getTitle(),
            music.getArtist(),
            music.getCoverUrl(),
            music.getLinkUrl(),
            music.getDisplayOrder()
        );
    }
    
    private Music convertToEntity(MusicDto musicDto) {
        Music music = new Music();
        music.setTitle(musicDto.getTitle());
        music.setArtist(musicDto.getArtist());
        music.setCoverUrl(musicDto.getCoverUrl());
        music.setLinkUrl(musicDto.getLinkUrl());
        music.setDisplayOrder(musicDto.getDisplayOrder());
        return music;
    }
}
