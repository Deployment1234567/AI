package com.SpeechToText.PilotProject.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SpeechToText.PilotProject.Entity.Recordings;

public interface RecordingRepo extends JpaRepository<Recordings, Integer>{

}