import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class InOut {

  public File projectFile(String path) {
    return new File(System.getProperty("user.dir", "."), path);
  }

  public File chooseFile(String title, File initialDirectory, String description, String... extensions) throws ExecutionException, InterruptedException {
    JFileChooser fc = new JFileChooser(initialDirectory);
    fc.setDialogTitle(title + " - " + description);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (extensions.length > 0) {
      fc.setAcceptAllFileFilterUsed(false);
      FileNameExtensionFilter filter = new FileNameExtensionFilter(String.join(",", extensions) +" files", extensions);
      fc.addChoosableFileFilter(filter);
    }
    int resultVal = fc.showOpenDialog(null);
    if (resultVal == JFileChooser.APPROVE_OPTION) {
      return fc.getSelectedFile();
    }
    return null;
  }
}
