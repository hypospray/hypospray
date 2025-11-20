package eu.jbeernink.hypospray.model;

import static eu.jbeernink.hypospray.model.todo.Todo.warnNotYetImplemented;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.System.Logger;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ClassType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;
import jakarta.enterprise.lang.model.types.Type;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.TypeVariableOwner;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveMethodInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.ArrayTypeInstance;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.DeclaredTypeVariableInstance;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;
import eu.jbeernink.hypospray.model.types.VoidTypeInstance;
import eu.jbeernink.hypospray.model.types.WildcardTypeInstance;
import eu.jbeernink.hypospray.model.types.lazy.LazyTypeVariableInstance;

/// Type factory for creating [TypeInstance] instances.
///
/// This factory implements the [Types] interface so that it can be used by any
/// [BuildCompatibleExtension] instances during container
/// initialization.
@NullMarked
public class TypeFactory implements Types {

	private static final Logger logger = System.getLogger(TypeFactory.class.getName());

	private static final TypeFactory INSTANCE = new TypeFactory();

	private final ClassInformationSource classInformationSource = ClassInformationSource.getInstance();

	public static TypeFactory getInstance() {
		return INSTANCE;
	}

	@Override
	public TypeInstance of(Class<?> clazz) {
		if (clazz.equals(Void.TYPE) || clazz.equals(Void.class)) {
			return ofVoid();
		}

		if (clazz.isPrimitive()) {
			return ofPrimitive(PrimitiveKind.valueOf(clazz.getSimpleName().toUpperCase(ROOT)));
		}

		if (clazz.isArray()) {
			return asArray(clazz.getComponentType(), 1);
		}

		return requireNonNull(ofClass(clazz.getName()));
	}

	private ArrayTypeInstance asArray(Class<?> componentType, int dimensions) {
		if (componentType.isArray()) {
			return asArray(componentType.componentType(), dimensions + 1);
		}

		return ofArray(of(componentType), dimensions);
	}

	public ClassTypeInstance ofObject() {
		return requireNonNull(ofClass(Object.class.getName()));
	}

	@Override
	public VoidTypeInstance ofVoid() {
		return VoidTypeInstance.VOID;
	}

	@Override
	public PrimitiveTypeInstance ofPrimitive(PrimitiveKind kind) {
		return switch (kind) {
			case BOOLEAN -> PrimitiveTypeInstance.BOOLEAN;
			case BYTE -> PrimitiveTypeInstance.BYTE;
			case SHORT -> PrimitiveTypeInstance.SHORT;
			case INT -> PrimitiveTypeInstance.INT;
			case LONG -> PrimitiveTypeInstance.LONG;
			case FLOAT -> PrimitiveTypeInstance.FLOAT;
			case DOUBLE -> PrimitiveTypeInstance.DOUBLE;
			case CHAR -> PrimitiveTypeInstance.CHAR;
		};
	}

	@Override
	public @Nullable ClassTypeInstance ofClass(String name) {
		try {
			ClassInformation<?> info = classInformationSource.getClassInformation(Class.forName(name));

			return ofClass(info);
		} catch (ClassNotFoundException e) {
			logger.log(Logger.Level.WARNING, String.format("Unable to load class %s:", name), e);
			return null;
		}
	}

	@Override
	public ClassTypeInstance ofClass(ClassInfo clazz) {
		return switch (clazz) {
			case ClassInformation<?> info -> new ClassTypeInstance(info, List.of());
			case ClassInfo info -> throw new IllegalArgumentException(
					"Unsupported ClassInfo type, only ClassInfo types created by Hypospray are supported: " +
					info.getClass().getName());
		};
	}

	@Override
	public ArrayTypeInstance ofArray(Type elementType, int dimensions) {
		return switch (elementType) {
			case TypeInstance typeInstance -> ofArray(typeInstance, dimensions);
			case Type type -> throw newUnsupportedTypeInstanceException(type);
		};
	}

