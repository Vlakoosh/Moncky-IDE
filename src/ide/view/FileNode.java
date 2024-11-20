package ide.view;

public class FileNode {
    private final String fileName;
    private final String filePath;

    public FileNode(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return fileName; // Display only the file name in the TreeView
    }
}
