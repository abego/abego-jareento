# JavaProblem IntDivideByInt

## 'int / int' expression does not return fraction

When generating Java code based on Smalltalk code one must take special care
of 'divide int by int' expressions (like `1/3`) as these result in a
fraction in Smalltalk and an `int` in Java.

(Similar for other integral types, like `long` or `short`.)

## Definition

### Code to Match

    someExpression1 / someExpression2

### Validation

fail if

- type of `someExpression1` is an integral type, and
- type of `someExpression2` is an integral type

with an integral type being any of:

- `int`
- `long`
- `short`
- `byte`
- `java.lang.Integer`
- `java.lang.Long`
- `java.lang.Short`
- `java.lang.Byte`

## Tests

### Smoketest

#### File sample.Foo.java

    package sample;
    
    @SuppressWarnings("all")
    public class Foo {
        public static void main(String[] args) {
            Number x = 1 / 3;
            Double d = 1.0;
            Number y = d / 3;
            Number z = 1 / 3.0;
            int i = 1;
            Integer i2 = 3;
            Number n = i / i2;
            Number m = i - i2;
        }
    }

#### Expected Output

    Foo.java\t6\t'int / int' expression does not return fraction\tIntDivideByInt
    Foo.java\t12\t'int / int' expression does not return fraction\tIntDivideByInt
