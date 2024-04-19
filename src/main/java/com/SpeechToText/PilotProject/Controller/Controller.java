package com.SpeechToText.PilotProject.Controller;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import com.SpeechToText.PilotProject.ApiResponse.ApiResponse;
import com.SpeechToText.PilotProject.ApiResponse.JobIdResponse;
import com.SpeechToText.PilotProject.Interface.RecordingsInterface;
import com.SpeechToText.PilotProject.Service.RecordingsImplementation;

@RestController()
@RequestMapping("/SpeechToText")
public class Controller {
	public static final Logger log = org.apache.logging.log4j.LogManager.getLogger(RecordingsImplementation.class);
	
	@Autowired
	RecordingsInterface recording;

	@Autowired
	private WebClient webClient;
	
	@Value("${internalCall}")
	String internalCall;
	
	@Value("${internalCalling}")
	String internalCalling;
	
	String jobId="";
	  
	@GetMapping("/getTranscript")
	public ResponseEntity<ApiResponse> getRecording() {
		try {
			ApiResponse response = recording.getRecording();
			if(response.getStatus().equalsIgnoreCase("404") || response.getStatus().equalsIgnoreCase("500")) {
				Integer responseCode = Integer.parseInt(response.getStatus()); 
				return ResponseEntity.status(responseCode).body(new ApiResponse(response.getStatus(),response.getMessage(),response.getErrorMessage(),response.getData()));
			}
			String recordedFilePath = response.getData().toString();
			log.info("this is recorded file path"+recordedFilePath);
			
			
			JobIdResponse getJobId = webClient.post().uri(internalCall+"/getJobId").bodyValue(recordedFilePath)
					.retrieve().bodyToMono(JobIdResponse.class).block();
			if(getJobId.getStatus().equalsIgnoreCase("404") || getJobId.getStatus().equalsIgnoreCase("500")) {
				Integer responseCode  = Integer.parseInt(response.getStatus());
				return ResponseEntity.status(responseCode).body(new ApiResponse(response.getStatus(),response.getMessage(),response.getErrorMessage(),response.getData()));
			}
			jobId = getJobId.getJobId();
			
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("200","Job Id Created Successfully",null,jobId));
		}
		catch(Exception e) {
			ApiResponse errorResponse = new ApiResponse("500","INTERNAL_SERVER_ERROR",e.getMessage(),null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@PostMapping("/getJobId")
	public ResponseEntity<JobIdResponse> getJobId(@RequestBody String filePath) {
		try {
			JobIdResponse response = recording.getJobId(filePath);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		catch(Exception e) {
			JobIdResponse errorResponse = new JobIdResponse("500","INTERNAL SERVER ERROR", false ,e.getMessage(),null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
		
	}
	
	@PostMapping("/getTranscript")
	public ResponseEntity<ApiResponse> getTranscriptFromId(@RequestBody String jobId) {
		try {
			ApiResponse successResponse = recording.getTranscript(jobId);
			return ResponseEntity.status(HttpStatus.OK).body(successResponse);
		}
		catch(Exception e) {
			ApiResponse errorResponse = new ApiResponse("500","INTERNAL SERVER_ERROR",null,null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	
	  @PostMapping("/getStatusOfJobId")
	  public ResponseEntity<ApiResponse> webhookEventListener(@RequestBody String payload) {
		  log.info("Webhook Payload ----->"+payload);
		  String errorMessage="";
		  try {
		        int statusStartIndex = payload.indexOf("\"status\":\"") + "\"status\":\"".length();
		        int statusEndIndex = payload.indexOf("\",", statusStartIndex);
		        String status = payload.substring(statusStartIndex, statusEndIndex);
		        if(status.equalsIgnoreCase("Completed")) {
		        	   ApiResponse responseBody = webClient.post().uri(internalCall+"/getTranscript").bodyValue(jobId)
								.retrieve().bodyToMono(ApiResponse.class).block();
						log.info(responseBody);
						errorMessage = responseBody.getErrorMessage();
						if (responseBody.getStatus().equals("200"))
							return ResponseEntity.status(200).body(responseBody);
		        }
		        else if(status.equalsIgnoreCase("Failed")) {
		        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("500","Transcription failed",errorMessage,null));
		        }		     
	        } catch (Exception e) {
	        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("500","Transcription Failed",e.getMessage(),null));
	        }
      	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("500","Transcription failed",errorMessage,null));
	    }
}

