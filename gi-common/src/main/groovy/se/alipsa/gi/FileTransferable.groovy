package se.alipsa.gi

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class FileTransferable implements Transferable {
  private List listOfFiles;

  FileTransferable(List listOfFiles) {
    this.listOfFiles = listOfFiles;
  }

  @Override
  DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{DataFlavor.javaFileListFlavor}
  }

  @Override
  boolean isDataFlavorSupported(DataFlavor flavor) {
    return DataFlavor.javaFileListFlavor == flavor
  }

  @Override
  Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    return listOfFiles
  }
}
