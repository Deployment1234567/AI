package com.SpeechToText.PilotProject.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.SpeechToText.PilotProject.Repo.FileRepository;
import com.SpeechToText.PilotProject.Entity.FileEntity;

import java.io.IOException;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Transactional
    public String saveFileToDatabase(MultipartFile file) throws IOException {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileData(file.getBytes());
        fileEntity = fileRepository.save(fileEntity);
        return fileEntity.getFileName();
    }

    public byte[] getFileFromDatabase(Long id) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(id);
        if (fileEntityOptional.isPresent()) {
            return fileEntityOptional.get().getFileData();
        } else {
            throw new RuntimeException("File not found with id: " + id);
        }
    }
}

