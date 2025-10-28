package com.twiiiins.service;

import com.twiiiins.entity.News;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {
    
    private final NewsRepository newsRepository;
    
    public List<News> getAllNews() {
        return newsRepository.findAllByOrderByDateDesc();
    }
    
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
    }
    
    public News createNews(News news) {
        return newsRepository.save(news);
    }
    
    public News updateNews(Long id, News newsDetails) {
        News news = getNewsById(id);
        news.setDate(newsDetails.getDate());
        news.setTitle(newsDetails.getTitle());
        news.setDescription(newsDetails.getDescription());
        news.setDisplayOrder(newsDetails.getDisplayOrder());
        return newsRepository.save(news);
    }
    
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
}