	private ArrayTypeInstance ofArray(TypeInstance elementType, int dimensions) {
		if (elementType.isArray()) {
			throw new IllegalArgumentException(
					"ElementType of an array cannot be an array itself, please use the dimensions parameter to indicate a nested array instead.");
		}

		if (dimensions <= 0) {
			throw new IllegalArgumentException(
					String.format("The dimensions of an array must be 1 or greater: %d.", dimensions));
		}

		if (dimensions == 1) {
			return new ArrayTypeInstance(elementType, List.of());
		}

		if (elementType.isWildcardType()) {
			throw new IllegalArgumentException("Wildcards are not supported as the element types of an array.");
		}

		return new ArrayTypeInstance(ofArray(elementType, dimensions - 1), List.of());
	}

	@Override
	public ParameterizedTypeInstance parameterized(Class<?> genericType, Class<?>... typeArguments) {
		int actualNumberOfGenericTypeParameters = genericType.getTypeParameters().length;
		if (actualNumberOfGenericTypeParameters == 0) {
			throw new IllegalArgumentException(
					String.format("Class does not have any generic type parameters: %s", genericType.getName()));
		}
		if (actualNumberOfGenericTypeParameters != typeArguments.length) {
			throw new IllegalArgumentException(
					String.format("Incorrect number of generic type parameters, %s has %d, but %d were specified.",
							genericType.getName(), actualNumberOfGenericTypeParameters, typeArguments.length));
		}

		return new ParameterizedTypeInstance(of(genericType).asClass(), Arrays.stream(typeArguments).map(this::of).toList(),
				List.of());
	}

	@Override
	public ParameterizedTypeInstance parameterized(Class<?> genericType, Type... typeArguments) {
		return parameterized(requireNonNull(ofClass(genericType.getName())), typeArguments);
	}

	@Override
	public ParameterizedTypeInstance parameterized(ClassType genericType, Type... typeArguments) {
		if (genericType instanceof ClassTypeInstance genericTypeInstance) {
			ClassInformation<?> declaration = genericTypeInstance.declaration();
			if (declaration.typeParameters().isEmpty()) {
				throw new IllegalArgumentException(
						"Class does not have any generic type parameters: " + genericType.declaration().name());
			}

			if (declaration.typeParameters().size() != typeArguments.length) {
				throw new IllegalArgumentException(
						String.format("Incorrect number of generic type parameters, %s has %d, but %d were specified.",
								declaration.name(), declaration.typeParameters().size(), typeArguments.length));
			}
			List<TypeInstance> typeArgumentInstances = Arrays.stream(typeArguments).map(TypeFactory::asTypeInstance).toList();

			return new ParameterizedTypeInstance(genericTypeInstance, typeArgumentInstances, List.of());
		}

		throw newUnsupportedTypeInstanceException(genericType);
	}

	public TypeVariableInstance typeVariable(TypeVariableOwner owner, TypeVariable<?> typeVariable) {
		var typeVariableMapping = new HashMap<TypeVariable<?>, TypeVariableInstance>();
		var typeVariableReference = new LateReference<TypeVariableInstance>();
		typeVariableMapping.put(typeVariable, new LazyTypeVariableInstance(typeVariableReference));

		List<TypeInstance> bounds = Arrays.stream(typeVariable.getAnnotatedBounds())
		                                  .map(annotatedType -> TypeFactory.getInstance()
		                                                                   .fromAnnotatedType(annotatedType,
				                                                                   typeVariableMapping))
		                                  .toList();

		List<AnnotationInformation> annotations = Arrays.stream(typeVariable.getAnnotations())
		                                                .map(ReflectiveAnnotationInformation::new)
		                                                .collect(toUnmodifiableList());

		TypeVariableInstance typeVariableInstance = typeVariable(owner, typeVariable.getName(), bounds, annotations);
		typeVariableReference.setValue(typeVariableInstance);
		return typeVariableInstance;
	}

	public TypeVariableInstance typeVariable(TypeVariableOwner owner, String name, List<TypeInstance> bounds,
	                                         List<AnnotationInformation> annotations) {
		return new DeclaredTypeVariableInstance(owner, name, bounds, annotations);
	}

	@Override
	public WildcardTypeInstance wildcardWithUpperBound(Type upperBound) {
		return switch (upperBound) {
			case TypeInstance typeInstance -> new WildcardTypeInstance(List.of(typeInstance), List.of(), List.of());
			case Type type -> throw newUnsupportedTypeInstanceException(type);
		};
	}

	@Override
	public WildcardTypeInstance wildcardWithLowerBound(Type lowerBound) {
		return switch (lowerBound) {
			case TypeInstance typeInstance -> new WildcardTypeInstance(List.of(), List.of(typeInstance), List.of());
			case Type type -> throw newUnsupportedTypeInstanceException(type);
		};
	}

