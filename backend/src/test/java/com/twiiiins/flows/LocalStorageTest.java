package com.twiiiins.flows;
import com.twiiiins.service.LocalFileService;
import com.twiiiins.exception.FileUploadException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class LocalStorageTest {
    @TempDir Path directory;
    LocalFileService storage;
    @BeforeEach void setup(){storage=new LocalFileService();ReflectionTestUtils.setField(storage,"uploadDir",directory.toString());ReflectionTestUtils.setField(storage,"baseUrl","/uploads/");}
    @Test void storesExactBytesInsideStorage() throws Exception {
        assertEquals("/uploads/files/rider.pdf",storage.uploadBytes(new byte[]{1,2,3},"files","rider.pdf"));
        assertArrayEquals(new byte[]{1,2,3},Files.readAllBytes(directory.resolve("files/rider.pdf")));
    }
    @Test void rejectsFolderAndFilenameTraversal() {
        assertThrows(FileUploadException.class,()->storage.uploadBytes(new byte[]{1},"../outside","a.pdf"));
        for(String name:new String[]{"../escape.pdf","..\\escape.pdf","/escape.pdf","..",""}) {
            assertThrows(FileUploadException.class,()->storage.uploadBytes(new byte[]{1},"files",name));
            assertThrows(FileUploadException.class,()->storage.uploadFileAs(new MockMultipartFile("file","a.pdf","application/pdf",new byte[]{1}),"files",name));
        }
    }
}
