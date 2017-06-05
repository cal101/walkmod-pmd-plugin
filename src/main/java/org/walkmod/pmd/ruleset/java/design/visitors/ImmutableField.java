package org.walkmod.pmd.ruleset.java.design.visitors;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.pmd.visitors.Modification;
import org.walkmod.pmd.visitors.PMDRuleVisitor;

@Modification
public class ImmutableField extends PMDRuleVisitor {

    private static final boolean DEBUG = false;
    private static final Logger log = Logger.getLogger(ImmutableField.class);
    static {
        //        log.setLevel(Level.DEBUG);
    }

    @Override
    public void visit(FieldDeclaration n, Node node) {
        FieldDeclaration aux = (FieldDeclaration) node;
        int modifiers = aux.getModifiers();
        if (ModifierSet.isPrivate(modifiers)) {
            List<VariableDeclarator> vds = aux.getVariables();

            if (vds != null && vds.size() == 1) {
                VariableDeclarator vd = vds.get(0);
                List<SymbolReference> usages = vd.getUsages();

                if (usages != null) {
                    if (DEBUG && log.isDebugEnabled()) {
                        log.debug("visit(FieldDeclaration): " + pos(n) + " " + n.getSymbolName());
                    }
                    Iterator<SymbolReference> it = usages.iterator();
                    boolean isUsedFromMethodOrInner = false;
                    boolean isAssigned = false;
                    while (it.hasNext() && !isUsedFromMethodOrInner) {
                        SymbolReference next = it.next();
                        Node auxNode = (Node) next;
                        if (DEBUG && log.isDebugEnabled()) {
                            log.debug("visit(FieldDeclaration): usage "
                                    + classNameWithPos(auxNode)
                                    + " (parent: "
                                    + classNameWithPos(auxNode.getParentNode()));
                        }

                        final boolean assigned = isAssigned(auxNode);
                        isUsedFromMethodOrInner =
                                ((isUsedFromMethod(auxNode) || isUsedFromOtherConstructor(auxNode, n.getParentNode()))
                                        && assigned);
                        isAssigned = isAssigned || assigned;
                    }
                    final boolean addFinal = !isUsedFromMethodOrInner && !(isAssigned && vd.getInit() != null);
                    if (addFinal) {
                        int auxModifiers = ModifierSet.addModifier(modifiers, Modifier.FINAL);
                        aux.setModifiers(auxModifiers);
                    }
                }
            }
        }
    }

    private String classNameWithPos(Node n) {
        return pos(n) + " " + className(n);
    }

    private String className(Node n) {
        return n != null ? n.getClass().getSimpleName() : "";
    }

    private static String pos(Node n) {
        return n != null ? n.getBeginLine() + ":" + n.getBeginColumn() : "";
    }

    private boolean isAssigned(Node node) {
        if (node == null) {
            return false;
        }
        Node parent = node.getParentNode();

        if (parent instanceof AssignExpr) {
            AssignExpr ae = (AssignExpr) parent;
            return (ae.getTarget() == node);
        }

        if (parent instanceof UnaryExpr) {
            UnaryExpr e = (UnaryExpr) parent;
            final UnaryExpr.Operator op = e.getOperator();
            return op != UnaryExpr.Operator.inverse
                    && op != UnaryExpr.Operator.negative
                    && op != UnaryExpr.Operator.not
                    && op != UnaryExpr.Operator.positive;
        }

        return false;
    }

    private boolean isUsedFromMethod(Node node) {
        return node != null && (node instanceof MethodDeclaration || isUsedFromMethod(node.getParentNode()));
    }

    private boolean isUsedFromOtherConstructor(Node node, Node fieldOwner) {
        if (DEBUG && log.isDebugEnabled()) {
            log.debug("visit(FieldDeclaration): isUsedFromOtherConstructor("
                    + classNameWithPos(node)
                    + ", "
                    + classNameWithPos(fieldOwner));
        }

        boolean res = false;
        if (node != null) {
            if (node instanceof ConstructorDeclaration) {
                if (DEBUG && log.isDebugEnabled()) {
                    log.debug("visit(FieldDeclaration): isUsedFromOtherConstructor, parent="
                            + classNameWithPos(node.getParentNode()));
                }
                res = node.getParentNode() != fieldOwner;
            } else if (!(node instanceof BodyDeclaration)) {
                res = isUsedFromOtherConstructor(node.getParentNode(), fieldOwner);
            }
        }
        if (DEBUG && log.isDebugEnabled()) {
            log.debug("visit(FieldDeclaration): isUsedFromOtherConstructor"
                    + "\n    node      : "
                    + classNameWithPos(node)
                    + "\n    fieldOwner: "
                    + classNameWithPos(fieldOwner)
                    + "\n    res: "
                    + res);
        }
        return res;
    }
}
