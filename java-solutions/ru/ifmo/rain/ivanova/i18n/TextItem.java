package ru.ifmo.rain.ivanova.i18n;

class TextItem {
    private int number;
    private int differentNumber;
    private String minNumberValue;
    private String maxNumberValue;
    private int minLength;
    private int maxLength;
    private String minLengthValue;
    private String maxLengthValue;
    private int medianLength;
    private String medianValue;

    TextItem(final int number, final int differentNumber, final String minNumberValue,
                    final String maxNumberValue, final int minLength, final int maxLength,
                    final String minLengthValue, final String maxLengthValue,
                    final int medianLength, final String medianValue) {
        this.number = number;
        this.differentNumber = differentNumber;
        this.minNumberValue = minNumberValue;
        this.maxNumberValue = maxNumberValue;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minLengthValue = minLengthValue;
        this.maxLengthValue = maxLengthValue;
        this.medianLength = medianLength;
        this.medianValue = medianValue;
    }

    int getNumber() {
        return number;
    }

    int getDifferentNumber() {
        return differentNumber;
    }

    String getMinNumberValue() {
        return minNumberValue;
    }

    String getMaxNumberValue() {
        return maxNumberValue;
    }

    int getMinLength() {
        return minLength;
    }

    int getMaxLength() {
        return maxLength;
    }

    String getMinLengthValue() {
        return minLengthValue;
    }

    String getMaxLengthValue() {
        return maxLengthValue;
    }

    int getMedianLength() {
        return medianLength;
    }

    String getMedianValue() {
        return medianValue;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setDifferentNumber(int differentNumber) {
        this.differentNumber = differentNumber;
    }

    public void setMinNumberValue(String minNumberValue) {
        this.minNumberValue = minNumberValue;
    }

    public void setMaxNumberValue(String maxNumberValue) {
        this.maxNumberValue = maxNumberValue;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setMinLengthValue(String minLengthValue) {
        this.minLengthValue = minLengthValue;
    }

    public void setMaxLengthValue(String maxLengthValue) {
        this.maxLengthValue = maxLengthValue;
    }

    public void setMedianLength(int medianLength) {
        this.medianLength = medianLength;
    }

    public void setMedianValue(String medianValue) {
        this.medianValue = medianValue;
    }
}
