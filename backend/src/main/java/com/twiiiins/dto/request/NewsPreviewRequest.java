package com.twiiiins.dto.request;
import com.twiiiins.entity.News;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class NewsPreviewRequest {
    @NotNull @Pattern(regexp = "en|de") private String language;
    @Size(max = 255) private String title;
    @Size(max = 255) private String titleDe;
    @Size(max = 100000) private String bodyEn;
    @Size(max = 100000) private String bodyDe;
    @Size(max = 255) private String eventWhenEn;
    @Size(max = 255) private String eventWhenDe;
    @Size(max = 255) private String eventLocationEn;
    @Size(max = 255) private String eventLocationDe;
    @Size(max = 255) private String ctaLabelEn;
    @Size(max = 255) private String ctaLabelDe;
    @Size(max = 2048) private String ctaUrl;
    @Size(max = 50) private java.util.List<@NotBlank @Size(max = 2048) String> imageUrls;
    @Size(max = 20) private java.util.List<@Size(max = 2048) String> videoUrls;
    public News toNews() {
        News item = new News();
        item.setTitle(title);
        item.setTitleDe(titleDe);
        item.setBodyEn(bodyEn);
        item.setBodyDe(bodyDe);
        item.setEventWhenEn(eventWhenEn);
        item.setEventWhenDe(eventWhenDe);
        item.setEventLocationEn(eventLocationEn);
        item.setEventLocationDe(eventLocationDe);
        item.setCtaLabelEn(ctaLabelEn);
        item.setCtaLabelDe(ctaLabelDe);
        item.setCtaUrl(ctaUrl);
        item.setImageUrls(imageUrls == null ? java.util.List.of() : imageUrls);
        item.setVideoUrls(videoUrls == null ? java.util.List.of() : videoUrls);
        return item;
    }
}
