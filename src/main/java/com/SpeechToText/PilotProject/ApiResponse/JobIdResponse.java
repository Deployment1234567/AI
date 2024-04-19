package com.SpeechToText.PilotProject.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobIdResponse {
	String status;
	String errorMessage;
	boolean success;
	String message;
    String jobId;
}
