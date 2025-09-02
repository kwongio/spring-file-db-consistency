package com.example.springfiledbconsistency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetaRepository fileMetaRepository;

    private final String storagePath = "./tmp/files/";

    private long count = 1L;

    @Transactional
    public void saveFile(String fileName, byte[] content) {
        // 1️⃣ DB에 메타데이터 먼저 저장
        FileMeta meta = new FileMeta();
        meta.setFileName(fileName);
        meta.setSize((long) content.length);
        fileMetaRepository.save(meta);

        // 2️⃣ 디스크에 파일 저장
        File file = new File(storagePath + fileName +  count++ );
        try {
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            }
        } catch (IOException e) {
            // 파일 저장 실패 시 DB 트랜잭션 롤백
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }
    }
}
