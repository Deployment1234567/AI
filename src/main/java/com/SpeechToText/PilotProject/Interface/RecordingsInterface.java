package com.SpeechToText.PilotProject.Interface;

import com.SpeechToText.PilotProject.ApiResponse.ApiResponse;
import com.SpeechToText.PilotProject.ApiResponse.JobIdResponse;

public interface RecordingsInterface {
	public ApiResponse getRecording();

	public ApiResponse getTranscript(String jobId);

	public JobIdResponse getJobId(String file);
}
