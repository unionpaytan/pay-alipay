package com.bootpay.common.utils;

public class AsciiUtil {
    /**
     * 字符串转换为Ascii
     * @param value
     * @return
     */
    public static String stringToAscii(String s)
    {
        String str = "";
        if ((s == null) || (s.trim().equals("")))
            return str;
        for (int i = 0; i < s.length(); i++) {
            byte[] bytes = String.valueOf(s.charAt(i)).getBytes();
            String s4;
            if (bytes.length == 1) {
                s4 = String.valueOf(s.charAt(i));
            } else {
                int ch = s.charAt(i);
                s4 = "\\u" + Integer.toHexString(ch);
            }
            str = str + s4;
        }
        return str;

    }
    /**
     * Ascii转换为字符串
     * @param theString
     * @return
     */
    public static String asciiToString(String theString)
    {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed      encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }

        }
        return outBuffer.toString();
    }
}
