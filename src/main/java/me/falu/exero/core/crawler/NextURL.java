package me.falu.exero.core.crawler;

import com.google.javascript.jscomp.parsing.parser.trees.ArrayLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
public class NextURL {
    private static final Logger LOGGER = LogManager.getLogger();
    private final URL uri;
    private final List<URL> sourceFiles;
    private final List<URL> styleFiles;

    public NextURL(URL domain, URL uri, ArrayLiteralExpressionTree tree, Map<String, String> arguments) {
        this.uri = uri;
        this.sourceFiles = new ArrayList<>();
        this.styleFiles = new ArrayList<>();
        for (ParseTree element : tree.elements) {
            String value = "";
            if (element.type.equals(ParseTreeType.LITERAL_EXPRESSION)) {
                value = element.asLiteralExpression().literalToken.toString();
            } else if (element.type.equals(ParseTreeType.IDENTIFIER_EXPRESSION)) {
                value = arguments.get(element.asIdentifierExpression().identifierToken.toString());
            }
            URL url = ExeroCrawler.resolveUrl(domain, "_next/" + ExeroCrawler.replaceQuotes(value));
            if (value.endsWith(".js\"")) {
                this.sourceFiles.add(url);
            } else if (value.endsWith(".css\"")) {
                this.styleFiles.add(url);
            }
        }
    }
}
