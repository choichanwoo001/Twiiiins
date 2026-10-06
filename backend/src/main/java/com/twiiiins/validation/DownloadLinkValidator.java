package com.twiiiins.validation;

import java.net.URI;
import java.util.Set;

public final class DownloadLinkValidator {
    private static final Set<String> DROPBOX_HOSTS = Set.of("dropbox.com", "www.dropbox.com", "dl.dropboxusercontent.com");
    private DownloadLinkValidator() {}

    public static boolean isValid(String source, String fileUrl) {
        // Old clients and existing uploaded URLs retain their existing validation.
        if (!"dropbox".equals(source)) return true;
        if (fileUrl == null || fileUrl.isBlank()) return false;
        try {
            URI uri = URI.create(fileUrl);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && DROPBOX_HOSTS.contains(uri.getHost().toLowerCase(java.util.Locale.ROOT))
                    && uri.getUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443)
                    && uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
