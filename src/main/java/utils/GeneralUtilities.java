/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 * Provides general utilities including frequently used string manipulation
 * functionality.
 *
 * @author Haitham Seada
 */
public class GeneralUtilities {

    public static String replaceBlanksWithSingleSpace(String text) {
        StringBuilder sb = new StringBuilder();
        boolean oneSpaceConsumed = false;
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                sb.append(text.charAt(i));
                oneSpaceConsumed = false;
            } else {
                if (!oneSpaceConsumed) {
                    sb.append(" ");
                    oneSpaceConsumed = true;
                }
            }
        }
        return sb.toString();
    }

    public static String replaceDashesWithUnderscores(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '-') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
