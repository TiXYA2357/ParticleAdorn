package TiXYA2357.ParticleAdorn;

import java.util.*;
import java.util.function.Function;

/**
 * 增强版数学表达式计算器
 * 支持四则运算、括号、负数和函数调用
 */
public final class SimpleCalculatorWithParentheses {

    // 运算符优先级映射
    private static final Map<Character, Integer> PRECEDENCE = new HashMap<>(){{
        put('+', 1);
        put('-', 1);
        put('*', 2);
        put('/', 2);
    }};

    // 支持的函数映射
    private static final Map<String, Function<Double, Double>> FUNCTIONS = new HashMap<>(){{
        put("sin", Math::sin);
        put("cos", Math::cos);
        put("tan", Math::tan);
        put("sqrt", Math::sqrt);
        put("abs", Math::abs);
        put("log", Math::log);
        put("ln", Math::log);
        put("exp", Math::exp);
    }};

    /**
     * 计算给定的数学表达式
     *
     * @param expression 数学表达式字符串
     * @return 表达式的计算结果
     * @throws IllegalArgumentException 当表达式格式错误时抛出
     * @throws ArithmeticException 当出现数学错误时抛出
     */
    public static double calculate(String expression) {
        if (expression == null || expression.trim().isEmpty())
            throw new IllegalArgumentException("表达式不能为空");

        try {
            // 预处理表达式
            var processedExpr = preprocessExpression(expression);
            // 词法分析分割token
            var tokens = tokenize(processedExpr);
            // 转换为后缀表达式
            var postfix = infixToPostfix(tokens);
            // 计算后缀表达式
            return evaluatePostfix(postfix);
        } catch (Exception e) {
            throw new IllegalArgumentException("表达式计算错误: " + e.getMessage(), e);
        }
    }

    /**
     * 词法分析器 - 分割表达式为token
     */
    private static List<String> tokenize(String expression) {
        var tokens = new ArrayList<String>();
        var len = expression.length();
        var i = 0;

        while (i < len) {
            var ch = expression.charAt(i);
            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // 处理数字（包括小数）
            if (Character.isDigit(ch) || ch == '.') {
                var start = i;
                while (i < len && (Character.isDigit(expression
                        .charAt(i)) || expression.charAt(i) == '.')) i++;
                tokens.add(expression.substring(start, i));
                continue;
            }

            // 处理函数名
            if (Character.isLetter(ch)) {
                var start = i;
                while (i < len && Character
                        .isLetter(expression.charAt(i))) i++;
                tokens.add(expression.substring(start, i));
                continue;
            }

            // 处理运算符和括号
            if ("+-*/()".indexOf(ch) != -1) {
                tokens.add(String.valueOf(ch));
                i++;
                continue;
            }
            throw new IllegalArgumentException("无效字符: " + ch);
        }

        return tokens;
    }

    /**
     * 预处理表达式 - 修复负号和隐式乘法
     */
    private static String preprocessExpression(String expression) {
        // 首先去除所有空格
        var cleanExpr = new StringBuilder();
        for (var ch : expression.toCharArray()) if (!Character.isWhitespace(ch)) cleanExpr.append(ch);

        var expr = cleanExpr.toString();

        // 处理隐式乘法
        return expr.replaceAll("(\\d)\\(", "$1*(")
                .replaceAll("\\)(\\d)", ")*$1")
                .replaceAll("\\)([a-zA-Z])", ")*$1")
                .replaceAll("(\\d+)\\*-(\\d+)", "$1*(0-$2)")// 处理特殊情况：与负数相乘, 这必须在通用负号处理之前
                .replaceAll("^-", "0-")// 正确处理负号, 开头的负号
                .replaceAll("\\(-", "(0-")// 左括号后的负号
                .replaceAll("([+*/])-", "$10-");// 运算符后的负号（但不在右括号后，那是减法）
    }

    /**
     * 将中缀表达式转换为后缀表达式
     */
    private static List<String> infixToPostfix(List<String> tokens) {
        var output = new ArrayList<String>();
        var operators = new ArrayDeque<String>();

        for (var i = 0; i < tokens.size(); i++) {
            var token = tokens.get(i);

            if (isNumber(token)) output.add(token);
            else if (isFunction(token)) {
                // 检查函数参数
                if (i + 1 >= tokens.size() || !tokens.get(i + 1).equals("("))
                    throw new IllegalArgumentException("函数 " + token + " 缺少参数");
                operators.push(token);
                operators.push("("); // 为函数压入左括号
                i++; // 跳过tokens中的左括号
            } else if (token.equals("(")) operators.push(token);
            else if (token.equals(")")) {
                // 处理直到匹配的左括号
                while (!operators.isEmpty() && !operators.peek().equals("("))
                    output.add(operators.pop());

                // 检查括号是否匹配
                if (operators.isEmpty()) throw new IllegalArgumentException("括号不匹配");

                operators.pop(); // 移除左括号

                // 如果栈顶是函数，也要弹出
                if (!operators.isEmpty() && isFunction(operators.peek()))
                    output.add(operators.pop());
            } else if (isOperator(token.charAt(0))) {
                var op = token.charAt(0);
                // 弹出优先级更高或相等的运算符
                while (!operators.isEmpty() &&
                       !operators.peek().equals("(") &&
                       precedence(operators.peek().charAt(0)) >= precedence(op))
                    output.add(operators.pop());
                operators.push(token);
            } else throw new IllegalArgumentException("无效token: " + token);
        }

        // 处理剩余的运算符
        while (!operators.isEmpty()) {
            var op = operators.pop();
            if (op.equals("(")) throw new IllegalArgumentException("括号不匹配");
            output.add(op);
        }
        return output;
    }

