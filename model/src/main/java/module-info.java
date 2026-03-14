import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.source.DefaultClassInformationSource;

@NullMarked
@GenerateServiceDescriptors
module eu.jbeernink.hypospray.model {
	requires transitive jakarta.cdi;
	requires eu.jbeernink.hypospray.annotation;
	requires eu.jbeernink.hypospray.invoker;
	requires eu.jbeernink.hypospray.util;
	requires org.jspecify;

	exports eu.jbeernink.hypospray.model to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.codegeneration.testing;

	exports eu.jbeernink.hypospray.model.annotation to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.codegeneration.generator, eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing;

	exports eu.jbeernink.hypospray.model.information to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.codegeneration.generator, eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing;
	exports eu.jbeernink.hypospray.model.information.lazy to eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.model.information.reflection to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.codegeneration.testing;
	// Only required for unit tests.
	opens eu.jbeernink.hypospray.model.information.reflection to eu.jbeernink.hypospray.invoker;
	exports eu.jbeernink.hypospray.model.information.source to eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.model.information.synthetic to eu.jbeernink.hypospray.core;

	exports eu.jbeernink.hypospray.model.types to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.codegeneration.generator, eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing;
	exports eu.jbeernink.hypospray.model.types.reflection to eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.model.reference to eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.model.information.synthetic.builder to eu.jbeernink.hypospray.core;

	uses ClassInformationSource;
	provides ClassInformationSource with DefaultClassInformationSource;
}