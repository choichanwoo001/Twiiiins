package com.twiiiins.service;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.entity.News;
import com.twiiiins.exception.ResourceNotFoundException;
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
    
    public List<NewsDto> getAllNews() {
        return newsRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public List<NewsDto> getNewsWithFilters(String title, LocalDate startDate, LocalDate endDate) {
        return newsRepository.findNewsWithFilters(title, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public NewsDto getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        return convertToDto(news);
    }
    
    public NewsDto createNews(NewsDto newsDto) {
        News news = convertToEntity(newsDto);
        News savedNews = newsRepository.save(news);
        return convertToDto(savedNews);
    }
    
    public NewsDto updateNews(Long id, NewsDto newsDto) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        
        news.setDate(newsDto.getDate());
        news.setTitle(newsDto.getTitle());
        news.setDescription(newsDto.getDescription());
        news.setDisplayOrder(newsDto.getDisplayOrder());
        
        News savedNews = newsRepository.save(news);
        return convertToDto(savedNews);
    }
    
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
    
    private NewsDto convertToDto(News news) {
        return new NewsDto(
            news.getId(),
            news.getDate(),
            news.getTitle(),
            news.getDescription(),
            news.getDisplayOrder()
        );
    }
    
    private News convertToEntity(NewsDto newsDto) {
        News news = new News();
        news.setDate(newsDto.getDate());
        news.setTitle(newsDto.getTitle());
        news.setDescription(newsDto.getDescription());
        news.setDisplayOrder(newsDto.getDisplayOrder());
        return news;
    }
}
