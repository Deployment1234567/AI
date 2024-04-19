package com.SpeechToText.PilotProject.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.SpeechToText.PilotProject.ApiResponse.ApiResponse;
import com.SpeechToText.PilotProject.ApiResponse.JobIdResponse;
import com.SpeechToText.PilotProject.ApiResponse.TranscriptDto;
import com.SpeechToText.PilotProject.ApiResponse.TranscriptResponse;
import com.SpeechToText.PilotProject.Repo.RecordingRepo;
import com.SpeechToText.PilotProject.Entity.Recordings;
import com.SpeechToText.PilotProject.Interface.RecordingsInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RecordingsImplementation implements RecordingsInterface {
	
	public static final Logger log = org.apache.logging.log4j.LogManager.getLogger(RecordingsImplementation.class);

	@Autowired
	RecordingRepo recordingDao;
	
	@Autowired
	WebClient webClient;
	
	@Value("${apiToken}")
	String token;

	@Value("${addressBaseUrl}")
	String transcriptUrl;

	@Value("${createJobBaseUrl}")
	String jobCreatingUrl;
	
	@Override
	public ApiResponse getRecording() {
		try {
			Optional<Recordings> recordingPath = recordingDao.findById(1);
			Recordings record = null;
			if(recordingPath.isPresent()) {
				record = recordingPath.get();
				String filePath = record.getFilePath();
				log.info("Recording Fetched"+filePath);
				log.info(record);
				return setApiResponse("200", "Path fetched from database", null, filePath);
			}
			else {
				log.warn("Recording with this id is not present");
				return new ApiResponse("404","Not Found","File Path with this id is not present in databse",null);
			}
		}
		catch(Exception e) {
			return setApiResponse("500", "INTERNAL_SERVER_ERROR", e.getMessage(), e);
		}
	}
	public ApiResponse setApiResponse(String status, String message,String errorMessage, Object data) {
        ApiResponse response = new ApiResponse(status,message,errorMessage,data);
        return response;
	}
	@Override
	public ApiResponse getTranscript(String jobId) {
		String token = this.token;
		ArrayList<TranscriptResponse> transcript = new ArrayList<TranscriptResponse>();
		ArrayList<TranscriptDto> transcriptDto = new ArrayList<TranscriptDto>();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", token);
		log.info(transcriptUrl+"/jobs/"+jobId);
		String responseBody = webClient.get().uri(transcriptUrl+"/jobs/"+jobId)
				.headers(httpHeaders -> httpHeaders.addAll(headers)).retrieve().bodyToMono(String.class).block();
		log.warn(responseBody);
		ObjectMapper objectMapper = new ObjectMapper();
			try {
				String summary="",sentiment="";
			    JsonNode jsonNode = objectMapper.readTree(responseBody);
			    JsonNode dataNode = jsonNode.get("data");
			    if (dataNode != null) {
			        JsonNode resultNode = dataNode.get("result");
			        String statusNode = dataNode.get("status").asText();
			        log.info(statusNode);
			        if (resultNode != null) {
			            JsonNode transcriptionNode = resultNode.get("transcription");
			            if (resultNode != null) {
			                JsonNode sentimentNode = resultNode.get("sentiment_detection");
			                if (sentimentNode != null) {
			                    sentiment = sentimentNode.get("overall").asText();
			                }
			            }
			            if (transcriptionNode != null) {
			                JsonNode segmentsNode = transcriptionNode.get("segments");
			                log.info(transcriptionNode.has("summary"));
			                    summary = transcriptionNode.get("summary").asText();
			                    summary = removeNewLines(summary);
			                    summary = removeSummaryFromStarting(summary);
			                    log.info(summary);
			                if (segmentsNode != null && segmentsNode.isArray()) {
			                    for (JsonNode segment : segmentsNode) {
			                    	String currentSpeaker="";
			                        String text = segment.get("text").asText();
			                        String speaker = segment.get("speaker").asText();
			                        currentSpeaker = speaker.equalsIgnoreCase("Speaker 0") ? "Agent" : "Customer";
			                        TranscriptDto editedTranscriptResponse = new TranscriptDto(text,currentSpeaker);
			                        transcriptDto.add(editedTranscriptResponse);
			                    }
			                } else {
			                    log.error("Error: 'segments' is either null or not an array");
			                }
			            }
			        }
			    }
                TranscriptResponse transcriptResponse = new TranscriptResponse(transcriptDto, summary, sentiment);
			    transcript.add(transcriptResponse);
				return new ApiResponse("200", "Data fetched", null, transcript);	
			} catch (Exception e) {
			    e.printStackTrace();
			    return new ApiResponse("500","INTERNAL SERVER ERROR",e.getMessage(),null);
			}
	}
	public static String removeNewLines(String summary) {
	    String result = summary.replaceAll("\n", "");
	    return result;
	}
	public static String removeSummaryFromStarting(String summary) {
		String result = summary.substring(9);
		return result;
	}
	@Override
	public JobIdResponse getJobId(String filePath) {
	    try {
        File file = new File(filePath);
        if (!file.exists()) {
            return new JobIdResponse("404", "File Not Found", false, null,null);
        }

	        // Read the content of the file into a FileSystemResource
	        FileSystemResource fileResource = new FileSystemResource(file);
	    	
	        // Get JSON data for configuration
	        Object configJsonValue = getJsonData();

	        // Create multipart form data
	        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
	        formData.add("config", configJsonValue);
	        formData.add("files", fileResource);
            log.info(fileResource);
	        // Set headers with authorization token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", this.token);

	        // Send multipart form data request using WebClient
	        String responseBody = webClient.post()
	                .uri(jobCreatingUrl)
	                .contentType(MediaType.MULTIPART_FORM_DATA)
	                .headers(httpHeaders -> httpHeaders.addAll(headers))
	                .body(BodyInserters.fromMultipartData(formData))
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
	        try {
	        	ObjectMapper objectMapper = new ObjectMapper();
	            JsonNode jsonNode = objectMapper.readTree(responseBody);
			    JsonNode dataNode = jsonNode.get("success");
			    JsonNode messageNode = jsonNode.get("message");
			    JsonNode datajobIdNode = jsonNode.get("data");
			    log.info("This is jobId Node :: -->"+datajobIdNode);
			    JobIdResponse response = new JobIdResponse();
			    boolean jobIdStatus = dataNode.asBoolean();
			    String jobIdMessage = messageNode.asText();
			    log.info(jobIdStatus);
			    log.info(jobIdMessage);
			    if(dataNode != null) {
			    	if(jobIdStatus)  response.setSuccess(true);
			    	else response.setSuccess(false);
			    }	
			    if(dataNode!=null) {
			    	response.setMessage(jobIdMessage);
			    }
			    if(datajobIdNode!=null) {
			    	JsonNode job = datajobIdNode.get("jobId");
			    	String jobId = job.asText();
			    	response.setJobId(jobId);
			    	log.info("Job id received ::-->"+jobId);
			    }
	            return new JobIdResponse("200", null , true, response.getMessage(), response.getJobId());
	        }
	        catch(Exception e) {
	        	return new JobIdResponse("500","INTERNAL_SERVER_ERROR", false , e.getMessage() , null);
	        }
	    } catch (Exception e) {
	        log.error("Error while sending file: {}", e.getMessage());
	        return new JobIdResponse("500", "INTERNAL_SERVER_ERROR", false , e.getMessage(), null);
	    }
	}
	
	 public static String getJsonData() {
	            String jsonData = "{\n" +
	                    "      \"file_transcription\": {\n" +
	                    "        \"language_id\": \"en\",\n" +
	                    "        \"mode\": \"advanced\"\n" +
	                    "      },\n" +
	                    "      \"speaker_diarization\": {\n" +
	                    "        \"mode\": \"speakers\",\n" +
	                    "        \"num_speakers\": 2,\n" +
	                    "        \"overrides\": {\n" +
	                    "          \"clustering\": {\n" +
	                    "            \"threshold\": 0.5\n" +
	                    "          }\n" +
	                    "        }\n" +
	                    "      },\n" +
	                    "      \"summarize\":true,\n" +
	                    "      \"sentiment_detect\":true\n" +
	                    "    }";
	            return jsonData;
	    }
}

