package com.SpeechToText.PilotProject.ApiResponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptDto {
	String message;
	String speaker;
}

