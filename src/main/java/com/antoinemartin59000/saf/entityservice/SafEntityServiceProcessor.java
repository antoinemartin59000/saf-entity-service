package com.antoinemartin59000.saf.entityservice;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("*") // Run on every compilation round
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SafEntityServiceProcessor extends AbstractProcessor {

    private static final String ENTITY_FQCN = SafEntityService.class.getName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        List<TypeElement> classElements = new ArrayList<>();
        for (Element element : roundEnv.getRootElements()) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                if (extendsEntity(classElement)) {
                    classElements.add(classElement);
                }
            }
        }

        if (classElements.isEmpty()) {
            return true;
        }

        try {
            generateHolder(classElements);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private boolean extendsEntity(TypeElement classElement) {
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) superclass;
        TypeElement superElement = (TypeElement) declaredType.asElement();
        return ENTITY_FQCN.equals(superElement.getQualifiedName().toString());
    }

    private void generateHolder(List<TypeElement> classElements) throws IOException {
        String className = SafEntityService.class.getSimpleName() + "Holder";
        String searchName = className + "Search";
        String packageName = processingEnv.getElementUtils().getPackageOf(classElements.get(0)).getQualifiedName().toString();

        JavaFileObject searchFile = processingEnv.getFiler().createSourceFile(packageName + "." + searchName);
        try (Writer writer = searchFile.openWriter()) {

            writer.write("package " + packageName + ";\n\n");
            String startClass = """
                    import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.antoinemartin59000.saf.entity.SafEntity;
import com.antoinemartin59000.saf.entity.SafEntitySearch;
import com.antoinemartin59000.saf.entitydao.SafEntityDao;
import com.antoinemartin59000.saf.entitydao.SafEntityDaoProvider;

public class SafEntityServiceProvider {

    private static final Map<Class<? extends SafEntity>, Class<? extends SafEntityService>> MAP = new HashMap<>();

    static {

        // Configure Reflections to scan the desired package(s)
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(""))
                .setExpandSuperTypes(false) // faster, we only care about direct subclasses
        );

        // Get ALL classes that extend SafEntityService (including indirect subclasses)
        Set<Class<? extends SafEntityService>> subClasses = reflections.getSubTypesOf(SafEntityService.class);

        for (Class<? extends SafEntityService> subClass : subClasses) {
            // Skip interfaces and abstract classes if you only want concrete implementations
            if (subClass.isInterface() || java.lang.reflect.Modifier.isAbstract(subClass.getModifiers())) {
                continue;
            }

            // Get the generic superclass (SafEntityService<E, S>)
            Type genericSuper = subClass.getGenericSuperclass();

            if (genericSuper instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();

                if (typeArgs.length == 0) {
                    continue;
                }

                Type eType = typeArgs[0];

                if (eType instanceof Class eClass) {
                    MAP.put(eClass, subClass);
                }

            }
        }
    }

    private final Map<Class<? extends SafEntity>, SafEntityService<?, ?>> safEntityServices = new HashMap<>();

    public SafEntityServiceProvider(String cacheHost, int cachePort, Set<Class<?>> localCache) {

        SafEntityDaoProvider safEntityDaoProvider = new SafEntityDaoProvider(cacheHost, cachePort, localCache);

        for (Map.Entry<Class<? extends SafEntity>, Class<? extends SafEntityService>> entry : MAP.entrySet()) {
            Class<? extends SafEntity> safEntityClass = entry.getKey();
            Class<? extends SafEntityService> safEntityServiceClass = entry.getValue();

            SafEntityDao<?, ?> safEntityDao = safEntityDaoProvider.getEntityDao(safEntityClass);

            SafEntityService safEntityService;
            try {
                safEntityService = (SafEntityService) safEntityServiceClass.getConstructors()[0].newInstance(safEntityDao);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
                throw new RuntimeException(e);
            }

            safEntityServices.put(safEntityClass, safEntityService);
        }

    }

    public <E extends SafEntity, S extends SafEntitySearch, T extends SafEntityService<E, S>> T get(Class<E> safEntityClass) {
        return (T) safEntityServices.get(safEntityClass);
    }
                                        """;

            writer.write(startClass);

            for (TypeElement typeElement : classElements) {
                writer.write("public " + typeElement.getSimpleName().toString() + " get" + typeElement.getSimpleName().toString() + "Service() {\n");
                writer.write("    return get(" + typeElement.getSimpleName().toString() + ".class);\n");
                writer.write("}\n");

            }
            writer.write("\n");

            writer.write("}\n");
        }

    }

}
