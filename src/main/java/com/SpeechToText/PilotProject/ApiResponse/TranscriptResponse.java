package com.SpeechToText.PilotProject.ApiResponse;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptResponse {
	ArrayList<TranscriptDto> transcript;
	String summary;
	String sentiment;
}
