package com.SpeechToText.PilotProject.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ClientConfiguration {
	@Value("${addressBaseUrl}")
	String addressBaseUrl;
	@Bean
	public WebClient webClient() {
	  return WebClient.builder().baseUrl(addressBaseUrl).build();
	}
}
