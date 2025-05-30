package com.engskill.file_server.controller;

import com.engskill.file_server.entity.FileStore;
import com.engskill.file_server.entity.LoadFile;
import com.engskill.file_server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileStore> upload(@RequestParam("fileUp") MultipartFile file,
                                            @RequestParam("schema") String schema) throws Exception {
        return ResponseEntity.ok(fileService.upload(file, schema));
    }


    @GetMapping("/download/{schema}/{fileId}")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable("schema") String schema,
            @PathVariable("fileId") String fileId) {
        try {
            // Validate inputs
            if (schema == null || schema.isBlank() || fileId == null || fileId.isBlank()) {
                logger.warn("Input không hợp lệ: schema={}, fileId={}", schema, fileId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }


            // Lấy thông tin file từ MinIO
            LoadFile loadFile = fileService.download(fileId, schema);
            if (loadFile == null || loadFile.getInputStream() == null) {
                logger.error("Không tìm thấy file: fileId={}, schema={}", fileId, schema);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Chuẩn bị Content-Disposition cho tải xuống
            logger.info("Tên file: {}", loadFile.getFilename());
            String contentDisposition = "attachment;filename*=utf-8''" +
                    URLEncoder.encode(loadFile.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");

            // Xác định Content-Type
            MediaType mediaType;
            try {
                mediaType = MediaType.valueOf(loadFile.getFileType());
            } catch (Exception e) {
                logger.warn("Loại media không hợp lệ: {}, mặc định là application/octet-stream", loadFile.getFileType());
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // Trả về file dưới dạng InputStreamResource
            return ResponseEntity.ok()
                    .header("Content-Disposition", contentDisposition)
                    .contentType(mediaType)
                    .body(new ByteArrayResource(loadFile.getInputStream()));

        } catch (IllegalArgumentException e) {
            logger.error("Lỗi input hoặc giải mã: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý yêu cầu tải xuống: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
