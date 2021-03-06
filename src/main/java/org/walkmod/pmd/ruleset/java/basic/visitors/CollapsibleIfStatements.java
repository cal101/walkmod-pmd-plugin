/*
 * Copyright (C) 2016 Raquel Pau.
 *
 * Walkmod is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Walkmod is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Walkmod. If not, see <http://www.gnu.org/licenses/>.
 */
package org.walkmod.pmd.ruleset.java.basic.visitors;

import java.util.List;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.pmd.visitors.Modification;
import org.walkmod.pmd.visitors.PMDRuleVisitor;

@Modification
public class CollapsibleIfStatements extends PMDRuleVisitor {

    @Override
    public void visit(IfStmt n, Node node) {
        super.visit(n, node);
        n = (IfStmt) node;

        Node parent = n.getParentNode();

        if (parent != null) {
            if (parent instanceof BlockStmt) {
                parent = parent.getParentNode();
            }
            if (parent instanceof IfStmt) {
                IfStmt parentIf = (IfStmt) parent;

                Statement elseStmt = parentIf.getElseStmt();
                if (elseStmt == null) {

                    Statement thisElseStmt = n.getElseStmt();
                    if (thisElseStmt == null) {

                        Expression rightExpression = parentIf.getCondition();
                        if (rightExpression instanceof BinaryExpr) {
                            rightExpression = new EnclosedExpr(parentIf.getCondition());
                        }

                        Expression leftExpression = n.getCondition();
                        if (leftExpression instanceof BinaryExpr) {
                            leftExpression = new EnclosedExpr(n.getCondition());
                        }
                        try {
                            BinaryExpr condition = new BinaryExpr(rightExpression.clone(), leftExpression.clone(),
                                    BinaryExpr.Operator.and);

                            if (parentIf.getThenStmt() == n) {
                                try {
                                    parentIf.setThenStmt(n.getThenStmt().clone());
                                    parentIf.setCondition(condition);
                                } catch (CloneNotSupportedException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Statement stmt = parentIf.getThenStmt();
                                if (stmt instanceof BlockStmt) {
                                    BlockStmt block = (BlockStmt) stmt;
                                    List<Statement> stmts = block.getStmts();
                                    if (stmts.size() == 1) {

                                        parentIf.setThenStmt(n.getThenStmt().clone());
                                        parentIf.setCondition(condition);
                                    }
                                }
                            }
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
