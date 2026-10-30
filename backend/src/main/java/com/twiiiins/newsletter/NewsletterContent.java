package com.twiiiins.newsletter;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import java.net.URI;
import com.twiiiins.entity.News;

@Component
public class NewsletterContent {
    private static final Safelist ALLOWED = new Safelist().addTags("p", "br", "strong", "em", "h2", "h3", "ul", "ol", "li", "a")
        .addAttributes("a", "href").addProtocols("a", "href", "https").addEnforcedAttribute("a", "rel", "noopener noreferrer");
    public String clean(String html) { return Jsoup.clean(html == null ? "" : html, ALLOWED); }
    public String escape(String text) { return org.jsoup.nodes.Entities.escape(text == null ? "" : text); }
    public String text(String html) {
        var document = Jsoup.parse(html == null ? "" : html);
        for (var link : document.select("a[href]")) link.appendText(" (" + link.attr("href") + ")");
        for (var block : document.select("p,h1,h2,h3,li,br,hr")) block.appendText("\n");
        return document.body().wholeText();
    }
    public String url(String url, String base) {
        URI uri;
        try { uri = URI.create(url.startsWith("/") && !url.startsWith("//") ? base + url : url); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid URL"); }
        URI origin = URI.create(base);
        boolean localPreview = "http".equals(origin.getScheme()) && "localhost".equals(origin.getHost())
            && "http".equals(uri.getScheme()) && java.util.Objects.equals(origin.getAuthority(), uri.getAuthority());
        if ((!"https".equals(uri.getScheme()) && !localPreview) || uri.getHost() == null || uri.getUserInfo() != null) throw new IllegalArgumentException("Use an HTTPS URL");
        return uri.toASCIIString();
    }
    public String buttonUrl(String value) {
        URI uri;
        try { uri = URI.create(value); } catch (Exception e) { throw new IllegalArgumentException("Use an HTTPS button URL"); }
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) throw new IllegalArgumentException("Use an HTTPS button URL");
        return uri.toASCIIString();
    }
    private static final String FOOTER = "<!--NEWSLETTER_FOOTER-->";
    private static final String TEMPLATE = resource("email.html");
    private static final String FOOTER_TEMPLATE = resource("footer.html");
    private static String resource(String name) {
        try (var input = new org.springframework.core.io.ClassPathResource("newsletter/" + name).getInputStream()) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) { throw new IllegalStateException("Missing newsletter template", e); }
    }
    private String expand(String template, java.util.Map<String, String> values) {
        var matcher = java.util.regex.Pattern.compile("\\{\\{(\\w+)\\}\\}").matcher(template);
        return matcher.replaceAll(match -> java.util.regex.Matcher.quoteReplacement(values.getOrDefault(match.group(1), "")));
    }
    private String safe(String value) { return value == null ? "" : value; }
    private String first(String preferred, String fallback) { return preferred == null || preferred.isBlank() ? safe(fallback) : preferred; }
    public boolean completeLanguage(String title, String body) {
        return title != null && !title.isBlank() && !text(clean(body)).replace('\u00a0', ' ').isBlank();
    }
    public String languageFor(String title, String body, String titleDe, String bodyDe, String requested) {
        SubscriptionService.language(requested);
        boolean en = completeLanguage(title, body), de = completeLanguage(titleDe, bodyDe);
        if ("de".equals(requested)) return de || !en ? "de" : "en";
        return en || !de ? "en" : "de";
    }
    public String languageFor(News news, String requested) {
        return languageFor(news.getTitle(), news.getBodyEn(), news.getTitleDe(), news.getBodyDe(), requested);
    }
    public String title(News news, String requested) {
        return "de".equals(languageFor(news, requested)) ? first(news.getTitleDe(), news.getTitle()) : first(news.getTitle(), news.getTitleDe());
    }
    private String image(String value, String base, String alt) {
        return "<p style=\"margin:24px 0;\"><img width=\"640\" style=\"display:block;width:100%;max-width:640px;height:auto;border-radius:16px;\" alt=\"" + escape(alt) + "\" src=\"" + escape(url(value, base)) + "\"></p>";
    }
    private String link(String href, String label) {
        return "<a style=\"color:#80ffcf;text-decoration:underline;\" href=\"" + escape(href) + "\">" + escape(label) + "</a>";
    }
    public String render(News news, String language, String base) {
        SubscriptionService.language(language);
        String selected = languageFor(news, language);
        boolean de = "de".equals(selected);
        String title = title(news, language);
        String when = de ? first(news.getEventWhenDe(), news.getEventWhenEn()) : first(news.getEventWhenEn(), news.getEventWhenDe());
        String location = de ? first(news.getEventLocationDe(), news.getEventLocationEn()) : first(news.getEventLocationEn(), news.getEventLocationDe());
        String label = de ? first(news.getCtaLabelDe(), news.getCtaLabelEn()) : first(news.getCtaLabelEn(), news.getCtaLabelDe());
        var body = Jsoup.parseBodyFragment(clean(de ? news.getBodyDe() : news.getBodyEn()));
        body.select("a").attr("style", "color:#80ffcf;text-decoration:underline;");
        String details = "";
        if (!when.isBlank() || !location.isBlank()) details = "<p style=\"margin:20px 0 28px;text-align:center;font-size:18px;line-height:1.5;color:#fff;\">"
            + escape(when) + (!when.isBlank() && !location.isBlank() ? "<br>" : "") + escape(location) + "</p>";
        var images = news.getImageUrls() == null ? java.util.List.<String>of() : news.getImageUrls();
        var gallery = new StringBuilder();
        for (int i = 1; i < images.size(); i++) gallery.append(image(images.get(i), base, ""));
        var actions = new StringBuilder();
        if (news.getCtaUrl() != null && !news.getCtaUrl().isBlank()) {
            String href = buttonUrl(news.getCtaUrl());
            if (!label.isBlank()) actions.append("<p style=\"margin:28px 0;\"><a style=\"display:inline-block;background:#80ffcf;color:#050505;padding:14px 24px;font-size:16px;font-weight:bold;text-decoration:none;\" href=\"")
                .append(escape(href)).append("\">").append(escape(label)).append("</a></p>");
        }
        if (news.getVideoUrls() != null) for (String video : news.getVideoUrls()) {
            if (!video.isBlank()) actions.append("<p>").append(link(url(video, base), de ? "Video ansehen" : "Watch video")).append("</p>");
        }
        return expand(TEMPLATE, java.util.Map.of("language", selected, "title", escape(title), "hero", images.isEmpty() ? "" : image(images.get(0), base, title),
            "details", details, "body", body.body().html(), "gallery", gallery.toString(), "actions", actions.toString()));
    }
    public String complete(String html, String language, NewsletterSettings settings, String view, String unsubscribe, boolean preview) {
        boolean de = "de".equals(language);
        String operator = safe(settings.getOperator());
        String reply = safe(settings.getReplyTo());
        String footer = expand(FOOTER_TEMPLATE, java.util.Map.of(
            "operator", escape(operator.isBlank() && preview ? (de ? "Beispiel: Veranstalter" : "Example: sender information") : operator),
            "replyTo", escape(reply.isBlank() && preview ? "Example: contact@example.com" : reply),
            "view", view == null ? (preview ? "<span>" + (de ? "Im Browser ansehen (Vorschau)" : "View in browser (preview)") + "</span>" : "") : link(view, de ? "Im Browser ansehen" : "View in browser"),
            "unsubscribe", unsubscribe == null ? (preview ? "<span>" + (de ? "Abmelden (Vorschau)" : "Unsubscribe (preview)") + "</span>" : "") : link(unsubscribe, de ? "Abmelden" : "Unsubscribe")));
        if (html.contains(FOOTER)) return html.replace(FOOTER, footer);
        // Already rendered test emails and legacy queued fragments keep their original body.
        if (html.stripLeading().toLowerCase(java.util.Locale.ROOT).startsWith("<!doctype html>")) return html;
        return "<!doctype html><html><body><div style=\"max-width:640px;margin:auto;font-family:Arial,sans-serif;line-height:1.6\">" + html + footer + "</div></body></html>";
    }
}
