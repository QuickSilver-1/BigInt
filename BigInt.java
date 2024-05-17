import java.util.ArrayList;

class BigInt { // Class for working with large numbers

    private final boolean MINUS; // Variable for storing the sign of a number
    private final ArrayList<Integer> digits = new ArrayList<>(); // Variable for storing the digits of a number in an array

    BigInt(String str) { // Constructor for creating class objects based on a string

        int index;
        MINUS = str.charAt(0) == '-';
        index = str.charAt(0) == '-' ? 1 : 0;

        while (index < str.length()) {
            if (!digits.isEmpty() || !(str.charAt(index) == '0')) digits.add(str.charAt(index) - '0');
            index += 1;
        }
        
        if (digits.isEmpty()) digits.add(0);
    }

    @Override
    public String toString() { // Printing a class object to a string
        return (MINUS) ? "-" + this.toStringAbs() : this.toStringAbs();
    }

    private String toStringAbs() { // Printing a class object to a string without taking into account the sign
        StringBuilder answer = new StringBuilder();
        for (int digit : digits) answer.append(digit);
        return answer.toString();
    }

    public static BigInt valueOf(long number) { // Use a similar method from the String class
        return new BigInt(String.valueOf(number));
    }

    public BigInt add(BigInt secondNumber) { // Consider numbers with different signs

        if (!this.MINUS) {
            return (!secondNumber.MINUS) ? this.addSign(secondNumber, false) : this.subtractSign(secondNumber);
        } else {
            return (!secondNumber.MINUS) ? secondNumber.subtractSign(this) : this.addSign(secondNumber, true);
        }
    }

    public BigInt subtract(BigInt secondNumber) { // Consider numbers with different signs

        if (!this.MINUS) {
            return (!secondNumber.MINUS) ? this.subtractSign(secondNumber) : this.addSign(secondNumber, false);
        } else {
            return (!secondNumber.MINUS) ? secondNumber.addSign(this, true) : secondNumber.subtractSign(this);
        }
    }

    private BigInt addSign(BigInt secondNumber, boolean reverse) {

        StringBuilder answer = new StringBuilder();
        int maxLength = Math.max(this.digits.size(), secondNumber.digits.size()), tens = 0, memory = 0, units, sum;

        // Column addition
        for (int count = 0; count < Math.min(this.digits.size(), secondNumber.digits.size()); count++) {

            sum = this.digits.get(this.digits.size() - count - 1) + secondNumber.digits.get(secondNumber.digits.size() - count - 1);
            units = sum % 10;

            // Add numbers to the string in reverse order
            answer.append(units + tens >= 10 ? (units + tens) % 10 : units + tens);

            tens = (sum + tens) / 10;
            memory = count;
        }

        // Add the remaining digits of the larger number to the response
        String last;
        for (int count = memory + 1; count < maxLength; count++) {

            last = (this.digits.size() > secondNumber.digits.size()) ?
                    String.valueOf(this.digits.get(maxLength - count - 1) + tens) :
                    String.valueOf(secondNumber.digits.get(maxLength - count - 1) + tens);
            answer.append(last.charAt(last.length() - 1));

            tens = last.length() == 2 ? 1 : 0;
        }

        if (tens == 1) answer.append(1);
        answer.reverse();

        // Take into account the signs of numbers
        return reverse ? new BigInt("-" + answer) : new BigInt(answer.toString());
    }

    private BigInt subtractSign(BigInt secondNumber) {

        StringBuilder answer = new StringBuilder();
        int maxLength = Math.max(secondNumber.digits.size(), this.digits.size()), debt = 0, memory = 0, subtract;

        // Column subtraction
        for (int count = 0; count < Math.min(this.digits.size(), secondNumber.digits.size()); count++) {

            if (this.compareToAbs(secondNumber) == 0) return new BigInt("0");

            subtract = this.compareToAbs(secondNumber) == 1 ?
                    this.digits.get(this.digits.size() - count - 1) - secondNumber.digits.get(secondNumber.digits.size() - count - 1) :
                    secondNumber.digits.get(secondNumber.digits.size() - count - 1) - this.digits.get(this.digits.size() - count - 1);

            answer.append(subtract + debt < 0 ? subtract + 10 + debt : subtract + debt);
            debt = subtract + debt < 0 ? -1 : 0;
            memory = count;
        }

        // Add to the answer the remaining digits of a larger absolute number
        for (int count = memory + 1; count < maxLength; count++) {

            answer.append(this.digits.size() > secondNumber.digits.size() ?
                    this.digits.get(maxLength - count - 1) + debt :
                    secondNumber.digits.get(maxLength - count - 1) + debt);

            if (answer.charAt(answer.length() - 2) == '-') {
                answer.delete(answer.length() - 2, answer.length() - 1);
                answer.setCharAt(answer.length() - 1, (char) (10 - (answer.charAt(answer.length() - 1) - '0') + 48));
                debt = -1;
            } else debt = 0;
        }

        answer.reverse();

        // Take into account the signs
        return this.compareToAbs(secondNumber) == -1 ? new BigInt("-" + answer) : new BigInt(answer.toString());
    }

