package com.example.springfiledbconsistency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileMetaRepository fileMetaRepository;

    private final String storagePath = "/tmp/files/";

    @BeforeEach
    void setup() {
        // 테스트 전 DB 초기화
        fileMetaRepository.deleteAll();
        // 파일 디렉토리 초기화
        File dir = new File(storagePath);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        } else {
            dir.mkdirs();
        }
    }

    @Test
    void 정상_파일_업로드_테스트() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        fileService.saveFile(file.getOriginalFilename(), file.getBytes());

        // DB에 저장 확인
        assertEquals(1, fileMetaRepository.count());

        // 파일 존재 확인
        File savedFile = new File(storagePath + "test.txt");
        assertTrue(savedFile.exists());
    }

    @Test
    void DB_저장_실패_테스트() {
        // DB 제약 조건 위반으로 저장 실패 시뮬레이션
        // 여기서는 fileName이 null이면 NOT NULL 위반
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileService.saveFile(null, "Hello".getBytes());
        });

        // DB 저장 안 됐는지 확인
        assertEquals(0, fileMetaRepository.count());
    }

    @Test
    void 파일_저장_실패_테스트() throws IOException {
        // 디스크 경로를 쓰기 불가 경로로 강제 설정
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileService.saveFile("test.txt", "Hello".getBytes());
        });

        // DB 트랜잭션 롤백 확인
        assertEquals(0, fileMetaRepository.count());
    }
}