	@Override
	public WildcardTypeInstance wildcardUnbounded() {
		return new WildcardTypeInstance(List.of(), List.of(), List.of());
	}

	public TypeInstance fromAnnotatedType(AnnotatedType annotatedType) {
		return fromAnnotatedType(annotatedType, new HashMap<>());
	}

	private TypeInstance fromAnnotatedType(AnnotatedType annotatedType,
	                                       Map<TypeVariable<?>, TypeVariableInstance> typeVariableMapping) {
		return switch (annotatedType) {
			case AnnotatedArrayType annotatedArrayType -> fromAnnotatedArrayType(annotatedArrayType, typeVariableMapping);
			case AnnotatedParameterizedType parameterizedType ->
					fromAnnotatedParameterizedType(parameterizedType, typeVariableMapping);
			case AnnotatedWildcardType wildcardType -> fromAnnotatedWildcardType(wildcardType);
			case AnnotatedTypeVariable typeVariable -> fromAnnotatedTypeVariable(typeVariable, typeVariableMapping);
			case AnnotatedType _ ->
					fromJavaType(annotatedType.getType()).withTypeAnnotations(getTypeAnnotations(annotatedType));
		};
	}

	private ArrayTypeInstance fromAnnotatedArrayType(AnnotatedArrayType annotatedArrayType,
	                                                 Map<TypeVariable<?>, TypeVariableInstance> typeVariableMapping) {
		return new ArrayTypeInstance(fromAnnotatedType(annotatedArrayType.getAnnotatedGenericComponentType()),
				Arrays.stream(annotatedArrayType.getAnnotations())
				      .map(ReflectiveAnnotationInformation::new)
				      .collect(toUnmodifiableList()));
	}

	private TypeVariableInstance fromAnnotatedTypeVariable(AnnotatedTypeVariable typeVariable,
	                                                       Map<TypeVariable<?>, TypeVariableInstance> typeVariableMapping) {
		var rawTypeVariable = (TypeVariable<?>) typeVariable.getType();
		if (typeVariableMapping.containsKey(rawTypeVariable)) {
			return typeVariableMapping.get(rawTypeVariable);
		}

		var typeVariableReference = new LateReference<TypeVariableInstance>();
		typeVariableMapping.put(rawTypeVariable, new LazyTypeVariableInstance(typeVariableReference));

		TypeVariableOwner owner = getTypeVariableOwner(rawTypeVariable);

		List<TypeInstance> bounds = new ArrayList<>();
		for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
			TypeInstance typeInstance = fromAnnotatedType(bound, typeVariableMapping);
			bounds.add(typeInstance);
		}

		List<AnnotationInformation> annotations = Arrays.stream(typeVariable.getAnnotations())
		                                                .map(ReflectiveAnnotationInformation::new)
		                                                .collect(toUnmodifiableList());


