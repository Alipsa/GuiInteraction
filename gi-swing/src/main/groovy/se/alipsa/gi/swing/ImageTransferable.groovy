package se.alipsa.gi.swing

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class ImageTransferable implements Transferable {
  private Image image;

  ImageTransferable(Image image) {
    this.image = image
  }

  Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException {
    if (isDataFlavorSupported(flavor)) {
      return image;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor == DataFlavor.imageFlavor
  }

  DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{DataFlavor.imageFlavor}
  }
}
