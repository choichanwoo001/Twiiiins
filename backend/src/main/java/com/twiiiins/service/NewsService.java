package com.twiiiins.service;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.dto.request.NewsCreateRequest;
import com.twiiiins.dto.request.NewsUpdateRequest;
import com.twiiiins.entity.News;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.NewsMapper;
import com.twiiiins.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {
    
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    
    public List<NewsDto> getAllNews() {
        return newsRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(newsMapper::toDto)
                .toList();
    }
    
    public List<NewsDto> getNewsWithFilters(String title, LocalDate startDate, LocalDate endDate) {
        return newsRepository.findNewsWithFilters(title, startDate, endDate)
                .stream()
                .map(newsMapper::toDto)
                .toList();
    }
    
    public NewsDto getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        return newsMapper.toDto(news);
    }
    
    public NewsDto createNews(NewsCreateRequest request) {
        News news = newsMapper.toEntity(request);
        News savedNews = newsRepository.save(news);
        return newsMapper.toDto(savedNews);
    }
    
    public NewsDto updateNews(Long id, NewsUpdateRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        
        newsMapper.updateEntityFromUpdateRequest(request, news);
        
        News savedNews = newsRepository.save(news);
        return newsMapper.toDto(savedNews);
    }
    
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
}
