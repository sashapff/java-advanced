package ru.ifmo.rain.ivanova.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class TextHandler {
    private final BufferedReader reader;
    private final String text;
    private final Locale inputLocale;

    private TextItem sentences;
    private TextItem strings;
    private TextItem words;
    private TextItem numbers;
    private TextItem money;
    private TextItem dates;

    TextHandler(BufferedReader reader, Locale inputLocale) {
        this.reader = reader;
        this.inputLocale = inputLocale;
        text = fileToString();
    }

    private String fileToString() {
        try {
            String line = reader.readLine();
            final StringBuilder stringBuilder = new StringBuilder();
            while (line != null) {
                stringBuilder.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            System.out.println("Can't read");
        }
        return null;
    }

    TextItem getSentences() {
        return sentences;
    }

    TextItem getStrings() {
        return strings;
    }

    TextItem getWords() {
        return words;
    }

    TextItem getNumbers() {
        return numbers;
    }

    TextItem getMoney() {
        return money;
    }

    TextItem getDates() {
        return dates;
    }

    private <T> void put(final Map<T, Integer> map, final T key) {
        map.putIfAbsent(key, 0);
        map.put(key, map.get(key) + 1);
    }

    private Map<String, Integer> getIteratorData(final BreakIterator iterator) {
        Map<String, Integer> map = new HashMap<>();
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            String key = text.substring(start, end).trim();
            if (!key.equals("")) {
                put(map, key);
            }
        }
        return map;
    }

    private Map<String, Integer> getStringsData() {
        Map<String, Integer> map = new HashMap<>();
        String[] lines = text.split(System.lineSeparator());
        for (String line : lines) {
            if (!line.equals("")) {
                put(map, line);
            }
        }
        return map;
    }

    private Map<Number, Integer> getNumbersData(final NumberFormat numberFormat, final boolean flag) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, inputLocale);
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(inputLocale);
        Map<Number, Integer> map = new HashMap<>();
        final BreakIterator iterator = BreakIterator.getLineInstance(inputLocale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            String key = text.substring(start, end).trim();
            try {
                dateFormat.parse(key);
            } catch (ParseException ignored) {
                try {
                    currencyFormat.parse(key);
                    if (flag) {
                        put(map, numberFormat.parse(key));
                    }
                } catch (ParseException ignored1) {
                    try {
                        put(map, numberFormat.parse(key));
                    } catch (ParseException ignored2) {
                    }
                }
            }
        }
        return map;
    }

    private Map<Date, Integer> getDatesData(final DateFormat dateFormat) {
        Map<Date, Integer> map = new HashMap<>();
        final BreakIterator iterator = BreakIterator.getLineInstance(inputLocale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            try {
                put(map, dateFormat.parse(text.substring(start, end).trim()));
            } catch (ParseException ignored) {
            }
        }
        return map;
    }

    class Pair {
        Integer first, second;

        Pair(Integer first, Integer second) {
            this.first = first;
            this.second = second;
        }
    }

    private void handleCommonItem(final TextItem item, final String key, final int value,
                                  final Pair number) {
        item.setNumber(item.getNumber() + value);
        int length = key.length();
        if (value > number.second) {
            number.second = value;
            item.setMaxNumberValue(key);
        }
        if (value < number.first) {
            number.first = value;
            item.setMinNumberValue(key);
        }
        if (length > item.getMaxLength()) {
            item.setMaxLength(length);
            item.setMaxLengthValue(key);
        }
        if (length < item.getMinLength()) {
            item.setMinLength(length);
            item.setMinLengthValue(key);
        }
    }

    private void checkFields(final TextItem item) {
        if (item.getMinLength() == Integer.MAX_VALUE) {
            item.setMinLength(0);
        }
    }

    private Pair makePair() {
        return new Pair(Integer.MAX_VALUE, 0);
    }

    private TextItem handleLengthItem(final Map<String, Integer> map) {
        TextItem item = new TextItem(0, map.size(), "", "",
                Integer.MAX_VALUE, 0, "", "", 0, "");
        Pair number = makePair();
        long summaryLength = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            int value = entry.getValue();
            String key = entry.getKey();
            handleCommonItem(item, key, value, number);
            summaryLength += key.length() * value;
        }
        checkFields(item);
        final int num = item.getNumber();
        item.setMedianLength((int) summaryLength / (num != 0 ? num : 1));
        return item;
    }

    private TextItem handleNumberItem(final Map<Number, Integer> map, final NumberFormat numberFormat) {
        TextItem item = new TextItem(0, map.size(), "", "",
                Integer.MAX_VALUE, 0, "", "", 0, "");
        Pair number = makePair();
        double summaryValue = 0;
        for (Map.Entry<Number, Integer> entry : map.entrySet()) {
            int value = entry.getValue();
            handleCommonItem(item, numberFormat.format(entry.getKey()), value, number);
            summaryValue += entry.getKey().doubleValue() * value;
        }
        if (item.getMinLength() == Integer.MAX_VALUE) {
            item.setMinLength(0);
        }
        checkFields(item);
        final int num = item.getNumber();
        item.setMedianValue(numberFormat.format(summaryValue / (num != 0 ? num : 1)));
        return item;
    }

    private TextItem handleDateItem(final Map<Date, Integer> map, final DateFormat dateFormat) {
        TextItem item = new TextItem(0, map.size(), "", "",
                Integer.MAX_VALUE, 0, "", "", 0, "");
        Pair number = makePair();
        long summaryValue = 0;
        for (Map.Entry<Date, Integer> entry : map.entrySet()) {
            int value = entry.getValue();
            handleCommonItem(item, dateFormat.format(entry.getKey()), value, number);
            summaryValue += entry.getKey().getTime() * value;
        }
        if (item.getMinLength() == Integer.MAX_VALUE) {
            item.setMinLength(0);
        }
        checkFields(item);
        final int num = item.getNumber();
        item.setMedianValue(dateFormat.format(summaryValue / (num != 0 ? num : 1)));
        return item;
    }

    private void handleSentences() {
        sentences = handleLengthItem(getIteratorData(BreakIterator.getSentenceInstance(inputLocale)));
    }

    private void handleStrings() {
        strings = handleLengthItem(getStringsData());
    }

    private void handleWords() {
        words = handleLengthItem(getIteratorData(BreakIterator.getLineInstance(inputLocale)));
    }

    private void handleNumbers() {
        numbers = handleNumberItem(getNumbersData(NumberFormat.getNumberInstance(inputLocale), false),
                NumberFormat.getNumberInstance(inputLocale));
    }

    private void handleMoney() {
        money = handleNumberItem(getNumbersData(NumberFormat.getCurrencyInstance(inputLocale), true),
                NumberFormat.getCurrencyInstance(inputLocale));
    }

    private void handleDates() {
        dates = handleDateItem(getDatesData(DateFormat.getDateInstance(DateFormat.SHORT, inputLocale)),
                DateFormat.getDateInstance(DateFormat.SHORT, inputLocale));
    }

    TextHandler handle() {
        handleSentences();
        handleStrings();
        handleWords();
        handleNumbers();
        handleMoney();
        handleDates();
        return this;
    }
}
