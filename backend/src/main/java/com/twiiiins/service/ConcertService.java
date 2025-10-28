package com.twiiiins.service;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.entity.Concert;
import com.twiiiins.exception.ResourceNotFoundException;
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
    
    public List<ConcertDto> getAllConcerts() {
        return concertRepository.findAllByOrderByDateDesc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public List<ConcertDto> getUpcomingConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(false)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public List<ConcertDto> getPastConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(true)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public ConcertDto getConcertById(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        return convertToDto(concert);
    }
    
    public ConcertDto createConcert(ConcertDto concertDto) {
        Concert concert = convertToEntity(concertDto);
        Concert savedConcert = concertRepository.save(concert);
        return convertToDto(savedConcert);
    }
    
    public ConcertDto updateConcert(Long id, ConcertDto concertDto) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        
        concert.setDate(concertDto.getDate());
        concert.setLocation(concertDto.getLocation());
        concert.setName(concertDto.getName());
        concert.setStartTime(concertDto.getStartTime());
        concert.setTicketInfo(concertDto.getTicketInfo());
        concert.setFullLocation(concertDto.getFullLocation());
        concert.setGoogleMapUrl(concertDto.getGoogleMapUrl());
        concert.setCollaborationInfo(concertDto.getCollaborationInfo());
        concert.setIsPast(concertDto.getIsPast());
        
        Concert savedConcert = concertRepository.save(concert);
        return convertToDto(savedConcert);
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
        return convertToDto(savedConcert);
    }
    
    public ConcertDto moveToUpcomingEvent(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
        concert.setIsPast(false);
        Concert savedConcert = concertRepository.save(concert);
        return convertToDto(savedConcert);
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
    
    private ConcertDto convertToDto(Concert concert) {
        return new ConcertDto(
            concert.getId(),
            concert.getDate(),
            concert.getLocation(),
            concert.getName(),
            concert.getStartTime(),
            concert.getTicketInfo(),
            concert.getFullLocation(),
            concert.getGoogleMapUrl(),
            concert.getCollaborationInfo(),
            concert.getIsPast()
        );
    }
    
    private Concert convertToEntity(ConcertDto concertDto) {
        Concert concert = new Concert();
        concert.setDate(concertDto.getDate());
        concert.setLocation(concertDto.getLocation());
        concert.setName(concertDto.getName());
        concert.setStartTime(concertDto.getStartTime());
        concert.setTicketInfo(concertDto.getTicketInfo());
        concert.setFullLocation(concertDto.getFullLocation());
        concert.setGoogleMapUrl(concertDto.getGoogleMapUrl());
        concert.setCollaborationInfo(concertDto.getCollaborationInfo());
        concert.setIsPast(concertDto.getIsPast());
        return concert;
    }
}

