package com.twiiiins.service;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.dto.request.*;
import com.twiiiins.entity.News;
import com.twiiiins.mapper.NewsMapper;
import com.twiiiins.repository.NewsRepository;
import com.twiiiins.newsletter.*;
import com.twiiiins.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final NewsletterContent content;
    private final NewsletterSettings settings;
    private final MailingRepository mailings;
    private boolean published(News n) { return "PUBLISHED".equals(n.getStatus()); }
    public NewsDto localize(NewsDto dto, String language) {
        SubscriptionService.language(language);
        if ("de".equals(language) && dto.getTitleDe() != null && !dto.getTitleDe().isBlank()) dto.setTitle(dto.getTitleDe());
        String body = "de".equals(language) && dto.getBodyDe() != null && !dto.getBodyDe().isBlank() ? dto.getBodyDe() : dto.getBodyEn();
        dto.setBody(body == null || body.isBlank() ? "<p>" + content.escape(dto.getDescription()) + "</p>" : body);
        return dto;
    }
    public List<NewsDto> getAllNews() {
        return newsRepository.findAllByOrderByDisplayOrderAsc().stream().filter(this::published).map(newsMapper::toDto).toList();
    }
    public List<NewsDto> getNewsWithFilters(String title, LocalDate startDate, LocalDate endDate) {
        return newsRepository.findNewsWithFilters(title, startDate, endDate).stream().filter(this::published).map(newsMapper::toDto).toList();
    }
    public List<NewsDto> adminList() {
        return newsRepository.findAllByOrderByDisplayOrderAsc().stream().map(newsMapper::toDto).toList();
    }
    public List<NewsDto> adminList(String source) {
        return adminList().stream().filter(n -> source == null || source.equals(n.getSource())).toList();
    }
    public News getEntity(Long id) {
        return newsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("News not found"));
    }
    public NewsDto getNewsById(Long id) {
        News news = getEntity(id);
        if (!published(news)) throw new ResourceNotFoundException("News not found");
        return newsMapper.toDto(news);
    }
    public NewsDto adminGet(Long id) { return newsMapper.toDto(getEntity(id)); }
    public NewsDto manualGet(Long id) {
        News item = getEntity(id); requireSource(item, News.NEWS); return newsMapper.toDto(item);
    }
    public NewsDto newsletterGet(Long id) {
        News item = getEntity(id); requireSource(item, News.NEWSLETTER); return newsMapper.toDto(item);
    }
    public void requireSource(News item, String expected) {
        if (!expected.equals(item.getSource())) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.CONFLICT,
            News.NEWS.equals(expected) ? "Newsletter 글은 Newsletter 메뉴에서 수정하세요." : "일반 News는 Media > News 메뉴에서 관리하세요.");
    }
    private String plainBody(String description) {
        return "<p>" + content.escape(description).replace("\n", "<br>") + "</p>";
    }
    private void validateImage(String url, boolean newsletter) {
        java.net.URI uri = java.net.URI.create(url);
        if (url.startsWith("/uploads/") && uri.normalize().equals(uri) && uri.getHost() == null) return;
        if (!newsletter && ("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
            && uri.getHost() != null && uri.getUserInfo() == null) return;
        content.url(url, settings.getPublicUrl());
    }
    private void sanitize(News news) {
        news.setTitle(news.getTitle().replaceAll("[\\p{Cntrl}]", "").strip());
        if (news.getTitle().isBlank()) throw new IllegalArgumentException("Title is required.");
        if (news.getTitleDe() != null) news.setTitleDe(news.getTitleDe().replaceAll("[\\p{Cntrl}]", "").strip());
        news.setBodyEn(content.clean(news.getBodyEn()));
        news.setBodyDe(content.clean(news.getBodyDe()));
        if (news.getImageUrls().size() > 50 || news.getVideoUrls().size() > 20) throw new IllegalArgumentException("Too many images or video links.");
        if (news.getCtaUrl() != null && !news.getCtaUrl().isBlank()) content.buttonUrl(news.getCtaUrl());
        for (String url : news.getVideoUrls()) content.url(url, settings.getPublicUrl());
        for (String url : news.getImageUrls()) validateImage(url, News.NEWSLETTER.equals(news.getSource()));
    }
    private boolean present(String value) { return value != null && !value.isBlank(); }
    private void requirePair(String en, String de, String name) {
        if (present(en) != present(de)) throw new IllegalArgumentException(name + " requires English and German text.");
    }
    public void validatePublication(News news) {
        requireSource(news, News.NEWSLETTER);
        requirePair(news.getEventWhenEn(), news.getEventWhenDe(), "Event time");
        requirePair(news.getEventLocationEn(), news.getEventLocationDe(), "Event location");
        if (present(news.getCtaUrl()) || present(news.getCtaLabelEn()) || present(news.getCtaLabelDe())) {
            if (!present(news.getCtaUrl()) || !present(news.getCtaLabelEn()) || !present(news.getCtaLabelDe()))
                throw new IllegalArgumentException("Button URL and English and German labels are required together.");
            content.buttonUrl(news.getCtaUrl());
        }
        if (news.getTitle() == null || news.getTitle().isBlank() || news.getTitleDe() == null || news.getTitleDe().isBlank()
            || content.text(content.clean(news.getBodyEn())).isBlank() || content.text(content.clean(news.getBodyDe())).isBlank())
            throw new IllegalArgumentException("English and German titles and content are required.");
    }
    @Transactional public NewsDto createNews(NewsCreateRequest request) {
        News news = newsMapper.toEntity(request);
        news.setSource(News.NEWSLETTER);
        news.setStatus("DRAFT");
        sanitize(news);
        return newsMapper.toDto(newsRepository.saveAndFlush(news));
    }
    @Transactional public NewsDto createManualNews(NewsCreateRequest request) {
        News item = newsMapper.toEntity(request);
        item.setSource(News.NEWS); item.setStatus("PUBLISHED");
        item.setTitleDe(null); item.setBodyDe(null); item.setVideoUrls(new ArrayList<>());
        item.setBodyEn(plainBody(item.getDescription()));
        sanitize(item);
        return newsMapper.toDto(newsRepository.saveAndFlush(item));
    }
    @Transactional public NewsDto updateNews(Long id, NewsUpdateRequest request) {
        return update(id, request, News.NEWSLETTER);
    }
    @Transactional public NewsDto updateManualNews(Long id, NewsUpdateRequest request) {
        return update(id, request, News.NEWS);
    }
    private NewsDto update(Long id, NewsUpdateRequest request, String source) {
        News news = newsRepository.lockById(id).orElseThrow(() -> new ResourceNotFoundException("News not found"));
        requireSource(news, source);
        if (request.getVersion() != null && !Objects.equals(request.getVersion(), news.getVersion()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "News changed. Reload before saving.");
        if (News.NEWS.equals(source)) {
            if (request.getDate() != null) news.setDate(request.getDate());
            if (request.getTitle() != null) news.setTitle(request.getTitle());
            if (request.getDescription() != null) news.setDescription(request.getDescription());
            if (request.getDisplayOrder() != null) news.setDisplayOrder(request.getDisplayOrder());
            if (request.getImageUrls() != null) news.setImageUrls(new ArrayList<>(request.getImageUrls()));
            news.setBodyEn(plainBody(news.getDescription()));
        } else newsMapper.updateEntityFromUpdateRequest(request, news);
        sanitize(news);
        return newsMapper.toDto(newsRepository.saveAndFlush(news));
    }
    @Transactional public NewsDto publish(Long id) {
        News news = newsRepository.lockById(id).orElseThrow(() -> new ResourceNotFoundException("News not found"));
        validatePublication(news);
        news.setStatus("PUBLISHED");
        return newsMapper.toDto(newsRepository.saveAndFlush(news));
    }
    @Transactional public void deleteNews(Long id) {
        delete(id, News.NEWSLETTER);
    }
    @Transactional public void deleteManualNews(Long id) {
        delete(id, News.NEWS);
    }
    private void delete(Long id, String source) {
        News news = newsRepository.lockById(id).orElseThrow(() -> new ResourceNotFoundException("News not found"));
        requireSource(news, source);
        if (mailings.existsByNewsId(id)) news.setArchived(true);
        else newsRepository.delete(news);
    }
}
