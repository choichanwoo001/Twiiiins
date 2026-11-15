package com.twiiiins.service;

import com.twiiiins.entity.News;
import com.twiiiins.newsletter.MailingRepository;
import com.twiiiins.newsletter.NewsletterContent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class NewsPublication {
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";

    private final NewsletterContent content;
    private final MailingRepository mailings;

    void initialize(News news, String source) {
        news.setSource(source);
        news.setStatus(News.NEWS.equals(source) ? PUBLISHED : DRAFT);
    }

    boolean listed(News news) {
        return reachableByLink(news) && !news.isArchived();
    }

    boolean reachableByLink(News news) {
        return PUBLISHED.equals(news.getStatus());
    }

    public void requireSource(News news, String expected) {
        if (!expected.equals(news.getSource())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                News.NEWS.equals(expected) ? "Newsletter 글은 Newsletter 메뉴에서 수정하세요."
                    : "일반 News는 Media > News 메뉴에서 관리하세요.");
        }
    }

    void publish(News news) {
        validateNewsletter(news);
        news.setStatus(PUBLISHED);
    }

    /** Apply the publishing option inside the transaction that records the mailing. */
    public void prepareMailing(News news, boolean publishToNews) {
        if (news.isArchived()) throw new IllegalArgumentException("Archived news cannot be sent");
        if (publishToNews) {
            publish(news);
        } else {
            if (!DRAFT.equals(news.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only unpublished newsletters can be sent by email only.");
            }
            validateNewsletter(news);
        }
    }

    boolean archiveForRemoval(News news, String source) {
        requireSource(news, source);
        if (!mailings.existsByNewsId(news.getId())) return false;
        news.setArchived(true);
        return true;
    }

    private void validateNewsletter(News news) {
        requireSource(news, News.NEWSLETTER);
        boolean hasButtonUrl = present(news.getCtaUrl());
        boolean hasButtonLabel = present(news.getCtaLabelEn()) || present(news.getCtaLabelDe());
        if (hasButtonUrl != hasButtonLabel) {
            throw new IllegalArgumentException("Button URL and at least one button label are required together.");
        }
        if (hasButtonUrl) content.buttonUrl(news.getCtaUrl());
        if (!content.completeLanguage(news.getTitle(), news.getBodyEn())
            && !content.completeLanguage(news.getTitleDe(), news.getBodyDe())) {
            throw new IllegalArgumentException("Enter a title and content together in English or German.");
        }
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }
}
