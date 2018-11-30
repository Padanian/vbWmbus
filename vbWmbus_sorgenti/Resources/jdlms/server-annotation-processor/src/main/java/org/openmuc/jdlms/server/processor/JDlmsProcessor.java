package org.openmuc.jdlms.server.processor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;

import com.google.auto.service.AutoService;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public class JDlmsProcessor extends AbstractProcessor {

    public JDlmsProcessor() {
        super();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();

        types.add(CosemClass.class.getCanonicalName());
        types.add(CosemMethod.class.getCanonicalName());
        types.add(CosemAttribute.class.getCanonicalName());

        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> cosemObjectss = roundEnv.getElementsAnnotatedWith(CosemClass.class);

        for (Element cosemObject : cosemObjectss) {
            checkCosemClass(cosemObject);
        }
        return true;
    }

    private boolean typeIsSubtypeOfCosemSnInterfaceObject(Element enclosingElement) {
        return typeIsOfClass(CosemSnInterfaceObject.class, enclosingElement.asType());
    }

    private boolean typeIsOfClass(Class<?> clazz, TypeMirror type) {
        TypeMirror snType = processingEnv.getElementUtils().getTypeElement(clazz.getName()).asType();

        return processingEnv.getTypeUtils().isSubtype(type, snType);
    }

    private void checkCosemClass(Element classElement) {

        CosemClass cosemClass = classElement.getAnnotation(CosemClass.class);

        if (cosemClass.id() < 0) {
            error(classElement, "COSEM class ID must be positive.");
        }
        else if (cosemClass.id() > 0xFFFF) {
            error(classElement, "COSEM class ID be in range from uint16.");
        }

        if (cosemClass.version() < 0) {
            error(classElement, "COSEM class version must be positive.");
        }

        List<? extends Element> annotatedElementsInClass = classElement.getEnclosedElements();

        Set<Byte> attributes = new HashSet<>();
        Set<Byte> methods = new HashSet<>();
        Set<Integer> shortNameOffset = new HashSet<>();

        boolean subtypeOfCosemSnInterfaceObject = typeIsSubtypeOfCosemSnInterfaceObject(classElement);

        for (Element attributeOrMethod : annotatedElementsInClass) {
            CosemAttribute cosemAttribute = attributeOrMethod.getAnnotation(CosemAttribute.class);
            CosemMethod cosemMethod = attributeOrMethod.getAnnotation(CosemMethod.class);

            if (cosemAttribute != null) {
                checkAttributes(attributes, shortNameOffset, subtypeOfCosemSnInterfaceObject, attributeOrMethod,
                        cosemAttribute);
            }
            else if (cosemMethod != null) {
                checkMethods(methods, shortNameOffset, subtypeOfCosemSnInterfaceObject, attributeOrMethod, cosemMethod);
            }
        }

    }

    private void checkMethods(Set<Byte> methods, Set<Integer> shortNameOffset, boolean subtypeOfCosemSnInterfaceObject,
            Element attributeOrMethod, CosemMethod cosemMethod) {
        if (!methods.add(cosemMethod.id())) {
            error(attributeOrMethod, "Method ID not unique in class.");
        }

        ExecutableType type = (ExecutableType) attributeOrMethod.asType();

        TypeMirror returnType = type.getReturnType();

        boolean rTypeIsDO = typeIsOfClass(DataObject.class, returnType);
        boolean rTypeIsVoid = returnType.getKind() == TypeKind.VOID;

        if (!rTypeIsDO && !rTypeIsVoid) {
            error(attributeOrMethod, "Return type may only be %s or void.", DataObject.class.getSimpleName());
        }

        List<? extends TypeMirror> parameterTypes = type.getParameterTypes();

        if (parameterTypes.isEmpty() && cosemMethod.consumes() != DataObject.Type.DONT_CARE) {
            error(attributeOrMethod, "Specify at least one parameter.");
        }
        else if (parameterTypes.size() == 1) {
            TypeMirror t = parameterTypes.get(0);

            boolean conIdTypeCorrect = typeIsLong(t);

            boolean valTypeCorrect = typeIsDataObject(t);

            if (!conIdTypeCorrect && !valTypeCorrect) {
                error(attributeOrMethod, "Parameters may only be of type %s and long/Long.",
                        DataObject.class.getSimpleName());
            }
        }
        else if (parameterTypes.size() == 2) {
            TypeMirror valType = parameterTypes.get(0);

            TypeMirror conIdType = parameterTypes.get(1);
            boolean conIdTypeCorrect = typeIsLong(conIdType);

            boolean valTypeCorrect = typeIsDataObject(valType);

            if (!valTypeCorrect || !conIdTypeCorrect) {
                error(attributeOrMethod, "Parameters may only be of type %s and long/Long.",
                        DataObject.class.getSimpleName());
            }

        }
        else if (parameterTypes.size() > 2) {
            error(attributeOrMethod, "Method has too many parameters.");
        }

        checkSnOffset(shortNameOffset, subtypeOfCosemSnInterfaceObject, cosemMethod.snOffset(), attributeOrMethod);
    }

    private void checkAttributes(Set<Byte> attributes, Set<Integer> shortNameOffset,
            boolean subtypeOfCosemSnInterfaceObject, Element attributeOrMethod, CosemAttribute cosemAttribute) {
        int attributeId = cosemAttribute.id();
        if (attributeId == 1) {
            error(attributeOrMethod, "COSEM Attribute ID 1 is reserved for the system.");
        }

        if (!attributes.add(cosemAttribute.id())) {
            error(attributeOrMethod, "Attribute ID not unique in class.");
        }

        TypeMirror fieldType = attributeOrMethod.asType();

        String fullTypeClassName = fieldType.toString();

        if (!DataObject.class.getCanonicalName().equals(fullTypeClassName)) {
            error(attributeOrMethod, "Type of a COSEM must be of type %s.", DataObject.class.getSimpleName());
        }

        checkSnOffset(shortNameOffset, subtypeOfCosemSnInterfaceObject, cosemAttribute.snOffset(), attributeOrMethod);
    }

    private boolean typeIsDataObject(TypeMirror valType) {
        return typeIsOfClass(DataObject.class, valType);
    }

    private boolean typeIsLong(TypeMirror conIdType) {
        return conIdType.getKind() == TypeKind.LONG || typeIsOfClass(Long.class, conIdType);
    }

    private void checkSnOffset(Set<Integer> shortNameOffset, boolean subtypeOfCosemSnInterfaceObject, int snOffset,
            Element element) {

        if (subtypeOfCosemSnInterfaceObject) {
            if (snOffset < 0) {
                error(element, "SN offset must be specified.");
            }
            else if ((snOffset % 0x08) != 0) {
                error(element, "SN offset must be a multiple of 8.");
            }
            else if (!shortNameOffset.add(snOffset)) {
                error(element, "SN offset duplication: 0x%02x.", snOffset);
            }
        }
        else {
            if (snOffset != -1) {
                error(element, "SN offset must not be specified.");
            }
        }
    }

    private void error(Element element, String message, Object... args) {
        msg(element, message, Kind.ERROR, args);
    }

    private void msg(Element element, String message, Kind level, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(level, message, element);
    }
}
