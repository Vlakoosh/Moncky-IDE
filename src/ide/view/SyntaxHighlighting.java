package ide.view;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighting {
    Pattern pattern = Pattern.compile("(?<KEYWORD>\\b(halt|li|ld|st|jp)\\b)|" +
            "(?<ALU>\\b(nop|or|and|xor|add|sub|shl|shr|ashr|not|neg)\\b)|" +
            "(?<REGISTER>\\br[0-9]+\\b)|" +
            "(?<LABEL>:[A-Za-z_][A-Za-z0-9_]*)(?=\\s|$)|" +
            "(?<COMMENT>;.*)");


    void setupSyntaxHighlighting(CodeArea codeArea) {
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // Only react if text changes
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = pattern.matcher(text);

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastEnd = 0;

        while (matcher.find()) {
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
            if (matcher.group("KEYWORD") != null) {
                spansBuilder.add(Collections.singleton("keyword"), matcher.end() - matcher.start());
            } else if (matcher.group("ALU") != null) {
                spansBuilder.add(Collections.singleton("alu"), matcher.end() - matcher.start());
            } else if (matcher.group("REGISTER") != null) {
                spansBuilder.add(Collections.singleton("register"), matcher.end() - matcher.start());
            } else if (matcher.group("LABEL") != null) {
                spansBuilder.add(Collections.singleton("label"), matcher.end() - matcher.start());
            } else if (matcher.group("COMMENT") != null) {
                spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
            }
            lastEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }
}
