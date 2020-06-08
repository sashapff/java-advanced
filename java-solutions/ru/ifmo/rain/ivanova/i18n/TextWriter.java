package ru.ifmo.rain.ivanova.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

class TextWriter {
    private final BufferedWriter writer;
    private final ResourceBundle outputBundle;
    private final TextHandler textHandler;

    TextWriter(final BufferedWriter writer, final ResourceBundle outputBundle,
               final TextHandler textHandler) {
        this.writer = writer;
        this.outputBundle = outputBundle;
        this.textHandler = textHandler;
    }

    private void writeHead(final String inputFile) throws IOException {
        writer.write("<h1>");
        writer.write(MessageFormat.format(
                "{0} {1}: {2}",
                outputBundle.getString("Analyzed"),
                outputBundle.getString("file"),
                inputFile
        ));
        writer.write("</h1>\n");
    }

    private void before() throws IOException {
        writer.write("<h2>");
    }

    private void after() throws IOException {
        writer.write("</h2>\n");
    }

    private String getString(final String s) {
        return outputBundle.getString(s);
    }

    private void writeItemHead(final TextItem item, final String ofOne) throws IOException {
        writer.write("<p>");
        writer.write(MessageFormat.format(
                "{0} {1}: {2, number}",
                getString("Number"),
                ofOne,
                item.getNumber()
        ));
        writer.write("</p>");
    }

    private void writeSummary() throws IOException {
        before();
        writer.write(MessageFormat.format(
                "{0} {1}",
                getString("Summary"),
                getString("statistics")
        ));
        after();
        writeItemHead(textHandler.getSentences(), getString("ofSentences"));
        writeItemHead(textHandler.getStrings(), getString("ofStrings"));
        writeItemHead(textHandler.getWords(), getString("ofWords"));
        writeItemHead(textHandler.getNumbers(), getString("ofNumbers"));
        writeItemHead(textHandler.getMoney(), getString("ofMoney"));
        writeItemHead(textHandler.getDates(), getString("ofDates"));
    }

    private void writeCommonItem(final TextItem item, final String ofMany, final String one,
                                 final String ofOne) throws IOException {
        before();
        writer.write(MessageFormat.format(
                "{0} {1}",
                getString("Statistics"),
                ofMany
        ));
        after();
        writer.write(MessageFormat.format(
                "<p>{0} {1}: {2, number} ({3}: {4, number})</p>\n" +
                        "<p>{5}{6}: {7}</p>\n" +
                        "<p>{8}{9}: {10}</p>\n" +
                        "<p>{11}{12} {13}: {14, number} ({15})</p>\n" +
                        "<p>{16}{17} {18}: {19, number} ({20})</p>\n",

                getString("Number"),
                ofMany,
                item.getNumber(),
                getString("unique"),
                item.getDifferentNumber(),

                getString("Minimum"),
                one,
                item.getMinNumberValue(),
                getString("Maximum"),
                one,
                item.getMaxNumberValue(),

                getString("Minimum"),
                getString("prLength"),
                ofOne,
                item.getMinLength(),
                item.getMinLengthValue(),

                getString("Maximum"),
                getString("prLength"),
                ofOne,
                item.getMaxLength(),
                item.getMaxLengthValue()
        ));
    }

    private void writeLengthItem(final TextItem item, final String ofMany,
                                 final String prOne, final String ofOne) throws IOException {
        writeCommonItem(item, ofMany, prOne, ofOne);
        writer.write(MessageFormat.format(
                "<p>{0} {1}: {2, number}</p>\n",
                getString("medianLength"),
                ofOne,
                item.getMedianLength()
        ));
    }

    private void writeValueItem(final TextItem item, final String ofMany,
                                final String prOne, final String ofOne) throws IOException {
        writeCommonItem(item, ofMany, prOne, ofOne);
        writer.write(MessageFormat.format(
                "<p>{0} {1}: {2}</p>\n",
                getString("medianValue"),
                ofOne,
                item.getMedianValue()
        ));
    }

    private void writeSentences() throws IOException {
        writeLengthItem(textHandler.getSentences(), getString("ofSentences"),
                getString("prSentence"), getString("ofSentence"));
    }

    private void writeStrings() throws IOException {
        writeLengthItem(textHandler.getStrings(), getString("ofStrings"),
                getString("prString"), getString("ofString"));
    }

    private void writeWords() throws IOException {
        writeLengthItem(textHandler.getWords(), getString("ofWords"),
                getString("prWord"), getString("ofWord"));
    }

    private void writeNumbers() throws IOException {
        writeValueItem(textHandler.getNumbers(), getString("ofNumbers"),
                getString("prNumber"), getString("ofNumber"));
    }

    private void writeMoney() throws IOException {
        writeValueItem(textHandler.getMoney(), getString("ofMoney"),
                getString("prMoney"), getString("ofMoney"));
    }

    private void writeDates() throws IOException {
        writeValueItem(textHandler.getDates(), getString("ofDates"),
                getString("prDate"), getString("ofDate"));
    }

    void write(final String inputFile) {
        try {
            writer.write("<meta charset=\"UTF-8\" />\n");
            writer.write("<html>\n");
            writeHead(inputFile);
            writeSummary();
            writeSentences();
            writeStrings();
            writeWords();
            writeNumbers();
            writeMoney();
            writeDates();
            writer.write("</html>\n");
        } catch (IOException e) {
            System.out.println("Can't write");
        }
    }
}
