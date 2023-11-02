package se.alipsa.gi.fx;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

class PasswordDialog extends Dialog<String> {

  PasswordDialog(String title, String message) {
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL)
    setTitle(title)
    setHeaderText(message)
    GridPane pane = new GridPane()
    pane.setPadding(new Insets(5))
    getDialogPane().setContent(pane)

    pane.add(new Label("Password: "), 0, 2)
    PasswordField pwdTf = new PasswordField()
    pane.add(pwdTf, 1, 2)
    pwdTf.requestFocus()
    setResultConverter(callback -> callback == ButtonType.OK ? pwdTf.getText() : null)
  }
}
