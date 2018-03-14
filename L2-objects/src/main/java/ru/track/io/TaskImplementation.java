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
        int countWords;
        final File fout;
        if (foutPath != null) {
            fout = new File(foutPath);
        } else {
            fout = File.createTempFile("based_file_", ".txt");
            fout.deleteOnExit();
        }
        FileInputStream in = new FileInputStream(fin);
        byte[] buffer = new byte[buffsize];
        byte[] result = new byte[buffsize* 4/3];
        // считаем файл в буфер
        countWords = in.read(buffer, 0, buffsize);
        int lastNum = 0;
        for (int i = 0, j = 0; i < countWords ;)
        {
            result[j] =  (byte) toBase64[((buffer[i] & 0b11111100) >> 2)];
            result[j+1] = (byte)toBase64[(((buffer[i] & 0b00000011) << 4)) | ((buffer[i+1] & 0b11110000) >>> 4)];
            result[j+2] = (byte) toBase64[(((buffer[i+1] & 0b00001111) << 2 )  | ((buffer[i+2] & 0b11000000) >>> 6))];
            result[j+3] = (byte) toBase64[buffer[i+2] & 0b00111111];
            i +=3;
            j +=4;
            lastNum = j;
        }
        //если количество считываемых байт не делится нацело на 3, нужно заполнить '='
        if (countWords % 3 == 1)
        {
            lastNum -= 4;
            countWords += 2;
            result [lastNum+2] = '=';
            result [lastNum+3] = '=';

        }
        if (countWords % 3 == 2)
        {
            lastNum -= 4;
            countWords++;
            result [lastNum+3] = '=';
        }

        // запись в файл
        try(FileOutputStream out = new FileOutputStream(fout);
            BufferedOutputStream bos = new BufferedOutputStream(out))
        {
            bos.write(result, 0, countWords * 4 / 3);
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
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
