package org.lpt.util;

import java.util.ArrayList;
import java.util.List;

public class History<TYPE> {
    private int historyIndex = -1;
    private final List<TYPE> commandHistory = new ArrayList<>();
    private TYPE currentCommand;

    public void navigateHistory(int index) {
        if (commandHistory.isEmpty()) return;

        if (historyIndex == -1) {
            historyIndex = commandHistory.size() - 1;
        } else {
            historyIndex += index;
            if (historyIndex < 0 || historyIndex >= commandHistory.size()) {
                historyIndex = Math.max(0, Math.min(historyIndex, commandHistory.size() - 1));
                return;
            }
        }

        currentCommand = commandHistory.get(historyIndex);
    }

    public TYPE getCommand() {
        return currentCommand;
    }

    public void addCommand(TYPE command) {
        commandHistory.add(command);
        historyIndex = -1;
    }
}