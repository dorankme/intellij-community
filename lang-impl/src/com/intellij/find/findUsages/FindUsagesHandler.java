/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.find.findUsages;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadActionProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.util.TextOccurrencesUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author peter
 */
public abstract class FindUsagesHandler {
  // return this handler if you want to cancel the search
  public static final FindUsagesHandler NULL_HANDLER = new FindUsagesHandler(PsiUtilBase.NULL_PSI_ELEMENT){};

  private final PsiElement myPsiElement;

  protected FindUsagesHandler(@NotNull PsiElement psiElement) {
    myPsiElement = psiElement;
  }

  @NotNull
  public AbstractFindUsagesDialog getFindUsagesDialog(boolean isSingleFile, boolean toShowInNewTab, boolean mustOpenInNewTab) {
    return new CommonFindUsagesDialog(myPsiElement, getProject(), getFindUsagesOptions(), toShowInNewTab, mustOpenInNewTab, isSingleFile, this);
  }

  public final PsiElement getPsiElement() {
    return myPsiElement;
  }

  protected final Project getProject() {
    return myPsiElement.getProject();
  }

  @NotNull
  public PsiElement[] getPrimaryElements() {
    return new PsiElement[]{myPsiElement};
  }

  @NotNull
  public PsiElement[] getSecondaryElements() {
    return PsiElement.EMPTY_ARRAY;
  }

  public static FindUsagesOptions createFindUsagesOptions(final Project project) {
    FindUsagesOptions findUsagesOptions = new FindUsagesOptions(project);
    findUsagesOptions.isUsages = true;
    findUsagesOptions.isIncludeOverloadUsages = false;
    findUsagesOptions.isIncludeSubpackages = true;
    findUsagesOptions.isReadAccess = true;
    findUsagesOptions.isWriteAccess = true;
    findUsagesOptions.isCheckDeepInheritance = true;
    findUsagesOptions.isSearchForTextOccurences = true;
    return findUsagesOptions;
  }

  @NotNull
  public FindUsagesOptions getFindUsagesOptions() {
    FindUsagesOptions options = createFindUsagesOptions(getProject());
    options.isSearchForTextOccurences &= isSearchForTextOccurencesAvailable(getPsiElement(), false);
    return options;
  }

  public void processElementUsages(@NotNull final PsiElement element, @NotNull final Processor<UsageInfo> processor, @NotNull FindUsagesOptions options) {
    if (options.isUsages) {
      ReferencesSearch.search(element, options.searchScope, false).forEach(new ReadActionProcessor<PsiReference>() {
        public boolean processInReadAction(final PsiReference ref) {
          TextRange rangeInElement = ref.getRangeInElement();
          return processor.process(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(), false));
        }
      });
    }

    if (options.isSearchForTextOccurences && options.searchScope instanceof GlobalSearchScope) {
      processUsages(element, processor, options);
    }
  }

  public void processUsages(@NotNull final PsiElement element, @NotNull Processor<UsageInfo> processor, @NotNull FindUsagesOptions options) {
    String stringToSearch = getStringToSearch(element);
    if (stringToSearch != null) {
      final TextRange elementTextRange = ApplicationManager.getApplication().runReadAction(new Computable<TextRange>() {
        public TextRange compute() {
          if (!element.isValid()) return null;
          return element.getTextRange();
        }
      });
      TextOccurrencesUtil.UsageInfoFactory factory = new TextOccurrencesUtil.UsageInfoFactory() {
        public UsageInfo createUsageInfo(@NotNull PsiElement usage, int startOffset, int endOffset) {
          if (elementTextRange != null
              && usage.getContainingFile() == element.getContainingFile()
              && elementTextRange.contains(startOffset)
              && elementTextRange.contains(endOffset)) {
            return null;
          }
          return new UsageInfo(usage, startOffset, endOffset, true);
        }
      };
      TextOccurrencesUtil.processTextOccurences(element, stringToSearch, (GlobalSearchScope)options.searchScope, processor, factory);
    }
  }

  protected String getStringToSearch(final PsiElement element) {
    if (element instanceof PsiNamedElement) {
      return ((PsiNamedElement)element).getName();
    }

    return element.getText();
  }

  protected boolean isSearchForTextOccurencesAvailable(PsiElement psiElement, boolean isSingleFile) {
    return false;
  }

  public Collection<PsiReference> findReferencesToHighlight(PsiElement target, SearchScope searchScope) {
    return ReferencesSearch.search(target, searchScope, false).findAll();
  }
}
