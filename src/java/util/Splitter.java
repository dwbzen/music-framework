package util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cascade Pattern is used to remove the duplication and the verbosity of the code.
 * @author bacond6
 *
 * @param <T>
 */
public class Splitter<T> {
    private List<T> passed;
    private List<T> notPassed;

    protected Splitter(List<T> passed, List<T> notPassed)     {
        this.passed = passed;
        this.notPassed = notPassed;
    }

    public static <T> Splitter<T> splitBy(Collection<T> items, Predicate<T> test) {
        List<T> passed = new LinkedList<T>();
        List<T> notPassed = new LinkedList<T>();
        items.stream().forEach(item -> {
            if(test.test(item)){
                passed.add(item);
                return;
            }
            notPassed.add(item);
        });
        return new Splitter<T>(passed, notPassed);
    }

    public static <T> Splitter<T> splitByPartition(Collection<T> items, Predicate<T> test) {
        Map<Boolean, List<T>> map = items.stream().collect(Collectors.partitioningBy(test));

        return new Splitter<T>(map.get(true), map.get(false));
    }

    public Splitter<T> workWithPassed(Consumer<Stream<T>> func) {
        func.accept(passed.stream());
        return this;    // cascade pattern - allows chaining //
    }

    public Splitter<T> workWithNotPassed(Consumer<Stream<T>> func) {
        func.accept(notPassed.stream());
        return this;    // cascade pattern - allows chaining //
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Splitter.splitBy(numbers, num -> num%2 == 0)
                    .workWithPassed(passed ->
                        passed.forEach(even -> System.out.println("" + even + " -> " + even)))
                    .workWithNotPassed(notPassed ->
                        notPassed.map(odd -> odd * odd)
                            .forEach(odd -> System.out.println("" + Math.sqrt(odd) + " -> " + odd)));

        Splitter.splitByPartition(numbers, num -> num%2 == 0)
        .workWithPassed(passed ->
            passed.forEach(even -> System.out.println("" + even + " -> " + even)))
        .workWithNotPassed(notPassed ->
            notPassed.map(odd -> odd * odd)
                .forEach(odd -> System.out.println("" + Math.sqrt(odd) + " -> " + odd)
        ));
    }
}
