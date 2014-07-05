package org.nuc.revedere.review;

import java.io.Serializable;

public class ReviewFile implements Serializable {
	private static final long serialVersionUID = -8992154190093388403L;
	private final String fileContent;
	private final String fileRelativePath;

	public ReviewFile(String fileRelativePath, String fileContent) {
		this.fileRelativePath = fileRelativePath;
		this.fileContent = fileContent;
	}

	public String getFileContent() {
		return fileContent;
	}

	public String getFileRelativePath() {
		return fileRelativePath;
	}
}
