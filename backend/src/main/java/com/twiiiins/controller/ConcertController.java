package com.twiiiins.controller;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {
    
    private final ConcertService concertService;
    
    @GetMapping
    public ResponseEntity<List<ConcertDto>> getAllConcerts() {
        return ResponseEntity.ok(concertService.getAllConcerts());
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<ConcertDto>> getUpcomingConcerts() {
        return ResponseEntity.ok(concertService.getUpcomingConcerts());
    }
    
    @GetMapping("/past")
    public ResponseEntity<List<ConcertDto>> getPastConcerts() {
        return ResponseEntity.ok(concertService.getPastConcerts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConcertDto> getConcertById(@PathVariable Long id) {
        return ResponseEntity.ok(concertService.getConcertById(id));
    }
    
    @PostMapping
    public ResponseEntity<ConcertDto> createConcert(@RequestBody ConcertDto concertDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(concertService.createConcert(concertDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ConcertDto> updateConcert(
            @PathVariable Long id,
            @RequestBody ConcertDto concertDto) {
        return ResponseEntity.ok(concertService.updateConcert(id, concertDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/move-to-past")
    public ResponseEntity<ConcertDto> moveToPastEvent(@PathVariable Long id) {
        return ResponseEntity.ok(concertService.moveToPastEvent(id));
    }
    
    @PutMapping("/{id}/move-to-upcoming")
    public ResponseEntity<ConcertDto> moveToUpcomingEvent(@PathVariable Long id) {
        return ResponseEntity.ok(concertService.moveToUpcomingEvent(id));
    }
    
    @PutMapping("/auto-move-past")
    public ResponseEntity<AutoMoveResponse> autoMovePastEvents(@RequestBody AutoMoveRequest request) {
        int movedCount = concertService.autoMovePastEvents(request.getCurrentDate());
        return ResponseEntity.ok(new AutoMoveResponse(movedCount));
    }
    
    // DTO 클래스들
    public static class AutoMoveRequest {
        private String currentDate;
        
        public String getCurrentDate() {
            return currentDate;
        }
        
        public void setCurrentDate(String currentDate) {
            this.currentDate = currentDate;
        }
    }
    
    public static class AutoMoveResponse {
        private int movedCount;
        
        public AutoMoveResponse(int movedCount) {
            this.movedCount = movedCount;
        }
        
        public int getMovedCount() {
            return movedCount;
        }
        
        public void setMovedCount(int movedCount) {
            this.movedCount = movedCount;
        }
    }
}