    /**
     * 计算后缀表达式
     */
    private static double evaluatePostfix(List<String> postfix) {
        var stack = new ArrayDeque<Double>();

        for (var token : postfix) {
            if (isNumber(token)) stack.push(Double.parseDouble(token));
            else if (isFunction(token)) {
                if (stack.isEmpty()) throw new IllegalArgumentException("函数参数不足: " + token);
                stack.push(applyFunction(token, stack.pop()));
            } else if (isOperator(token.charAt(0))) {
                if (stack.size() < 2) throw new IllegalArgumentException("运算符操作数不足: " + token);
                var b = stack.pop();
                var a = stack.pop();
                stack.push(applyOperator(token.charAt(0), a, b));
            }
        }
        if (stack.size() != 1) throw new IllegalArgumentException("表达式格式错误");

        return stack.pop();
    }

    /**
     * 判断字符串是否为数字
     */
    private static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为函数
     */
    private static boolean isFunction(String str) {
        return FUNCTIONS.containsKey(str.toLowerCase());
    }

    /**
     * 判断字符是否为运算符
     */
    private static boolean isOperator(char ch) {
        return PRECEDENCE.containsKey(ch);
    }

    /**
     * 获取运算符优先级
     */
    private static int precedence(char op) {
        return PRECEDENCE.getOrDefault(op, 0);
    }

    /**
     * 应用运算符
     */
    private static double applyOperator(char operator, double a, double b) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) throw new ArithmeticException("除数不能为零");
                yield a / b;
            }
            default -> throw new IllegalArgumentException("不支持的运算符: " + operator);
        };
    }

    /**
     * 应用函数
     */
    private static double applyFunction(String functionName, double operand) {
        var function = FUNCTIONS.get(functionName.toLowerCase());
        if (function == null) throw new IllegalArgumentException("不支持的函数: " + functionName);

        // 三角函数特殊处理（使用角度制）
        if (functionName.equalsIgnoreCase("sin") ||
            functionName.equalsIgnoreCase("cos") ||
            functionName.equalsIgnoreCase("tan"))
            operand = Math.toRadians(operand);
        try {
            return function.apply(operand);
        } catch (Exception e) {
            throw new ArithmeticException("函数计算错误: " + functionName + "(" + operand + ")");
        }
    }

    /**
     * 获取支持的函数列表
     */
    public static Set<String> getSupportedFunctions() {
        return FUNCTIONS.keySet();
    }

    /**
     * 添加自定义函数
     */
    public static void addFunction(String name, Function<Double, Double> function) {
        FUNCTIONS.put(name.toLowerCase(), function);
    }

    // 测试示例
//    public static void main(String[] args) {
//        System.out.println("=== 数学表达式计算器测试 ===");
//
//        // 首先测试有问题的情况
//        System.out.println("\n--- 测试有问题的情况 ---");
//        testCalculation("2*-6", -12.0);
//        testCalculation("sin(30)", 0.5);
//        testCalculation("sqrt(16)", 4.0);
//
//        // 基本算术测试
//        System.out.println("\n--- 基本算术测试 ---");
//        testCalculation("2 + 3 * 4", 14.0);
//        testCalculation("(2 + 3) * 4", 20.0);
//        testCalculation("10 / 2 - 3", 2.0);
//        testCalculation("2 * 3", 6.0);
//
//        // 负数测试
//        System.out.println("\n--- 负数测试 ---");
//        testCalculation("-5 + 3", -2.0);
//        testCalculation("(-2) * 3", -6.0);
//        testCalculation("-(-5)", 5.0);
//        testCalculation("-(-5) + - 3", 2.0);
//
//        // 函数测试
//        System.out.println("\n--- 函数测试 ---");
//        testCalculation("cos(60)", 0.5);
//        testCalculation("abs(-5)", 5.0);
//
//        // 复杂表达式测试
//        System.out.println("\n--- 复杂表达式测试 ---");
//        testCalculation("2 * (3 + 4) - 5", 9.0);
//        testCalculation("sin(30) + cos(60)", 1.0);
//        testCalculation("sqrt(25) * 2", 10.0);
//        testCalculation("2 * sin(30)", 1.0);
//
//        System.out.println("\n所有测试完成!");
//    }

//    private static void testCalculation(String expr, double expected) {
//        try {
//            var result = calculate(expr);
//            var passed = Math.abs(result - expected) < 1e-10;
//            System.out.printf("测试: " + expr + "  结果: %.6f (期望: %.6f) %s%n",
//                result, expected, passed ? "★" : "☆");
//        } catch (Exception e) {
//            System.out.printf("错误: %s%n", e);
//        }
//    }
}
