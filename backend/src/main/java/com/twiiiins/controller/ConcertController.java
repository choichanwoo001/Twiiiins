package com.twiiiins.controller;

import com.twiiiins.entity.Concert;
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
    public ResponseEntity<List<Concert>> getAllConcerts() {
        return ResponseEntity.ok(concertService.getAllConcerts());
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<Concert>> getUpcomingConcerts() {
        return ResponseEntity.ok(concertService.getUpcomingConcerts());
    }
    
    @GetMapping("/past")
    public ResponseEntity<List<Concert>> getPastConcerts() {
        return ResponseEntity.ok(concertService.getPastConcerts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Concert> getConcertById(@PathVariable Long id) {
        return ResponseEntity.ok(concertService.getConcertById(id));
    }
    
    @PostMapping
    public ResponseEntity<Concert> createConcert(@RequestBody Concert concert) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(concertService.createConcert(concert));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Concert> updateConcert(
            @PathVariable Long id,
            @RequestBody Concert concert) {
        return ResponseEntity.ok(concertService.updateConcert(id, concert));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/move-to-past")
    public ResponseEntity<Concert> moveToPastEvent(@PathVariable Long id) {
        return ResponseEntity.ok(concertService.moveToPastEvent(id));
    }
    
    @PutMapping("/{id}/move-to-upcoming")
    public ResponseEntity<Concert> moveToUpcomingEvent(@PathVariable Long id) {
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

