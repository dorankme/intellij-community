
package com.intellij.find.findUsages;

import com.intellij.find.FindSettings;
import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 */
public class FindUsagesOptions extends UserDataHolderBase implements Cloneable {
  public SearchScope searchScope;

  public boolean isSearchForTextOccurences = true;

  public boolean isUsages = false;
  public boolean isClassesUsages = false;
  public boolean isMethodsUsages = false;
  public boolean isFieldsUsages = false;
  public boolean isDerivedClasses = false;
  public boolean isImplementingClasses = false;
  public boolean isDerivedInterfaces = false;
  public boolean isOverridingMethods = false;
  public boolean isImplementingMethods = false;
  public boolean isIncludeSubpackages = true;
  public boolean isSkipImportStatements = false;
  public boolean isSkipPackageStatements = false;
  public boolean isCheckDeepInheritance = true;
  public boolean isIncludeInherited = false;
  public boolean isReadAccess = false;
  public boolean isWriteAccess = false;
  public boolean isIncludeOverloadUsages = false;
  public boolean isThrowUsages = false;

  public FindUsagesOptions(@NotNull Project project) {
    String defaultScopeName = FindSettings.getInstance().getDefaultScopeName();
    List<SearchScope> predefined = ScopeChooserCombo.getPredefinedScopes(project, true, false, false, false);
    for (SearchScope scope : predefined) {
      if (scope.getDisplayName().equals(defaultScopeName)) {
        searchScope = scope;
        break;
      }
    }
    if (searchScope == null) {
      searchScope = ProjectScope.getProjectScope(project);
    }
  }

  public Object clone() {
    return super.clone();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FindUsagesOptions that = (FindUsagesOptions)o;

    if (isCheckDeepInheritance != that.isCheckDeepInheritance) return false;
    if (isClassesUsages != that.isClassesUsages) return false;
    if (isDerivedClasses != that.isDerivedClasses) return false;
    if (isDerivedInterfaces != that.isDerivedInterfaces) return false;
    if (isFieldsUsages != that.isFieldsUsages) return false;
    if (isImplementingClasses != that.isImplementingClasses) return false;
    if (isImplementingMethods != that.isImplementingMethods) return false;
    if (isIncludeInherited != that.isIncludeInherited) return false;
    if (isIncludeOverloadUsages != that.isIncludeOverloadUsages) return false;
    if (isIncludeSubpackages != that.isIncludeSubpackages) return false;
    if (isMethodsUsages != that.isMethodsUsages) return false;
    if (isOverridingMethods != that.isOverridingMethods) return false;
    if (isReadAccess != that.isReadAccess) return false;
    if (isSearchForTextOccurences != that.isSearchForTextOccurences) return false;
    if (isSkipImportStatements != that.isSkipImportStatements) return false;
    if (isSkipPackageStatements != that.isSkipPackageStatements) return false;
    if (isThrowUsages != that.isThrowUsages) return false;
    if (isUsages != that.isUsages) return false;
    if (isWriteAccess != that.isWriteAccess) return false;
    if (searchScope != null ? !searchScope.equals(that.searchScope) : that.searchScope != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (searchScope != null ? searchScope.hashCode() : 0);
    result = 31 * result + (isSearchForTextOccurences ? 1 : 0);
    result = 31 * result + (isUsages ? 1 : 0);
    result = 31 * result + (isClassesUsages ? 1 : 0);
    result = 31 * result + (isMethodsUsages ? 1 : 0);
    result = 31 * result + (isFieldsUsages ? 1 : 0);
    result = 31 * result + (isDerivedClasses ? 1 : 0);
    result = 31 * result + (isImplementingClasses ? 1 : 0);
    result = 31 * result + (isDerivedInterfaces ? 1 : 0);
    result = 31 * result + (isOverridingMethods ? 1 : 0);
    result = 31 * result + (isImplementingMethods ? 1 : 0);
    result = 31 * result + (isIncludeSubpackages ? 1 : 0);
    result = 31 * result + (isSkipImportStatements ? 1 : 0);
    result = 31 * result + (isSkipPackageStatements ? 1 : 0);
    result = 31 * result + (isCheckDeepInheritance ? 1 : 0);
    result = 31 * result + (isIncludeInherited ? 1 : 0);
    result = 31 * result + (isReadAccess ? 1 : 0);
    result = 31 * result + (isWriteAccess ? 1 : 0);
    result = 31 * result + (isIncludeOverloadUsages ? 1 : 0);
    result = 31 * result + (isThrowUsages ? 1 : 0);
    return result;
  }
}
