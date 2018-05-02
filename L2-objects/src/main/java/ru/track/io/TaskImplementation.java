package ru.track.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.track.io.vendor.Bootstrapper;
import ru.track.io.vendor.FileEncoder;
import ru.track.io.vendor.ReferenceTaskImplementation;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public final class TaskImplementation implements FileEncoder {
    static int buffsize = 1023 * 8;

    /**
     * @param finPath  where to read binary data from
     * @param foutPath where to write encoded data. if null, please create and use temporary file.
     * @return file to read encoded data from
     * @throws IOException is case of input/output errors
     */
    @NotNull
    public File encodeFile(@NotNull String finPath, @Nullable String foutPath) throws IOException {

        final File fin = new File(finPath);
        final File fout;

        if (foutPath != null) {
            fout = new File(foutPath);
        } else {
            fout = File.createTempFile("based_file_", ".txt");
            fout.deleteOnExit();
        }

        try (
                final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fout), buffsize);
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fin), buffsize * 4 / 3)) {
            byte[] buffer = new byte[3];
            int countWords = bis.read(buffer, 0, 3);

            for (; countWords != -1; countWords = bis.read(buffer, 0, 3)) {
                bos.write((byte) toBase64[((buffer[0] & 0b11111100) >> 2)]);
                bos.write((byte) toBase64[(((buffer[0] & 0b00000011) << 4)) | ((buffer[1] & 0b11110000) >>> 4)]);

                if (countWords == 1) {
                    bos.write('=');
                    bos.write('=');
                } else if (countWords == 2) {
                    bos.write((byte) toBase64[(((buffer[1] & 0b00001111) << 2) | ((buffer[2] & 0b11000000) >>> 6))]);
                    bos.write('=');
                } else {
                    bos.write((byte) toBase64[(((buffer[1] & 0b00001111) << 2) | ((buffer[2] & 0b11000000) >>> 6))]);
                    bos.write((byte) toBase64[buffer[2] & 0b00111111]);
                }
                //Очищаем выделяемый buffer нулями
                buffer[0] = 0;
                buffer[1] = 0;
                buffer[2] = 0;
            }
            bos.flush();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        return fout;
    }

    private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static void main(String[] args) throws Exception {
        final FileEncoder encoder = new TaskImplementation();
        // NOTE: open http://localhost:9000/ in your web browser
        (new Bootstrapper(args, encoder))
                .bootstrap("", new InetSocketAddress("127.0.0.1", 9000));
    }

}
