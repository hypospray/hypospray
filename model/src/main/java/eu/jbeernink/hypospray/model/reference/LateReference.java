package eu.jbeernink.hypospray.model.reference;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Reference to an object that may only be set at a later time.
///
/// A late reference can only be set once to a value that must not be `null`. Until a value is set, any operation that
/// attempts to retrieve the value will trigger an [IllegalStateException] to be thrown. Once a value has been set, this
/// object becomes immutable, any further attempts to set a value will also cause an [IllegalStateException] to be thrown.
///
/// @param <E> the type of the reference.
@NullMarked
public final class LateReference<E> implements Supplier<E> {

	private final AtomicReference<@Nullable E> reference = new AtomicReference<>();

	/// Sets the value of this reference.
	///
	/// This method may only be called once per each late reference instance.
	///
	/// @param value the value to be set.
	public void setValue(E value) {
		if (!reference.compareAndSet(null, requireNonNull(value))) {
			throw new IllegalStateException("Reference has already been set to a value.");
		}
	}

	public E get() {
		@Nullable E value = reference.get();
		if (value == null) {
			throw new IllegalStateException("Attempt to retrieve a reference before a reference was set.");
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO #91: Implement proper equals.
		return obj instanceof LateReference;
	}

	@Override
	public int hashCode() {
		// TODO #91: Implement proper hashCode.
		return LateReference.class.hashCode();
	}
}
