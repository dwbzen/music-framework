package util.cp;

import java.util.Set;
import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 * This is a functional interface whose functional method is apply(T t).
 * This defines the interface of a ICollector-IProducer pair
 * 
 * @author don_bacon
 *
 * @param <T> the argument
 * @param <R> the result
 */
public interface IProducer<T, R>  extends Function<T, R> {
	R apply(T arg);
	Set<R> produce();
}
