package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import com.twiiiins.validation.ValidMediaUrl;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import com.twiiiins.validation.DownloadLinkValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadFileCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @ValidMediaUrl
    @Size(max = 2048)
    private String fileUrl;

    @PositiveOrZero
    private Integer displayOrder;
    @Pattern(regexp = "upload|dropbox")
    private String downloadSource;

    @AssertTrue(message = "Dropbox links must use HTTPS and an allowed Dropbox host")
    @JsonIgnore
    public boolean isDownloadSourceValid() {
        return DownloadLinkValidator.isValid(downloadSource, fileUrl);
    }
}


