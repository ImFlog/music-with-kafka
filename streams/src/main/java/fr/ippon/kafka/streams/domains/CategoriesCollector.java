package fr.ippon.kafka.streams.domains;

import lombok.AllArgsConstructor;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@AllArgsConstructor
public class CategoriesCollector implements Collector<File, Categories, Categories> {

    private Function<File, Integer> counter;

    @Override
    public Supplier<Categories> supplier() {
        return Categories::new;
    }

    @Override
    public BiConsumer<Categories, File> accumulator() {
        return (categories, file) -> categories.add(file.getName(), counter.apply(file));
    }

    @Override
    public BinaryOperator<Categories> combiner() {
        return Categories::merge;
    }

    @Override
    public Function<Categories, Categories> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.singleton(Characteristics.IDENTITY_FINISH);
    }
}
