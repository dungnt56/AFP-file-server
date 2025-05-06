package com.engskill.file_server.controller;

import com.engskill.file_server.entity.FileStore;
import com.engskill.file_server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileStore> upload(@RequestParam("file") MultipartFile file,
                                            @RequestParam("schema") String schema) throws Exception {
        return ResponseEntity.ok(fileService.upload(file, schema));
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("schema") String schema,
                                           @RequestParam("fileId") String fileId) throws Exception {
        return ResponseEntity.ok(fileService.download(schema, fileId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam("fileId") String fileId) {
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileStore>> list() {
        return ResponseEntity.ok(fileService.findAll());
    }
}
