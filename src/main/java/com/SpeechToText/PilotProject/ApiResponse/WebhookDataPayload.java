package com.SpeechToText.PilotProject.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookDataPayload {
	    private String event;
	    private Data data;
}
