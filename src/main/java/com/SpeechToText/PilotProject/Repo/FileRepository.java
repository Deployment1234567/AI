package com.SpeechToText.PilotProject.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SpeechToText.PilotProject.Entity.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
