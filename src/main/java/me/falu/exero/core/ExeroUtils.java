package me.falu.exero.core;

import com.google.javascript.jscomp.parsing.parser.Parser;
import com.google.javascript.jscomp.parsing.parser.SourceFile;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.ErrorReporter;
import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import me.falu.exero.Test;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ExeroUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    public static String cleanFilename(String filename) {
        String cleanedFilename = filename.replaceAll("[^a-zA-Z0-9]", "_");
        cleanedFilename = cleanedFilename.replaceAll("^_+|_+$", "");
        int maxLength = 255;
        cleanedFilename = cleanedFilename.substring(0, Math.min(cleanedFilename.length(), maxLength));
        return cleanedFilename;
    }

    public static ProgramTree createJsParser(String name, String content) {
        return new Parser(new Parser.Config(), new ErrorReporter() {
            @Override
            protected void reportError(SourcePosition location, String message) {
                LOGGER.error("{}: {}", location.toString(), message);
            }

            @Override
            protected void reportWarning(SourcePosition location, String message) {
                LOGGER.error("{}: {}", location.toString(), message);
            }
        }, new SourceFile(name, content)).parseProgram();
    }

    public static String beautify(Path file, String type) {
        try {
            InputStream stream = Test.class.getClassLoader().getResourceAsStream("beautify.exe");
            if (stream != null) {
                byte[] bytes = stream.readAllBytes();
                stream.close();
                Path path = FileUtils.getTempDirectory().toPath().resolve("beautify.exe");
                FileUtils.writeByteArrayToFile(path.toFile(), bytes);
                Process proc = Runtime.getRuntime().exec(new String[] { path.toString(), type, file.toString() });
                InputStream stream1 = proc.getInputStream();
                byte[] bytes1 = stream1.readAllBytes();
                stream1.close();
                return new String(bytes1, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.error("Error while beautifying {}", file.toString(), e);
        }
        return null;
    }
}
