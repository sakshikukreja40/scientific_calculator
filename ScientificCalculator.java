import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ScientificCalculator extends JFrame implements ActionListener {
    private JTextField textField;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setEditable(false);
        textField.setPreferredSize(new Dimension(250, 40)); // Reduced width
        add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(9, 4)); // Adjusted grid layout
        buttonPanel.setBackground(Color.LIGHT_GRAY); // Changed color to light gray
        buttonPanel.setForeground(Color.PINK);

        // Button labels for the calculator
        String[] buttonLabels = {
                "1", "2", "3", "/", "4", "5", "6", "*", "7", "8", "9", "-", "0", ".", "=", "+", "Clear",
                "(", ")", "^", "sqrt", "cbrt", "log", "sin", "cos", "tan", "=", "Delete"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        String expression = textField.getText();

        switch (command) {
            case "=":
                try {
                    double result = evaluateExpression(expression);
                    textField.setText(Double.toString(result));
                } catch (ArithmeticException e) {
                    textField.setText("Error: " + e.getMessage());
                }
                break;
            case "Clear":
                textField.setText("");
                break;
            case "Delete":
                if (!expression.isEmpty()) {
                    String newExpression = expression.substring(0, expression.length() - 1);
                    textField.setText(newExpression);
                }
                break;
            default:
                textField.setText(expression + command);
                break;
        }
    }

    // Evaluate the mathematical expression using the expression parser
    private double evaluateExpression(String expression) {
        return new ExpressionParser().parse(expression);
    }

    // ExpressionParser handles parsing and evaluating mathematical expressions
    private static class ExpressionParser {
        private int pos = -1;
        private int ch;

        // Move to the next character in the expression
        private void nextChar(String expression) {
            ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
        }

        // Skip whitespace characters and check if the current character matches the one to be eaten
        private boolean eat(int charToEat, String expression) {
            while (ch == ' ') nextChar(expression);
            if (ch == charToEat) {
                nextChar(expression);
                return true;
            }
            return false;
        }

        // Parse the entire expression and return the result
        private double parse(String expression) {
            nextChar(expression);
            double x = parseExpression(expression);
            if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
            return x;
        }

        // Parse the expression by evaluating the terms and handling addition and subtraction
        private double parseExpression(String expression) {
            double x = parseTerm(expression);
            while (true) {
                if (eat('+', expression)) x += parseTerm(expression);
                else if (eat('-', expression)) x -= parseTerm(expression);
                else return x;
            }
        }

        // Parse the term by evaluating the factors and handling multiplication, division, and exponentiation
        private double parseTerm(String expression) {
            double x = parseFactor(expression);
            while (true) {
                if (eat('*', expression)) x *= parseFactor(expression);
                else if (eat('/', expression)) x /= parseFactor(expression);
                else if (eat('^', expression)) x = Math.pow(x, parseFactor(expression));
                else return x;
            }
        }

        // Parse the factor by handling positive/negative signs, parentheses, numbers, and functions
        private double parseFactor(String expression) {
            if (eat('+', expression)) return parseFactor(expression);
            if (eat('-', expression)) return -parseFactor(expression);

            double x;
            int startPos = this.pos;
            if (eat('(', expression)) {
                // Handle parentheses by recursively parsing the expression inside them
                x = parseExpression(expression);
                eat(')', expression);
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                // Parse numbers (integer or decimal)
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar(expression);
                x = Double.parseDouble(expression.substring(startPos, this.pos));
            } else if (ch >= 'a' && ch <= 'z') {
                // Parse functions (such as sqrt, sin, cos, etc.)
                while (ch >= 'a' && ch <= 'z') nextChar(expression);
                String func = expression.substring(startPos, this.pos);
                x = parseFactor(expression);
                switch (func) {
                    case "sqrt":
                        x = Math.sqrt(x);
                        break;
                    case "cbrt":
                        x = Math.cbrt(x);
                        break;
                    case "log":
                        x = Math.log10(x);
                        break;
                    case "sin":
                        x = Math.sin(Math.toRadians(x));
                        break;
                    case "cos":
                        x = Math.cos(Math.toRadians(x));
                        break;
                    case "tan":
                        x = Math.tan(Math.toRadians(x));
                        break;
                    default:
                        throw new RuntimeException("Unknown function: " + func);
                }
            } else {
                throw new RuntimeException("Unexpected: " + (char)ch);
            }

            return x;
        }
    }

    public static void main(String[] args) {
        // Create and display the calculator GUI
        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}
