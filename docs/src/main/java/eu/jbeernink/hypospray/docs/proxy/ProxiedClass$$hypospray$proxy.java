package eu.jbeernink.hypospray.docs.proxy;

import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;

public class ProxiedClass$$hypospray$proxy extends ProxiedClass {
	private final Function<String, Invoker<Void, ?>> invokerFactory;

	public ProxiedClass$$hypospray$proxy(Function<String, Invoker<Void, ?>> invokerFactory) {
		this.invokerFactory = invokerFactory;
	}

	@Override
	public int hashCode() {
		try {
			Invoker<Void, ?> invoker = invokerFactory.apply("hashCode[()I]");
			return (int) invoker.invoke(null, new Object[]{});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected checked exception thrown.", e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Invoker<Void, ?> invoker = invokerFactory.apply("equals[(Ljava.lang.Object;)Z]");
			return (boolean) invoker.invoke(null, new Object[]{obj});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected checked exception thrown.", e);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		try {
			Invoker<Void, ?> invoker = invokerFactory.apply("clone[()java.lang.Object;]");
			return invoker.invoke(null, new Object[]{});
		} catch (RuntimeException | CloneNotSupportedException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected checked exception thrown.", e);
		}
	}

	@Override
	public String toString() {
		try {
			Invoker<Void, ?> invoker = invokerFactory.apply("toString[()Ljava/lang/String;]");
			return (String) invoker.invoke(null, new Object[]{});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected checked exception thrown.", e);
		}
	}

	@Override
	public void run() {
		try {
			Invoker<Void, ?> invoker = invokerFactory.apply("run[()V]");
			invoker.invoke(null, new Object[]{});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void throwsException() throws Exception {
		Invoker<Void, ?> invoker = invokerFactory.apply("throwsException[()V]");
		invoker.invoke(null, new Object[]{});
	}
}
