package com.example.musicBackend.external.itunes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesTrackDto {
    private Long trackId;
    private String trackName;
    private String artistName;
    private String collectionName;
    private Long trackTimeMillis;
    private String artworkUrl100;
    private String releaseDate;
    private String previewUrl;
    private String primaryGenreName;
}