		return new DeclaredTypeVariableInstance(owner, rawTypeVariable.getName(), bounds, annotations);
	}

	private ParameterizedTypeInstance fromAnnotatedParameterizedType(AnnotatedParameterizedType parameterizedType,
	                                                                 Map<TypeVariable<?>, TypeVariableInstance> typeVariableMapping) {
		ClassTypeInstance rawType = fromJavaType(((ParameterizedType) parameterizedType.getType()).getRawType()).asClass();

		List<AnnotationInformation> typeAnnotations = getTypeAnnotations(parameterizedType);

		List<TypeInstance> typeArguments = new ArrayList<>();
		for (AnnotatedType annotatedType : parameterizedType.getAnnotatedActualTypeArguments()) {
			TypeInstance typeInstance = fromAnnotatedType(annotatedType, typeVariableMapping);
			typeArguments.add(typeInstance);
		}

		return new ParameterizedTypeInstance(rawType.asClass(), typeArguments, typeAnnotations);
	}

	private TypeInstance fromAnnotatedWildcardType(AnnotatedWildcardType wildcardType) {
		List<TypeInstance> upperBounds =
				Arrays.stream(wildcardType.getAnnotatedUpperBounds()).map(this::fromAnnotatedType).toList();

		List<TypeInstance> lowerBounds =
				Arrays.stream(wildcardType.getAnnotatedLowerBounds()).map(this::fromAnnotatedType).toList();

		if (!lowerBounds.isEmpty() && upperBounds.stream().allMatch(TypeFactory::isDefaultUpperBound)) {
			upperBounds = List.of();
		}

		return new WildcardTypeInstance(upperBounds, lowerBounds, getTypeAnnotations(wildcardType));
	}

	public TypeInstance fromJavaType(java.lang.reflect.Type type) {
		return switch (type) {
			case Class<?> clazz -> of(clazz);
			case TypeVariable<?> typeVariable -> getTypeVariableInstance(typeVariable);
			case ParameterizedType parameterizedType -> {
				List<TypeInstance> typeParameters =
						Arrays.stream(parameterizedType.getActualTypeArguments()).map(this::fromJavaType).toList();

				yield new ParameterizedTypeInstance(fromJavaType(parameterizedType.getRawType()).asClass(), typeParameters,
						List.of());
			}
			case WildcardType wildcardType -> {
				List<TypeInstance> upperBounds = Arrays.stream(wildcardType.getUpperBounds()).map(this::fromJavaType).toList();
				List<TypeInstance> lowerBounds = Arrays.stream(wildcardType.getLowerBounds()).map(this::fromJavaType).toList();

				if (!lowerBounds.isEmpty() && upperBounds.stream().allMatch(TypeFactory::isDefaultUpperBound)) {
					upperBounds = List.of();
				}

				yield new WildcardTypeInstance(upperBounds, lowerBounds, List.of());
			}
			case java.lang.reflect.Type _ -> {
				warnNotYetImplemented();
				yield ofObject();
			}
		};
	}

	private DeclaredTypeVariableInstance getTypeVariableInstance(TypeVariable<?> typeVariable) {
		LazyTypeVariableInstance lazyTypeVariableInstance = new LazyTypeVariableInstance(new LateReference<>());
		var typeVariableMapping = new HashMap<TypeVariable<?>, TypeVariableInstance>();
		typeVariableMapping.put(typeVariable, lazyTypeVariableInstance);
		TypeVariableOwner owner = getTypeVariableOwner(typeVariable);

		List<TypeInstance> bounds = Stream.of(typeVariable.getAnnotatedBounds())
		                                  .map(annotatedBound -> fromAnnotatedType(annotatedBound, typeVariableMapping))
		                                  .toList();
		List<AnnotationInformation> annotations = Arrays.stream(typeVariable.getAnnotations())
		                                                .map(ReflectiveAnnotationInformation::new)
		                                                .collect(toUnmodifiableList());

		DeclaredTypeVariableInstance typeVariableInstance =
				new DeclaredTypeVariableInstance(owner, typeVariable.getName(), bounds, annotations);
		lazyTypeVariableInstance.typeVariable().setValue(typeVariableInstance);

		return typeVariableInstance;
	}

	private TypeVariableOwner getTypeVariableOwner(TypeVariable<?> typeVariable) {
		TypeVariableOwner owner = switch (typeVariable.getGenericDeclaration()) {
			case Class<?> clazz -> classInformationSource.getClassInformation(clazz);
			case Method method -> new ReflectiveMethodInformation(method);
			case Object o -> throw new IllegalArgumentException("Unsupported type variable owner: " + o.getClass());
		};
		return owner;
	}

	private TypeFactory() {
	}

	private static TypeInstance asTypeInstance(Type type) {
		return switch (type) {
			case TypeInstance typeInstance -> typeInstance;
			case Type _ -> throw newUnsupportedTypeInstanceException(type);
		};
	}

	private static IllegalArgumentException newUnsupportedTypeInstanceException(Type type) {
		return new IllegalArgumentException(
				"Unsupported Type instance, only Type instances created by the TypeFactory are supported: " +
				type.getClass().getName());
	}

	private static boolean isDefaultUpperBound(TypeInstance upperBound) {
		return switch (upperBound) {
			case ClassTypeInstance classType ->
					classType.declaration().name().equals(Object.class.getName()) && classType.typeAnnotations().isEmpty();
			case TypeInstance _ -> false;
		};
	}

	private static List<AnnotationInformation> getTypeAnnotations(AnnotatedType annotatedType) {
		return Arrays.stream(annotatedType.getAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}
}
