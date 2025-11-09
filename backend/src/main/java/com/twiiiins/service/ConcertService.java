package com.twiiiins.service;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.entity.Concert;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ConcertMapper;
import com.twiiiins.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConcertService {
    
    private final ConcertRepository concertRepository;
    private final ConcertMapper concertMapper;
    
    public List<ConcertDto> getAllConcerts() {
        return concertRepository.findAllByOrderByDateDesc()
                .stream()
                .map(concertMapper::toDto)
                .toList();
    }
    
    public List<ConcertDto> getUpcomingConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(false)
                .stream()
                .map(concertMapper::toDto)
                .toList();
    }
    
    public List<ConcertDto> getPastConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(true)
                .stream()
                .map(concertMapper::toDto)
                .toList();
    }
    
    public ConcertDto getConcertById(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        return concertMapper.toDto(concert);
    }
    
    public ConcertDto createConcert(ConcertDto concertDto) {
        Concert concert = concertMapper.toEntity(concertDto);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    public ConcertDto updateConcert(Long id, ConcertDto concertDto) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        
        concertMapper.updateEntityFromDto(concertDto, concert);
        
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    public void deleteConcert(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concertRepository.delete(concert);
    }
    
    public ConcertDto moveToPastEvent(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concert.setIsPast(true);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    public ConcertDto moveToUpcomingEvent(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concert.setIsPast(false);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    public int autoMovePastEvents(String currentDateStr) {
        LocalDate currentDate = LocalDate.parse(currentDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        
        // 어제까지의 콘서트들을 Past Event로 이동
        List<Concert> concertsToMove = concertRepository.findByDateBeforeAndIsPast(currentDate, false);
        
        for (Concert concert : concertsToMove) {
            concert.setIsPast(true);
            concertRepository.save(concert);
        }
        
        return concertsToMove.size();
    }
    
}

