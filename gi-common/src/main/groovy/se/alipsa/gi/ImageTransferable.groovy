package se.alipsa.gi

import groovy.transform.CompileStatic

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

@CompileStatic
class ImageTransferable implements Transferable {
  private Image image;

  ImageTransferable(Image image) {
    if (image == null) {
      throw new IllegalArgumentException("image cannot be null")
    }
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
