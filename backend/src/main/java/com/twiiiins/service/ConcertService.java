package com.twiiiins.service;

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
    
    public List<Concert> getAllConcerts() {
        return concertRepository.findAllByOrderByDateDesc();
    }
    
    public List<Concert> getUpcomingConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(false);
    }
    
    public List<Concert> getPastConcerts() {
        return concertRepository.findByIsPastOrderByDateAsc(true);
    }
    
    public Concert getConcertById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + id));
    }
    
    public Concert createConcert(Concert concert) {
        return concertRepository.save(concert);
    }
    
    public Concert updateConcert(Long id, Concert concertDetails) {
        Concert concert = getConcertById(id);
        
        concert.setDate(concertDetails.getDate());
        concert.setLocation(concertDetails.getLocation());
        concert.setName(concertDetails.getName());
        concert.setStartTime(concertDetails.getStartTime());
        concert.setTicketInfo(concertDetails.getTicketInfo());
        concert.setFullLocation(concertDetails.getFullLocation());
        concert.setGoogleMapUrl(concertDetails.getGoogleMapUrl());
        concert.setCollaborationInfo(concertDetails.getCollaborationInfo());
        concert.setIsPast(concertDetails.getIsPast());
        
        return concertRepository.save(concert);
    }
    
    public void deleteConcert(Long id) {
        Concert concert = getConcertById(id);
        concertRepository.delete(concert);
    }
    
    public Concert moveToPastEvent(Long id) {
        Concert concert = getConcertById(id);
        concert.setIsPast(true);
        return concertRepository.save(concert);
    }
    
    public Concert moveToUpcomingEvent(Long id) {
        Concert concert = getConcertById(id);
        concert.setIsPast(false);
        return concertRepository.save(concert);
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

