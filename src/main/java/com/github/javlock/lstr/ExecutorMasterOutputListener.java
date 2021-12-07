package com.github.javlock.lstr;

import java.io.IOException;

public interface ExecutorMasterOutputListener {

	void appendInput(String line);

	void appendOutput(String line) throws IOException;

	void startedProcess(Long pid);

}
