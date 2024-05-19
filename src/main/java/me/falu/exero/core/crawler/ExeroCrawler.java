package me.falu.exero.core.crawler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import lombok.NonNull;
import me.falu.exero.ExeroHttp;
import me.falu.exero.core.ExeroProject;
import me.falu.exero.core.ExeroUtils;
import me.falu.exero.gui.LoadingScreenWindow;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ExeroCrawler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ExeroProject project;
    private final URL domain;

    public ExeroCrawler(ExeroProject project) {
        this.project = project;
        this.domain = project.getDomain();
    }

    public List<NextURL> getPages() {
        List<NextURL> pages = new ArrayList<>();
        String body = ExeroHttp.get(this.domain);
        if (body != null) {
            Document doc = Jsoup.parse(body);
            for (Element element : doc.getElementsByTag("script")) {
                if (element.hasAttr("src")) {
                    String src = element.attribute("src").getValue();
                    if (src.endsWith("_buildManifest.js")) {
                        String manifest = ExeroHttp.get(resolveUrl(this.domain, src));
                        if (manifest != null) {
                            ProgramTree tree = ExeroUtils.createJsParser("_buildManifest.js", manifest);
                            CommaExpressionTree root = tree.sourceElements.getFirst().asExpressionStatement().expression.asCommaExpression();
                            BinaryOperatorTree equals = root.expressions.getFirst().asBinaryOperator();
                            CallExpressionTree call = equals.right.asCallExpression();
                            FunctionDeclarationTree function = call.operand.asFunctionDeclaration();
                            Map<String, String> arguments = new HashMap<>();
                            for (int i = 0; i < function.formalParameterList.parameters.size(); i++) {
                                IdentifierExpressionTree parameter = function.formalParameterList.parameters.get(i).asIdentifierExpression();
                                arguments.put(parameter.identifierToken.value, call.arguments.arguments.get(i).asLiteralExpression().literalToken.toString());
                            }
                            BlockTree functionBody = function.functionBody.asBlock();
                            ObjectLiteralExpressionTree ret = functionBody.statements.getFirst().asReturnStatement().expression.asObjectLiteralExpression();
                            for (ParseTree v : ret.propertyNameAndValues) {
                                PropertyNameAssignmentTree property = v.asPropertyNameAssignment();
                                if (!property.name.toString().startsWith("\"/")) {
                                    continue;
                                }
                                URL url = resolveUrl(this.domain, replaceQuotes(property.name.toString()));
                                NextURL page = new NextURL(this.domain, url, property.value.asArrayLiteralExpression(), arguments);
                                pages.add(page);
                            }
                        }
                    }
                }
            }
        }
        int amount = 0;
        for (NextURL page : pages) {
            amount += page.getSourceFiles().size();
            amount += page.getStyleFiles().size();
        }
        LOGGER.info("Parsed {} source files.", amount);
        return pages;
    }

    public void downloadSources(LoadingScreenWindow loadingScreen) {
        for (NextURL page : this.getPages()) {
            Path jsFolder = this.project.getProjectPath().resolve("js");
            Path cssFolder = this.project.getProjectPath().resolve("css");
            if (jsFolder.toFile().mkdirs() && cssFolder.toFile().mkdirs()) {
                LOGGER.info("Made sources folders.");
            }
            for (URL url : page.getSourceFiles()) {
                this.downloadFile(url, jsFolder, "js");
                String log = "Downloaded js file " + url.getFile() + ".";
                loadingScreen.setStatusText(log);
                LOGGER.info(log);
            }
            for (URL url : page.getStyleFiles()) {
                this.downloadFile(url, cssFolder, "css");
                String log = "Downloaded css file " + url.getFile() + ".";
                loadingScreen.setStatusText(log);
                LOGGER.info(log);
            }
        }
    }

    private void downloadFile(URL url, Path folder, String beautifyType) {
        try {
            InputStream stream = url.openStream();
            File file = folder.resolve(Arrays.stream(url.getFile().split("/")).toList().getLast()).toFile();
            FileUtils.writeByteArrayToFile(file, stream.readAllBytes());
            String content = ExeroUtils.beautify(file.toPath(), beautifyType);
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            stream.close();
        } catch (IOException e) {
            LOGGER.error("Couldn't open stream for source file", e);
        }
    }

    public static String replaceQuotes(String value) {
        return value.replaceAll("\"", "");
    }

    @NonNull
    @SuppressWarnings("deprecation")
    public static URL resolveUrl(URL base, String add) {
        try {
            if (!add.startsWith("/")) {
                add = "/" + add;
            }
            return new URL(base.toString() + add.replace("\"", ""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
