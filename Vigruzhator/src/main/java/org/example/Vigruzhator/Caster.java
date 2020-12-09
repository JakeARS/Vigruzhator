package org.example.Vigruzhator;

public class Caster {

    public static String castStackTraceElementToString(StackTraceElement[] stackTraceElements) {
        String readyString = new String();

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            readyString = readyString + stackTraceElement.toString() + System.lineSeparator();
        }

        return readyString;
    }
}
