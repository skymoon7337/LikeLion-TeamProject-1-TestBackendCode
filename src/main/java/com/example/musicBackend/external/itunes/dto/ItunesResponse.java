package com.example.musicBackend.external.itunes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesResponse {
    private int resultCount;
    private List<ItunesTrackDto> results;
}