package com.streaming.jflix.service;

import com.streaming.jflix.binarystorage.MinioStorageService;
import com.streaming.jflix.enity.FileMetadataEntity;
import com.streaming.jflix.exception.StorageException;
import com.streaming.jflix.repository.FileMetadataRepository;
import com.streaming.jflix.util.Range;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultVideoService implements VideoService {

    private final MinioStorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;

    @Override
    @Transactional
    public UUID save(MultipartFile video) {
        try {
            UUID fileUuid = UUID.randomUUID();
            FileMetadataEntity metadata = FileMetadataEntity.builder()
                    .id(fileUuid.toString())
                    .size(video.getSize())
                    .httpContentType(video.getContentType())
                    .build();
            fileMetadataRepository.save(metadata);
            storageService.save(video, fileUuid);
            return fileUuid;
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file", ex);
            throw new StorageException(ex);
        }
    }

    @Override
    public ChunkWithMetadata fetchChunk(UUID uuid, Range range) {
        FileMetadataEntity fileMetadata = fileMetadataRepository.findById(uuid.toString()).orElseThrow();
        return new ChunkWithMetadata(fileMetadata, readChunk(uuid, range, fileMetadata.getSize()));
    }

    private byte[] readChunk(UUID uuid, Range range, long fileSize) {
        long startPosition = range.getRangeStart();
        long endPosition = range.getRangeEnd(fileSize);
        int chunkSize = (int) (endPosition - startPosition + 1);
        try(InputStream inputStream = storageService.getInputStream(uuid, startPosition, chunkSize)) {
            return inputStream.readAllBytes();
        } catch (Exception exception) {
            log.error("Exception occurred when trying to read file with ID = {}", uuid);
            throw new StorageException(exception);
        }
    }

    public record ChunkWithMetadata(
            FileMetadataEntity metadata,
            byte[] chunk
    ) {}

}
