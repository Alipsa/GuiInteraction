package se.alipsa.gi

import groovy.transform.CompileStatic

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

@CompileStatic
class FileTransferable implements Transferable {
  private List listOfFiles;

  FileTransferable(List listOfFiles) {
    if (listOfFiles == null) {
      throw new IllegalArgumentException("listOfFiles cannot be null")
    }
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
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor)
    }
    return listOfFiles
  }
}
