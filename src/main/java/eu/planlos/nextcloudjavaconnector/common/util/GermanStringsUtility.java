package eu.planlos.nextcloudjavaconnector.common.util;

public class GermanStringsUtility {

    public static String handleGermanChars(String text) {
        return text
                .replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("Ä", "Ae")
                .replaceAll("Ö", "Oe")
                .replaceAll("Ü", "Ue")
                .replaceAll("ß", "ss");
    }
}