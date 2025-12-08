package pendulum.uielements;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.text.*;

public class TextFieldUtils {

    public static JTextField createNumericTextField(
            int x, int y, int width, int height,
            Font font,
            Color activeColor,
            Color errorColor,
            boolean allowDecimal,
            Consumer<Number> onValueSet
    ) {
        JTextField textField = new RoundedTextField(5);
        textField.setBounds(x, y, width, height);
        textField.setFont(font);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBackground(Color.WHITE);

        // --- Allow digits and at most one period if decimals are allowed ---
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (isValidInput(fb, string)) super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                if (isValidInput(fb, string)) super.replace(fb, offset, length, string, attrs);
            }

            private boolean isValidInput(FilterBypass fb, String text) throws BadLocationException {
                if (text == null) return true;
                String newText = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                        .insert(fb.getDocument().getLength(), text)
                        .toString();
                return allowDecimal ? newText.matches("\\d*\\.?\\d*") : newText.matches("\\d*");
            }
        });

        // --- Parse and handle Enter key ---
        textField.addActionListener(e -> {
            try {
                String text = textField.getText().trim();
                @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
                Number value = allowDecimal ? Double.parseDouble(text) : Integer.parseInt(text);
                onValueSet.accept(value);
                textField.setBackground(Color.WHITE);
            } catch (NumberFormatException ex) {
                textField.setBackground(errorColor);
            }
        });

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));
            }
            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }
        });

        return textField;
    }
    
}