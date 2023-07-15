package onlyviewer.display.java.model;

import javafx.scene.image.Image;
import lombok.SneakyThrows;
import onlyviewer.home.java.model.ImageModel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ImageUtil {
    @SneakyThrows
    public static Image decryptDatImage(ImageModel im, Function<InputStream, Image> imageCreator) {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(im.getImageFile().toPath()))) {
            //获取文件
            //file.read()读出来返回的是int类型的数据，通过while循环不断地获取返回值的数据，知道为-1为止

            final int HEADER_LENGTH = 4;

            ByteBuffer fileHeader = ByteBuffer.allocate(HEADER_LENGTH);
            int readLength = in.read(fileHeader.array(), 0, HEADER_LENGTH);
            if (readLength < HEADER_LENGTH)
                throw new IllegalStateException("File length less than header length: actual length = " + readLength + ", file: " + im.getImageFilePath());

            int headerInt = fileHeader.getInt(0);
            Optional<Byte> possibleEncryptByte = guessEncryptByte(headerInt, 0xFFD8FFE1, 0xFFD8FFE0); // jpg
            if (!possibleEncryptByte.isPresent())
                possibleEncryptByte = guessEncryptByte(headerInt, 0x89504E47); // png
            if (!possibleEncryptByte.isPresent())
                throw new IllegalStateException("Unknown DAT file type: header = " + Integer.toHexString(headerInt) + ", file path: " + im.getImageFilePath());

            byte encryptByte = possibleEncryptByte.get();
            InputStream decryptedIn = new InputStream() {
                private int outputByteCount = 0;

                @Override
                public int read() throws IOException {
                    int resultByte;
                    if (outputByteCount < HEADER_LENGTH) {
                        resultByte = fileHeader.get(outputByteCount++);
                    } else {
                        resultByte = in.read();
                        if (resultByte == -1)
                            return -1;
                    }
                    return (resultByte ^ encryptByte) & 0xFF;
                }
            };
//            return new Image(decryptedIn, requestedWidth, requestedHeight,
//                    preserveRatio, smooth);
            return imageCreator.apply(decryptedIn);
        }
    }

    public static Optional<Byte> guessEncryptByte(int actualHeader, int... legalHeaders) {
        for (int legalHeader : legalHeaders) {
            byte firstLegalByte = (byte) (legalHeader >> 24);
            byte possibleEncryptByte = (byte) ((actualHeader >> 24) ^ firstLegalByte);
            int promotedPossibleEncryptByte = possibleEncryptByte & 0xFF;
            int possibleEncryptInt = (promotedPossibleEncryptByte << 24)
                    | (promotedPossibleEncryptByte << 16)
                    | (promotedPossibleEncryptByte << 8)
                    | promotedPossibleEncryptByte;
            int decryptedHeader = actualHeader ^ possibleEncryptInt;
            if (decryptedHeader == legalHeader) {
                return Optional.of(possibleEncryptByte);
            }
        }
        return Optional.empty();
    }

    /*
     * 已知图片是字节对字节异或的，找一张原始图片和加密图片，对比第一个字节，就能得到异或码。
     * JPEG文件的前几个是FFD8FFE1(最后字节也有E0的)，PNG文件的前几个字节是89504E47。
     * 还有一些开头是RIFF的，后缀对图像解析来说不重要，对本程序是否对要加载的图像做解密处理重要
     * 用文件夹中加密图片的第一个字节与这两个文件头的第1个字节相比，算出来异或码，再用来解后面几个字节，看是不是能得到正确文件头，就知道是哪一种文件。
     */
    public static Image createImage(ImageModel im) {
        Image image2;
        if (im.getImageType().equalsIgnoreCase("dat")) {
            image2 = decryptDatImage(im, Image::new);
        } else {
            image2 = new Image(im.getImageFile().toURI().toString());
        }
        return image2;
    }

    public static Image createImage(ImageModel im,
                                    double requestedWidth, double requestedHeight, boolean preserveRatio,
                                    boolean smooth, boolean backgroundLoading) {
        try {
            if (im.getImageType().equalsIgnoreCase("dat")) {
                // TODO 目前，经过解密的图像一定是已经加载到字节流了，所以无法做到backgroundLoading
                return decryptDatImage(im, in -> new Image(in, requestedWidth, requestedHeight,
                        preserveRatio, smooth));
            } else {
                return new Image(im.getImageFile().toURI().toString(), requestedWidth, requestedHeight,
                        preserveRatio, smooth, backgroundLoading);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            return new Image("/onlyviewer/home/resources/images/no_result.png");
        }
    }
}
