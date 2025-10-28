package com.twiiiins.controller;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<List<NewsDto>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsDto> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }
    
    @PostMapping
    public ResponseEntity<NewsDto> createNews(@RequestBody NewsDto newsDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.createNews(newsDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NewsDto> updateNews(
            @PathVariable Long id,
            @RequestBody NewsDto newsDto) {
        return ResponseEntity.ok(newsService.updateNews(id, newsDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}
