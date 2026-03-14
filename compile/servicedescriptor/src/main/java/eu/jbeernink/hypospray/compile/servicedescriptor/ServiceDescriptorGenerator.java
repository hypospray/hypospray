package eu.jbeernink.hypospray.compile.servicedescriptor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Gatherer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors")
public class ServiceDescriptorGenerator extends AbstractProcessor {

	private record Service(TypeElement service, TypeElement implementation) {}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Map<TypeElement, List<Service>> services = annotations.stream()
		                                                      .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(
				                                                      annotation).stream())
		                                                      .gather(isModuleElement())
		                                                      .flatMap(
				                                                      moduleElement -> moduleElement.getDirectives().stream())
		                                                      .filter(
				                                                      directive -> directive instanceof ModuleElement.ProvidesDirective)
		                                                      .flatMap(
				                                                      directive -> ((ModuleElement.ProvidesDirective) directive).getImplementations()
				                                                                                                                .stream()
				                                                                                                                .map(
						                                                                                                                implementation -> new Service(
								                                                                                                                ((ModuleElement.ProvidesDirective) directive).getService(),
								                                                                                                                implementation)))
		                                                      .collect(Collectors.groupingBy(Service::service));

		services.forEach((service, implementations) -> {
			try {
				FileObject resource = processingEnv.getFiler()
				                                   .createResource(StandardLocation.CLASS_OUTPUT, "",
						                                   "META-INF/services/" + service.getQualifiedName());

				try (BufferedWriter writer = new BufferedWriter(resource.openWriter())) {
					for (Service implementation : implementations) {
						writer.write(implementation.implementation.getQualifiedName().toString());
						writer.newLine();
					}

				}
			} catch (IOException e) {
				throw new UnsupportedOperationException(e);
			}
		});
		return false;
	}

	private static Gatherer<Element, ?, ModuleElement> isModuleElement() {
		return Gatherer.of((_, element, downstream) -> {
			if (element instanceof ModuleElement moduleElement) {
				return downstream.push(moduleElement);
			}

			return true;
		});
	}
}
