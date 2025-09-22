package com.inview.backend.dto;

import lombok.Data;

@Data
public class TtsRequest {
    private String text;
    private String voiceId;                 // 예: "21m00Tcm4TlvDq8ikWAM"
    private String modelId;                 // 예: "eleven_multilingual_v2"
    private String outputFormat;            // 예: "mp3_44100_128"
    private Double stability;               // 옵션(null 가능)
    private Double similarityBoost;         // 옵션(null 가능)
}
