package com.twiiiins.flows;
import com.twiiiins.service.ImageResizeService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
class ImageResizeTest {
    @ParameterizedTest @CsvSource({"1200,800", "800,1200", "900,900", "2000,400", "400,1600"})
    void variantsPreserveRatioAndRespectSize(int width,int height) throws Exception {
        var out=new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB),"png",out);
        var file=new MockMultipartFile("file","photo.png","image/png",out.toByteArray()); var resize=new ImageResizeService();
        for(byte[] bytes: new byte[][]{resize.resizeToMaxLongEdge(file,640,0.85f),resize.generateThumbnail(file,400,400,0.85f)}) {
            var image=ImageIO.read(new ByteArrayInputStream(bytes)); assertNotNull(image);
            assertTrue(Math.max(image.getWidth(),image.getHeight())<=640);
            assertEquals((double)width/height,(double)image.getWidth()/image.getHeight(),0.02);
        }
    }
}
