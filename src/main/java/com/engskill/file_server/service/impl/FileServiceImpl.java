package com.engskill.file_server.service.impl;

import com.engskill.file_server.entity.FileStore;
import com.engskill.file_server.entity.LoadFile;
import com.engskill.file_server.repository.FileStoreRepository;
import com.engskill.file_server.service.FileService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final FileStoreRepository fileRepo;

    public FileServiceImpl(MinioClient minioClient, FileStoreRepository fileRepo) {
        this.minioClient = minioClient;
        this.fileRepo = fileRepo;
    }

    @Override
    public FileStore upload(MultipartFile file, String bucketName) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String fileId = UUID.randomUUID().toString();

        String url = "/" + bucketName + "/" + fileId;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileId)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        FileStore fs = new FileStore();
        fs.setFileId(fileId);
        fs.setFileName(file.getOriginalFilename());
        fs.setUrl(url);
        fs.setFileSize(readableFileSize(file.getSize()));
        fs.setBucketName(bucketName);
        fs.setIsSave(0);
        fs.setCreateTime(Instant.now());

        return fileRepo.save(fs);
    }

    private String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    @Override
    public LoadFile download(String fileId, String schema) throws Exception {
        try {
            // Lấy thông tin object từ MinIO
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(schema).object(fileId).build()
            );
            Optional<FileStore> fileStore = fileRepo.findByFileId(fileId);
            // Lấy InputStream của object
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(schema).object(fileId).build()
            );
            // Xác định Content-Type (hoặc dùng giá trị mặc định)
            String contentType = stat.contentType() != null ? stat.contentType() : "application/octet-stream";
            // Lấy tên file từ metadata hoặc fileId
            String filename = stat.userMetadata().getOrDefault("filename", fileStore.isEmpty() ? fileId : fileStore.get().getFileName());
            return new LoadFile(inputStream.readAllBytes(), filename, contentType);
        } catch (Exception e) {
//            logger.error("Lỗi khi tải file từ MinIO: fileId={}, schema={}", fileId, schema, e);
            return null; // Controller sẽ xử lý null bằng 404
        }
    }

    @Override
    public void delete(String fileId) {
        Optional<FileStore> optional = fileRepo.findByFileId(fileId);
        optional.ifPresent(file -> {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(file.getBucketName()).object(file.getFileId()).build());
                fileRepo.delete(file);
            } catch (Exception e) {
                throw new RuntimeException("Cannot delete file from MinIO", e);
            }
        });
    }

    @Override
    public List<FileStore> findAll() {
        return fileRepo.findAll();
    }
}
