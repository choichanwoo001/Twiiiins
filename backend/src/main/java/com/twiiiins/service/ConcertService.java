package com.twiiiins.service;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.dto.request.ConcertCreateRequest;
import com.twiiiins.dto.request.ConcertUpdateRequest;
import com.twiiiins.entity.Concert;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ConcertMapper;
import com.twiiiins.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    
    public ConcertDto getConcertById(@NonNull Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        return concertMapper.toDto(concert);
    }
    
    @Transactional
    public ConcertDto createConcert(@NonNull ConcertCreateRequest request) {
        Concert concert = Objects.requireNonNull(
                concertMapper.toEntity(request),
                "ConcertMapper.toEntity returned null"
        );
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    @Transactional
    public ConcertDto updateConcert(@NonNull Long id, @NonNull ConcertUpdateRequest request) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concertMapper.updateEntityFromUpdateRequest(request, concert);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    @Transactional
    public void deleteConcert(@NonNull Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concertRepository.delete(concert);
    }
    
    @Transactional
    public ConcertDto moveToPastEvent(@NonNull Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concert.setIsPast(true);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    @Transactional
    public ConcertDto moveToUpcomingEvent(@NonNull Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concert.setIsPast(false);
        Concert savedConcert = concertRepository.save(concert);
        return concertMapper.toDto(savedConcert);
    }
    
    @Transactional
    public int autoMovePastEvents(@NonNull LocalDate currentDate) {
        List<Concert> concertsToMove = concertRepository.findByDateBeforeAndIsPast(currentDate, false);
        for (Concert concert : concertsToMove) {
            concert.setIsPast(true);
            concertRepository.save(concert);
        }
        return concertsToMove.size();
    }
    
}