    public BigInt multiply(BigInt secondNumber) {

        int[] column = new int[this.digits.size() + secondNumber.digits.size()];
        int index, productNumbers, units, memory, tens;

        // Column multiplication
        for (int step = 0; step < secondNumber.digits.size(); step++) {

            memory = 0;
            tens = 0;

            for (int count = 0; count < this.digits.size(); count++) {

                productNumbers = this.digits.get(this.digits.size() - count - 1) * secondNumber.digits.get(secondNumber.digits.size() - step - 1);
                units = productNumbers % 10;
                index = column.length - step - count - 1;
                memory = (column[index] + productNumbers + tens) / 10;
                column[index] = (column[index] + units + tens) % 10;
                tens = memory;
                memory = count + 1;
            }

            // Add the remaining digits
            while (tens > 0) {

                column[column.length - step - memory - 1] = tens % 10;
                memory += 1;
                tens /= 10;
            }
        }

        final StringBuilder answer = new StringBuilder();

        // Write the response to a string
        for (int i : column) if (!(answer.toString() == "") || i != 0) answer.append(i);
        if ((answer.toString() == "")) answer.append(0);

        // Take into account the signs
        return ((this.MINUS ^ secondNumber.MINUS) && !(new BigInt(answer.toString()).toString().equals("0"))) ? new BigInt("-" + answer) : new BigInt(answer.toString());

    }

    public BigInt divide(BigInt secondNumber) {

        StringBuilder answer = new StringBuilder();
        String[] answerWithRemainder;

        // Take into account exceptions (divisors 1 and 0)
        if (secondNumber.toString().equals("0")) throw new ArithmeticException("BigInt divide by zero");
        else if (this.compareToAbs(secondNumber) < 0) return new BigInt("0");

        int start = 0;
        String remainder = "0";

        // Division by column
        while (start < this.digits.size()) {

            // Find the smallest part of the dividend that is greater than the divisor
            StringBuilder subNumber = new StringBuilder(remainder);

            while (start < secondNumber.digits.size()) {
                subNumber.append(this.digits.get(start));
                start += 1;
            }

            BigInt subNumberBigInt = new BigInt(subNumber.toString());
            if (subNumberBigInt.compareToAbs(secondNumber) < 0) {
                subNumberBigInt.digits.add(this.digits.get(start));
                start += 1;
            }

            // Find a simple quotient by subtraction
            answerWithRemainder = subNumberBigInt.divideSubtract(secondNumber);
            answer.append(answerWithRemainder[0]);
            remainder = answerWithRemainder[1];
        }

        // Take into account the signs
        return this.MINUS ^ secondNumber.MINUS && !answer.toString().equals("0") ? new BigInt("-" + answer) : new BigInt(answer.toString());
    }

    private String[] divideSubtract(BigInt secondNumber) {

        BigInt answer = new BigInt("0"),
                firstNumberCopy = new BigInt(this.toStringAbs()),
                secondNumberCopy = new BigInt(secondNumber.toStringAbs());

        // Subtract the divisor from the dividend and count the cycles
        while (firstNumberCopy.compareToAbs(secondNumberCopy) >= 0) {
            firstNumberCopy = firstNumberCopy.subtract(secondNumberCopy);
            answer = answer.add(new BigInt("1"));
        }

        return new String[]{answer.toString(), firstNumberCopy.toString()};
    }

    public int compareTo(BigInt secondNumber) {

        // Compare signs
        if (this.MINUS && !secondNumber.MINUS) return -1;
        else if (!this.MINUS && secondNumber.MINUS) return 1;
        // Compare numbers by module
        else if (this.MINUS) return -this.compareToAbs(secondNumber);
        else return this.compareToAbs(secondNumber);
    }

    private int compareToAbs(BigInt secondNumber) {

        // Compare numbers with the same sign element by element
        if (this.digits.size() > secondNumber.digits.size()) return 1;
        else if (this.digits.size() < secondNumber.digits.size()) return -1;
        else {
            for (int i = 0; i < this.digits.size(); i++) {
                if (this.digits.get(i) > secondNumber.digits.get(i)) return 1;
                else if (this.digits.get(i) < secondNumber.digits.get(i)) return -1;
            }
        }
        return 0;
    }
}
