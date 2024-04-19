package com.SpeechToText.PilotProject.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
	String status;
	String message;
	String errorMessage;
	Object data;
}
