package com.SpeechToText.PilotProject.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookPayload {
    public String customerId;
    public String status;
    public String jobId;
}