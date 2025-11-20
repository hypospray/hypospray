package eu.jbeernink.hypospray.util.value;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class LazyValue<T> {

	private final AtomicReference<T> reference = new AtomicReference<>();
	private final Supplier<T> supplier;

	public LazyValue(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public T get() {
		return reference.updateAndGet(value -> {
			if (value == null) {
				return requireNonNull(supplier.get());
			}

			return value;
		});
	}

	public boolean isCreated() {
		return reference.get() != null;
	}
}
