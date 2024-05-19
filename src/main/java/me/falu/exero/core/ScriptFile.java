package me.falu.exero.core;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import lombok.Getter;
import me.falu.exero.Main;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ScriptFile {
    private final File file;
    private final String scriptId;
    private final Map<String, String> functions;

    public ScriptFile(File file) throws IOException {
        this.file = file;
        this.functions = new HashMap<>();

        String content = FileUtils.readFileToString(this.file, StandardCharsets.UTF_8);
        ProgramTree tree = ExeroUtils.createJsParser(this.file.getName(), content);
        CallExpressionTree call = null;
        for (ParseTree tree1 : tree.sourceElements) {
            try {
                call = tree1.asExpressionStatement().expression.asCallExpression();
            } catch (ClassCastException ignored) {}
        }
        if (call == null) {
            this.scriptId = "Unknown Script ID";
            return;
        }
        ArrayLiteralExpressionTree arg = call.arguments.arguments.getFirst().asArrayLiteralExpression();
        this.scriptId = arg.elements.getFirst().asArrayLiteralExpression().elements.getFirst().asLiteralExpression().literalToken.toString();
        ObjectLiteralExpressionTree idToFunc = arg.elements.get(1).asObjectLiteralExpression();
        for (ParseTree element : idToFunc.propertyNameAndValues) {
            PropertyNameAssignmentTree entry = element.asPropertyNameAssignment();
            String key = entry.name.toString();
            SourceRange range = entry.value.asFunctionDeclaration().functionBody.location;
            this.functions.put(key, content.substring(range.start.offset, range.end.offset).replaceAll("\n {8}", "\n"));
        }
    }
}
