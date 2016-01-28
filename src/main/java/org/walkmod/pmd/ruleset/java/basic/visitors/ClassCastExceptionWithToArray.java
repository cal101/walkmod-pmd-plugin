package org.walkmod.pmd.ruleset.java.basic.visitors;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.expr.ArrayCreationExpr;
import org.walkmod.javalang.ast.expr.CastExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.QualifiedNameExpr;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.pmd.visitors.AbstractPMDRuleVisitor;

@RequiresSemanticAnalysis
public class ClassCastExceptionWithToArray<A> extends AbstractPMDRuleVisitor<A> {

   @Override
   public void visit(MethodCallExpr n, A ctx) {
      String name = n.getName();
      if (name.equals("toArray")) {
         List<Expression> args = n.getArgs();
         if (args == null || args.isEmpty()) {
            Node parentNode = n.getParentNode();
            if (parentNode != null) {
               if (parentNode instanceof CastExpr) {

                  CastExpr cast = (CastExpr) parentNode;

                  Type castedType = cast.getType();
                  if (castedType instanceof ReferenceType) {
                     Type arrayType = ((ReferenceType) castedType).getType();
                     Expression scope = n.getScope();

                     if (scope instanceof NameExpr) {
                        if (!(scope instanceof QualifiedNameExpr)) {

                           try {
                              NameExpr scopeForSizeOp = (NameExpr) ASTManager.parse(NameExpr.class, scope.toString());
                              MethodSymbolData msd = n.getSymbolData();
                              if (msd != null) {
                                 Method method = msd.getMethod();
                                 if (method != null) {
                                    Class<?> clazz = method.getDeclaringClass();
                                    if (Collection.class.isAssignableFrom(clazz)) {
                                       args = new LinkedList<Expression>();
                                       List<Expression> dimensions = new LinkedList<Expression>();
                                       dimensions.add(new MethodCallExpr(scopeForSizeOp, "size"));
                                       args.add(new ArrayCreationExpr(arrayType, dimensions, 0));
                                       n.setArgs(args);
                                    }
                                 }
                              }
                           } catch (ParseException e) {
                              throw new RuntimeException(e);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      super.visit(n, ctx);
   }
}