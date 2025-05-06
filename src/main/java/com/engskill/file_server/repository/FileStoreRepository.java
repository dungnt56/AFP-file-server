package com.engskill.file_server.repository;

import com.engskill.file_server.entity.FileStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileStoreRepository extends JpaRepository<FileStore, Long> {
    Optional<FileStore> findByFileId(String fileId);
}