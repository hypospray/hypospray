package eu.jbeernink.hypospray.codegeneration.classfile.spi;

import static eu.jbeernink.hypospray.model.types.VoidTypeInstance.VOID;
import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.TypeKind.REFERENCE;
import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Short;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import static java.lang.constant.ConstantDescs.MTD_void;
import static java.util.Comparator.comparingInt;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;
import jakarta.inject.Inject;

import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxy;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxyConfiguration;
import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public class ClassFileApiClientProxyGenerator implements ClientProxyGenerator {

	private static final String GENERATED_FIELD_PREFIX = "hypospray$$field";
	private static final String INVOKER_FACTORY_FIELD_NAME = "%s$invokerFactory".formatted(GENERATED_FIELD_PREFIX);

	private static final ClassDesc FUNCTION_CLASS_DESCRIPTOR = createClassDescriptor(Function.class);
	private static final ClassDesc INVOKER_CLASS_DESCRIPTOR = createClassDescriptor(Invoker.class);
	private static final MethodTypeDesc PROXY_CONSTRUCTOR_DESCRIPTOR =
			MethodTypeDesc.of(CD_void, FUNCTION_CLASS_DESCRIPTOR);

	@Override
	public ClientProxy createClientProxy(ClientProxyConfiguration clientProxyConfiguration) {
		var descriptor = ClassDesc.of(clientProxyConfiguration.proxyClassName());
		byte[] classFile = ClassFile.of()
		                            .build(descriptor, classBuilder -> buildProxyClass(classBuilder, descriptor,
				                            clientProxyConfiguration));


		return new ClientProxy(classFile);
	}

	private static void buildProxyClass(ClassBuilder classBuilder, ClassDesc classDescriptor,
	                                    ClientProxyConfiguration configuration) {

		ClassDesc superClassDescriptor = createClassDescriptor(configuration.proxiedClassInformation());

		classBuilder.withSuperclass(superClassDescriptor)
		            .withFlags(ACC_PUBLIC)
		            .withField(INVOKER_FACTORY_FIELD_NAME, FUNCTION_CLASS_DESCRIPTOR, ACC_FINAL | ACC_PRIVATE);

		ConstructorInformation<?> superClassConstructor =
				findSuperClassConstructor(configuration.proxiedClassInformation());

		classBuilder.withMethod(INIT_NAME, PROXY_CONSTRUCTOR_DESCRIPTOR, ACC_PUBLIC,
				methodBuilder -> createConstructor(methodBuilder, classDescriptor, superClassDescriptor,
						superClassConstructor));

		defineMethods(classBuilder, classDescriptor, configuration);
	}

	private static ConstructorInformation<?> findSuperClassConstructor(ClassInformation<?> classInformation) {
		return classInformation.constructorInformation()
		                       .stream()
		                       .filter(constructor -> constructor.parameterInformation().isEmpty() ||
		                                              constructor.hasAnnotation(Inject.class))
		                       .min(comparingInt(constructor -> constructor.parameterInformation().size()))
		                       .orElseThrow(() -> new IllegalArgumentException(
				                       "Cannot create a proxy for %s. Class does not have a public, protected or package friendly no-argument constructor or constructor marked with @Inject.".formatted(
						                       classInformation.name())));
	}

	private static void defineMethods(ClassBuilder classBuilder, ClassDesc classDescriptor,
	                                  ClientProxyConfiguration configuration) {
		FieldRefEntry invokerFactoryFieldRef = classBuilder.constantPool()
		                                                   .fieldRefEntry(classDescriptor, INVOKER_FACTORY_FIELD_NAME,
				                                                   FUNCTION_CLASS_DESCRIPTOR);

		configuration.proxiedClassInformation()
		             .allMethods()
		             .stream()
		             .filter(ClassFileApiClientProxyGenerator::isProxyableMethod)
		             .toList()
		             .forEach(method -> {
			             MethodTypeDesc methodTypeDesc =
					             MethodTypeDesc.of(ClassDesc.ofDescriptor(method.returnType().descriptorString()),
							             method.parameterInformation()
							                   .stream()
							                   .map(parameter -> parameter.type().descriptorString())
							                   .map(ClassDesc::ofDescriptor)
							                   .toList());

			             classBuilder.withMethod(method.name(), methodTypeDesc, ACC_PUBLIC,
					             methodBuilder -> methodBuilder.withCode(
							             codeBuilder -> generateProxyMethod(codeBuilder, method, invokerFactoryFieldRef)));
		             });

		classBuilder.withMethod("longTestTestTest",
				MethodTypeDesc.of(ClassDesc.ofDescriptor("J"), ClassDesc.ofDescriptor("J")), ACC_PUBLIC,
				methodBuilder -> methodBuilder.withCode(codeBuilder -> codeBuilder.lload(1).lload(1).lmul().lreturn()));

	}


	private static void generateProxyMethod(CodeBuilder codeBuilder, MethodInformation method,
	                                        FieldRefEntry invokerFactoryField) {
		int numberOfParameters = method.parameters().size();

		int invokerSlot = codeBuilder.allocateLocal(REFERENCE);
		int parameterArraySlot = codeBuilder.allocateLocal(REFERENCE);

		codeBuilder.aload(codeBuilder.receiverSlot())
		           .getfield(invokerFactoryField)
		           .ldc(method.methodIdentifier())
		           .invokeinterface(FUNCTION_CLASS_DESCRIPTOR, "apply", MethodTypeDesc.of(CD_Object, CD_Object))
		           .checkcast(INVOKER_CLASS_DESCRIPTOR)
		           .astore(invokerSlot)
		           .loadConstant(numberOfParameters)
		           .anewarray(CD_Object)
		           .astore(parameterArraySlot);

		for (int i = 0; i < numberOfParameters; i++) {
			ParameterInformation parameter = method.parameterInformation().get(i);
			codeBuilder.aload(parameterArraySlot).ldc(codeBuilder.constantPool().intEntry(i));

			if (parameter.type() instanceof PrimitiveTypeInstance(PrimitiveKind primitiveKind, _)) {
				var _ = storeBoxedPrimitiveOnStack(codeBuilder, primitiveKind, codeBuilder.parameterSlot(i));
			} else {
				codeBuilder.aload(codeBuilder.parameterSlot(i));
			}

			codeBuilder.aastore();
		}

		codeBuilder.aload(invokerSlot)
		           .aconst_null()
		           .aload(parameterArraySlot)
		           .invokeinterface(INVOKER_CLASS_DESCRIPTOR, "invoke",
				           MethodTypeDesc.of(CD_Object, CD_Object, CD_Object.arrayType()));

		switch (method.returnType()) {
			case PrimitiveTypeInstance(PrimitiveKind primitiveKind, _) -> {
				switch (primitiveKind) {
					case BOOLEAN -> codeBuilder.checkcast(CD_Boolean)
					                           .invokevirtual(CD_Boolean, "booleanValue", MethodTypeDesc.of(CD_boolean))
					                           .ireturn();
					case BYTE ->
							codeBuilder.checkcast(CD_Byte).invokevirtual(CD_Byte, "byteValue", MethodTypeDesc.of(CD_byte)).ireturn();
					case SHORT -> codeBuilder.checkcast(CD_Short)
					                         .invokevirtual(CD_Short, "shortValue", MethodTypeDesc.of(CD_short))
					                         .ireturn();
					case CHAR -> codeBuilder.checkcast(CD_Character)
					                        .invokevirtual(CD_Character, "charValue", MethodTypeDesc.of(CD_char))
					                        .ireturn();
					case INT -> codeBuilder.checkcast(CD_Integer)
					                       .invokevirtual(CD_Integer, "intValue", MethodTypeDesc.of(CD_int))
					                       .ireturn();
					case LONG ->
							codeBuilder.checkcast(CD_Long).invokevirtual(CD_Long, "longValue", MethodTypeDesc.of(CD_long)).lreturn();
					case FLOAT -> codeBuilder.checkcast(CD_Float)
					                         .invokevirtual(CD_Float, "floatValue", MethodTypeDesc.of(CD_float))
					                         .freturn();
					case DOUBLE -> codeBuilder.checkcast(CD_Double)
					                          .invokevirtual(CD_Double, "doubleValue", MethodTypeDesc.of(CD_double))
					                          .dreturn();
				}
			}
			case VOID -> codeBuilder.pop().return_();
			case TypeInstance returnType ->
					codeBuilder.checkcast(ClassDesc.ofDescriptor(returnType.descriptorString())).areturn();
		}
	}

	private static CodeBuilder storeBoxedPrimitiveOnStack(CodeBuilder codeBuilder, PrimitiveKind primitiveKind,
	                                                      int parameterSlot) {
		return switch (primitiveKind) {
			case BOOLEAN -> {
				MethodRefEntry booleanValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Boolean, "valueOf", MethodTypeDesc.of(CD_Boolean, CD_boolean));
				yield codeBuilder.iload(parameterSlot).invokestatic(booleanValueOfMethod);
			}
			case BYTE -> {
				MethodRefEntry byteValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Byte, "valueOf", MethodTypeDesc.of(CD_Byte, CD_byte));
				yield codeBuilder.iload(parameterSlot).invokestatic(byteValueOfMethod);
			}
			case SHORT -> {
				MethodRefEntry shortValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Short, "valueOf", MethodTypeDesc.of(CD_Short, CD_short));
				yield codeBuilder.iload(parameterSlot).invokestatic(shortValueOfMethod);
			}
			case CHAR -> {
				MethodRefEntry charValueOfMethod = codeBuilder.constantPool()
				                                              .methodRefEntry(CD_Character, "valueOf",
						                                              MethodTypeDesc.of(CD_Character, CD_char));
				yield codeBuilder.iload(parameterSlot).invokestatic(charValueOfMethod);
			}
			case INT -> {
				MethodRefEntry integerValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Integer, "valueOf", MethodTypeDesc.of(CD_Integer, CD_int));
				yield codeBuilder.iload(parameterSlot).invokestatic(integerValueOfMethod);
			}
			case LONG -> {
				MethodRefEntry longValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Long, "valueOf", MethodTypeDesc.of(CD_Long, CD_long));
				yield codeBuilder.lload(parameterSlot).invokestatic(longValueOfMethod);
			}
			case FLOAT -> {
				MethodRefEntry floatValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Float, "valueOf", MethodTypeDesc.of(CD_Float, CD_float));
				yield codeBuilder.fload(parameterSlot).invokestatic(floatValueOfMethod);
			}
			case DOUBLE -> {
				MethodRefEntry doubleValueOfMethod =
						codeBuilder.constantPool().methodRefEntry(CD_Double, "valueOf", MethodTypeDesc.of(CD_Double, CD_double));
				yield codeBuilder.dload(parameterSlot).invokestatic(doubleValueOfMethod);
			}
		};
	}

	private static boolean isProxyableMethod(MethodInformation methodInformation) {
		return !methodInformation.isFinal() && !Modifier.isPrivate(methodInformation.modifiers());
	}

	private static void createConstructor(MethodBuilder methodBuilder, ClassDesc classDesc,
	                                      ClassDesc superClassDescriptor,
	                                      ConstructorInformation<?> superClassConstructor) {
		FieldRefEntry invokerFactoryField =
				methodBuilder.constantPool().fieldRefEntry(classDesc, INVOKER_FACTORY_FIELD_NAME, FUNCTION_CLASS_DESCRIPTOR);

		MethodTypeDesc superClassConstructorDescriptor = getSuperClassConstructorDescriptor(superClassConstructor);
		methodBuilder.withCode(codeBuilder -> {
			codeBuilder.aload(0);

			superClassConstructor.parameterInformation().forEach(parameter -> {
				switch (parameter.type()) {
					case PrimitiveTypeInstance primitiveType -> {
						switch (primitiveType.primitiveKind()) {
							case BOOLEAN, BYTE, SHORT, INT, CHAR -> codeBuilder.iconst_0();
							case LONG -> codeBuilder.lconst_0();
							case FLOAT -> codeBuilder.fconst_0();
							case DOUBLE -> codeBuilder.dconst_0();
						}
					}
					default -> codeBuilder.aconst_null();
				}
			});
			codeBuilder.invokespecial(superClassDescriptor, INIT_NAME, superClassConstructorDescriptor)
			           .aload(0)
			           .aload(1)
			           .putfield(invokerFactoryField)
			           .return_();
		});
	}

	private static MethodTypeDesc getSuperClassConstructorDescriptor(ConstructorInformation<?> superClassConstructor) {
		if (superClassConstructor.parameterInformation().isEmpty()) {
			return MTD_void;
		}

		List<ClassDesc> parameterDescriptors = superClassConstructor.parameterInformation()
		                                                            .stream()
		                                                            .map(parameter -> ClassDesc.ofDescriptor(
				                                                            parameter.type().descriptorString()))
		                                                            .toList();
		return MethodTypeDesc.of(CD_void, parameterDescriptors);
	}

	private static ClassDesc createClassDescriptor(ClassInformation<?> classInformation) {
		return ClassDesc.of(classInformation.name());
	}

	private static ClassDesc createClassDescriptor(Class<?> clazz) {
		return ClassDesc.of(clazz.getPackageName(), clazz.getSimpleName());
	}
}
