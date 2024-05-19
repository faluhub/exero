package me.falu.exero;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Test {
    public static void main(String[] args) {
        new Test().beautify();
    }

    public void beautify() {
        try {
            InputStream stream = Test.class.getClassLoader().getResourceAsStream("beautify.exe");
            if (stream != null) {
                byte[] bytes = stream.readAllBytes();
                Path path = FileUtils.getTempDirectory().toPath().resolve("beautify.exe");
                FileUtils.writeByteArrayToFile(path.toFile(), bytes);
                Process proc = Runtime.getRuntime().exec(new String[] { path.toString(), "js", "" });
                proc.waitFor();
                String result = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                proc.destroy();
                stream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
