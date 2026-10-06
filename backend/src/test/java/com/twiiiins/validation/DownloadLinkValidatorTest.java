package com.twiiiins.validation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DownloadLinkValidatorTest {
    @Test void acceptsDropboxFileAndFolderLinks() {
        assertThat(DownloadLinkValidator.isValid("dropbox", "https://www.dropbox.com/scl/fi/abc/photo.jpg?dl=0")).isTrue();
        assertThat(DownloadLinkValidator.isValid("dropbox", "https://www.dropbox.com/scl/fo/abc?dl=0")).isTrue();
        assertThat(DownloadLinkValidator.isValid("dropbox", "https://dl.dropboxusercontent.com/s/abc/photo.jpg")).isTrue();
    }
    @Test void rejectsUnsafeOrImpersonatingLinks() {
        for (String url : new String[]{"http://www.dropbox.com/s/abc", "https://dropbox.com.evil.test/s/abc", "https://evil.test/dropbox.com/s/abc", "https://user@www.dropbox.com/s/abc", "https://www.dropbox.com:8443/s/abc", "https://www.dropbox.com/", "javascript:alert(1)", "not a url"}) {
            assertThat(DownloadLinkValidator.isValid("dropbox", url)).as(url).isFalse();
        }
        assertThat(DownloadLinkValidator.isValid("dropbox", null)).isFalse();
    }
    @Test void retainsLegacyAndUploadCompatibility() {
        assertThat(DownloadLinkValidator.isValid(null, "https://bucket.s3.amazonaws.com/key.pdf")).isTrue();
        assertThat(DownloadLinkValidator.isValid("upload", "/uploads/file/key.pdf")).isTrue();
    }
}
