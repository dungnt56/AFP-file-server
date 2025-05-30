package com.engskill.file_server.service;

import com.engskill.file_server.entity.FileStore;
import com.engskill.file_server.entity.LoadFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface FileService {
    FileStore upload(MultipartFile file, String schema) throws Exception;
    LoadFile download(String schema, String fileId) throws Exception;
    void delete(String fileId);
    List<FileStore> findAll();
}
