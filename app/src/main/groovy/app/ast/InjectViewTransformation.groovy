//package app.ast
//
//import android.view.View
//import groovyjarjarasm.asm.Opcodes
//import org.codehaus.groovy.ast.*
//import org.codehaus.groovy.ast.builder.AstBuilder
//import org.codehaus.groovy.ast.stmt.BlockStatement
//import org.codehaus.groovy.ast.stmt.Statement
//import org.codehaus.groovy.control.CompilePhase
//import org.codehaus.groovy.control.SourceUnit
//import org.codehaus.groovy.transform.ASTTransformation
//import org.codehaus.groovy.transform.GroovyASTTransformation
//
///**
// * Created by Arasthel on 16/08/14.
// */
//@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
//public class InjectViewTransformation implements ASTTransformation, Opcodes {
//
//    @Override
//    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
////        FieldNode annotatedField = astNodes[1];
////        AnnotationNode annotation = astNodes[0];
//
//        def annotatedField = astNodes[1] as FieldNode;
//        def annotation = astNodes[0] as AnnotationNode;
//
//        ClassNode declaringClass = annotatedField.declaringClass;
//
//        if (!AnnotationUtils.isSubtype(annotatedField.getType(), View.class)) {
//            throw new Exception("Annotated field must extend View class. Type: " +
//                    "${annotatedField.type.name}");
//        }
//
//        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass);
//
//        Variable viewParameter = injectMethod.parameters.first()
//
//        String id = null;
//
//        if (annotation.members.size() > 0) {
//            id = annotation.members.value.property.getValue();
//        }
//
//        if (id == null) {
//            id = annotatedField.name;
//        }
//
//        Statement statement = AnnotationUtils.createInjectExpression(annotatedField,
//                viewParameter, id)
//
//        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();
//        statementList.add(statement);
//
//        def superCall = new AstBuilder().buildFromString("super.injectViews()")[0] as Statement
//        def superClass = declaringClass.superClass
//        MethodNode parentInjectMethod = AnnotationUtils.getInjectViewsMethod(superClass);
//        if (parentInjectMethod && !statementList.contains(superCall)) {
//            statementList.add(0, superCall)
//        }
//    }
//
//
//    void autoInject() {
//
//    }
//
//}
