/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.inheritance;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.QuickFixFactory;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.GuiUtils;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * User: cdr
 */
class StaticInheritanceFix extends InspectionGadgetsFix {
  private static final Logger LOG = Logger.getInstance("#com.siyeh.ig.inheritance.StaticInheritanceFix");
  private final boolean myReplaceInWholeProject;

  StaticInheritanceFix(boolean replaceInWholeProject) {
    myReplaceInWholeProject = replaceInWholeProject;
  }

  @NotNull
  public String getName() {
    String scope = myReplaceInWholeProject ? InspectionGadgetsBundle.message("the.whole.project") : InspectionGadgetsBundle.message("this.class");
    return InspectionGadgetsBundle.message("static.inheritance.replace.quickfix", scope);
  }

  public void doFix(final Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
    final PsiJavaCodeReferenceElement referenceElement = (PsiJavaCodeReferenceElement)descriptor.getPsiElement();
    final PsiClass iface = (PsiClass)referenceElement.resolve();
    assert iface != null;
    final PsiField[] allFields = iface.getAllFields();

    final PsiClass implementingClass = ClassUtils.getContainingClass(referenceElement);
    final PsiManager manager = referenceElement.getManager();
    assert implementingClass != null;
    ProgressManager.getInstance().run(new Task.Modal(project, "Replacing usages of "+iface.getName(), false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        for (final PsiField field : allFields) {
          final Query<PsiReference> search = ReferencesSearch.search(field, implementingClass.getUseScope(), false);
          for (PsiReference reference : search) {
            if (!(reference instanceof PsiReferenceExpression)) {
              continue;
            }
            final PsiReferenceExpression referenceExpression = (PsiReferenceExpression)reference;
            if (!myReplaceInWholeProject) {
              PsiClass aClass = PsiTreeUtil.getParentOfType(referenceExpression, PsiClass.class);
              boolean isInheritor = false;
              while (aClass != null) {
                isInheritor = InheritanceUtil.isInheritorOrSelf(aClass, implementingClass, true);
                if (isInheritor) break;
                aClass = PsiTreeUtil.getParentOfType(aClass, PsiClass.class);
              }
              if (!isInheritor) continue;
            }
            Runnable runnable = new Runnable() {
              public void run() {
                if (isQuickFixOnReadOnlyFile(referenceExpression)) {
                  return;
                }
                final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory();
                final PsiReferenceExpression qualified =
                  (PsiReferenceExpression)elementFactory.createExpressionFromText("xxx." + referenceExpression.getText(), referenceExpression);
                final PsiReferenceExpression newReference = (PsiReferenceExpression)referenceExpression.replace(qualified);
                final PsiReferenceExpression qualifier = (PsiReferenceExpression)newReference.getQualifierExpression();
                assert qualifier != null : DebugUtil.psiToString(newReference, false);
                final PsiClass containingClass = field.getContainingClass();
                qualifier.bindToElement(containingClass);
              }
            };
            try {
              GuiUtils.runOrInvokeAndWait(runnable);
            }
            catch (InvocationTargetException e) {
              LOG.error(e);
            }
            catch (InterruptedException e) {
              LOG.error(e);
            }
          }
        }
        Runnable runnable = new Runnable() {
          public void run() {
            PsiClassType classType = JavaPsiFacade.getInstance(project).getElementFactory().createType(iface);
            IntentionAction fix = QuickFixFactory.getInstance().createExtendsListFix(implementingClass, classType, false);
            fix.invoke(project, null, implementingClass.getContainingFile());
          }
        };
        try {
          GuiUtils.runOrInvokeAndWait(runnable);
        }
        catch (InvocationTargetException e) {
          LOG.error(e);
        }
        catch (InterruptedException e) {
          LOG.error(e);
        }
      }
    });
  }
}
