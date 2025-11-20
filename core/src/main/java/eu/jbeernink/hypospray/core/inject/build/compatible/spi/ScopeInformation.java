package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.build.compatible.spi.ScopeInfo;

import eu.jbeernink.hypospray.model.information.ClassInformation;

/// Information about a scope.
public record ScopeInformation(ClassInformation<? extends Annotation> annotation, boolean isNormal) implements ScopeInfo {

}
