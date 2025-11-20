package eu.jbeernink.hypospray.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.spi.Context;

import eu.jbeernink.hypospray.util.stream.Collectors;

public record ScopeInstance(List<Context> contexts, Class<? extends Annotation> scopeAnnotation,
                            boolean isNormalScope) {

	public boolean isActive() {
		return contexts.stream().anyMatch(Context::isActive);
	}

	public Optional<Context> activeContext() {
		return contexts.stream()
		               .filter(Context::isActive)
		               .collect(Collectors.findOnly(() -> new IllegalArgumentException(
				               "More than one context active for scope: " + scopeAnnotation.getName())));
	}

	public ScopeInstance appendContextInstance(Context context) {
		// TODO validate context matches scope annotation.
		ArrayList<Context> contexts = new ArrayList<>(contexts());
		contexts.add(context);

		return new ScopeInstance(contexts, scopeAnnotation, isNormalScope);
	}
}
