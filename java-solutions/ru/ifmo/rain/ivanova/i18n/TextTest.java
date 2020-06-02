package ru.ifmo.rain.ivanova.i18n;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class TextTest {

    private void checkItem(final TextItem item, final int number, final int differentNumber,
                           final int minLength, final int maxLength, final int medianLength,
                           final String medianValue, final String minNumberValue,
                           final String maxNumberValue, final String minLengthValue, final String maxLengthValue) {
        assertEquals(number, item.getNumber());
        assertEquals(differentNumber, item.getDifferentNumber());
        assertEquals(minLength, item.getMinLength());
        assertEquals(maxLength, item.getMaxLength());
        assertEquals(medianLength, item.getMedianLength());
        assertEquals(medianValue, item.getMedianValue());
        assertEquals(minNumberValue, item.getMinNumberValue());
        assertEquals(maxNumberValue, item.getMaxNumberValue());
        assertEquals(minLengthValue, item.getMinLengthValue());
        assertEquals(maxLengthValue, item.getMaxLengthValue());
    }

    private Locale ru = TextStatistics.getLocale("ru");
    private Locale en = TextStatistics.getLocale("en");
    private Locale es = TextStatistics.getLocale("es");

    private TextHandler before(final String fileName, final Locale locale) {
        try (final BufferedReader reader = Files.newBufferedReader(TextStatistics.getPath(fileName), StandardCharsets.UTF_8)) {
            return new TextHandler(reader, locale).handle();
        } catch (IOException e) {
            return null;
        }
    }

    @Test
    public void test00_ru() {
        final TextHandler handler = before("ru_input.txt", ru);
        if (handler == null) {
            return;
        }
        checkItem(handler.getSentences(), 37, 16, 4, 56, 20,
                "",
                "Лол 5 3,00 ¤ 06.06.2006.",
                "Ура!",
                "Ура!",
                "Получается, что одной сестре 12 лет, а другим двум по 8."
        );
        checkItem(handler.getStrings(), 19, 7, 4, 136, 40,
                "",
                "Лол 5 3,00 ¤ 06.06.2006.",
                "Ура!",
                "Ура!",
                "Моя дата рождения 28.02.2000 . А моя сестра родилась 24.11.2007 . А другая 23.12.2011 . А еще 23.12.2011 родилась моя двоюродная сестра."
        );
        checkItem(handler.getWords(), 136, 56, 1, 11, 4,
                "",
                "Лол",
                "А",
                "А",
                "06.06.2006."
        );
        checkItem(handler.getNumbers(), 7, 4, 1, 2, 0,
                "12,143",
                "5",
                "20",
                "5",
                "20");
        checkItem(handler.getMoney(), 9, 4, 6, 11, 0,
                "8 444,78 ¤",
                "3,00 ¤",
                "400,00 ¤",
                "3,00 ¤",
                "35 600,00 ¤");
        checkItem(handler.getDates(), 9, 4, 10, 10, 0,
                "29.10.2007",
                "06.06.2006",
                "23.12.2011",
                "28.02.2000",
                "28.02.2000"
        );
    }

    @Test
    public void test01_en() {
        final TextHandler handler = before("en_input.txt", en);
        if (handler == null) {
            return;
        }
        checkItem(handler.getSentences(), 69, 17, 4, 111, 14,
                "",
                "Sasha 20 years old 1/10/21 ¤3.15 tss.",
                "Top.",
                "Top.",
                "Top top top top top 100500 top top 5/31/20 top top top top top top top top top top top top top top top top top."
        );
        checkItem(handler.getStrings(), 27, 12, 21, 111, 39,
                "",
                "Sasha 20 years old 1/10/21 ¤3.15 tss.",
                "Mee too. I don't like Java 500.",
                "O, 2.28 really. Cool!",
                "Top top top top top 100500 top top 5/31/20 top top top top top top top top top top top top top top top top top."
        );
        checkItem(handler.getWords(), 207, 45, 1, 15, 4,
                "",
                "Sasha",
                "top",
                "I",
                "Procrastination"
        );
        checkItem(handler.getNumbers(), 17, 6, 2, 7, 0,
                "12,066.739",
                "20",
                "500",
                "20",
                "100,500"
        );
        checkItem(handler.getMoney(), 5, 3, 5, 10, 0,
                "¤4,318.55",
                "¤3.15",
                "¤10,789.80",
                "¤5.00",
                "¤10,789.80"
        );
        checkItem(handler.getDates(), 7, 3, 7, 7, 0,
                "9/18/14",
                "1/10/21",
                "5/31/20",
                "5/31/20",
                "5/31/20"
        );
    }

    @Test
    public void test02_es() {
        final TextHandler handler = before("es_input.txt", es);
        if (handler == null) {
            return;
        }
        checkItem(handler.getSentences(), 7, 4, 14, 21, 17,
                "",
                "Sasha pff 5 14/02/20.",
                "Mi nombre es Sasha .",
                "Tengo 20 años.",
                "Sasha pff 5 14/02/20."
        );
        checkItem(handler.getStrings(), 1, 1, 129, 129, 129,
                "",
                "Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Sasha pff 5 14/02/20.",
                "Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Sasha pff 5 14/02/20.",
                "Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Sasha pff 5 14/02/20.",
                "Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Mi nombre es Sasha . Tengo 20 años. Nací el 28/02/00. Sasha pff 5 14/02/20."
        );
        checkItem(handler.getWords(), 26, 14, 1, 9, 4,
                "",
                "pff",
                "Sasha",
                ".",
                "28/02/00."
        );
        checkItem(handler.getNumbers(), 3, 2, 1, 2, 0,
                "15",
                "5",
                "20",
                "5",
                "20"
        );
        checkItem(handler.getMoney(), 0, 0, 0, 0, 0,
                "0,00 ¤",
                "",
                "",
                "",
                ""
        );
        checkItem(handler.getDates(), 3, 2, 7, 7, 0,
                "24/10/06",
                "14/2/20",
                "28/2/00",
                "28/2/00",
                "28/2/00"
        );
    }
}
