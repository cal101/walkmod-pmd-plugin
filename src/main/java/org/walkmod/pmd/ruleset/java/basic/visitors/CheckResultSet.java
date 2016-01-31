package org.walkmod.pmd.ruleset.java.basic.visitors;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolDefinition;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.pmd.visitors.AbstractPMDRuleVisitor;

@RequiresSemanticAnalysis
public class CheckResultSet<T> extends AbstractPMDRuleVisitor<T> {

   @Override
   public void visit(MethodDeclaration md, T ctx) {
      List<Parameter> params = md.getParameters();
      if (params != null) {
         Iterator<Parameter> it = params.iterator();
         while (it.hasNext()) {
            Parameter current = it.next();
            Type type = current.getType();
            SymbolData sd = type.getSymbolData();

            if (sd != null) {
               if (sd.getClazz().isAssignableFrom(ResultSet.class)) {
                  updateStmts(current);
               }
            }
         }
      }
      super.visit(md, ctx);
   }

   private void updateStmts(SymbolDefinition symbolDef) {
      List<SymbolReference> usages = symbolDef.getUsages();
      if (usages != null) {
         Iterator<SymbolReference> itUsages = usages.iterator();
         boolean updated = false;
         while (itUsages.hasNext() && !updated) {
            SymbolReference usage = itUsages.next();
            Node parent = ((Node) usage).getParentNode();
            if (parent instanceof MethodCallExpr) {
               MethodCallExpr mce = (MethodCallExpr) parent;
               if (mce.getName().equals("next")) {
                  Node grandParent = mce.getParentNode();
                  if (grandParent instanceof ExpressionStmt) {
                     Node grandGrandParent = grandParent.getParentNode();
                     if (grandGrandParent instanceof BlockStmt) {
                        if (itUsages.hasNext()) {
                           BlockStmt block = (BlockStmt) grandGrandParent;
                           List<Statement> stmts = new LinkedList<Statement>(block.getStmts());
                           List<Statement> pendingBock = new LinkedList<Statement>();
                           Iterator<Statement> itStmt = stmts.iterator();
                           boolean removed = false;
                           while (!removed && itStmt.hasNext()) {
                              Statement currentStmt = itStmt.next();
                              if (currentStmt == grandParent) {
                                 if (itStmt.hasNext()) {
                                    itStmt.remove();
                                    while (itStmt.hasNext()) {
                                       pendingBock.add(itStmt.next());
                                       itStmt.remove();
                                    }
                                    removed = true;
                                 }
                              }
                           }
                           if (removed) {
                              stmts.add(new IfStmt(mce, new BlockStmt(pendingBock), null));
                           }
                           block.setStmts(stmts);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void visit(VariableDeclarationExpr vd, T ctx) {
      Type type = vd.getType();
      SymbolData sd = type.getSymbolData();

      if (sd != null) {
         if (sd.getClazz().isAssignableFrom(ResultSet.class)) {
            List<VariableDeclarator> vars = vd.getVars();

            if (vars != null) {
               Iterator<VariableDeclarator> it = vars.iterator();

               while (it.hasNext()) {
                  VariableDeclarator currentVar = it.next();

                  updateStmts(currentVar);
               }
            }
         }
      }
      super.visit(vd, ctx);
   }
}