package com.streaming.jflix.enity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataEntity {
    @Id
    private String id;
    private long size;
    private String httpContentType;
}
