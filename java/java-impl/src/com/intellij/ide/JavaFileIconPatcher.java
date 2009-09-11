package com.intellij.ide;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;

import javax.swing.*;

/**
 * @author yole
 */
public class JavaFileIconPatcher implements FileIconPatcher {
  public Icon patchIcon(final Icon baseIcon, final VirtualFile file, final int flags, final Project project) {
    if (project == null) {
      return baseIcon;
    }

    Icon icon = replaceIcon(file, flags, project, baseIcon);

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    if (fileIndex.isInSource(file) && CompilerManager.getInstance(project).isExcludedFromCompilation(file)) {
      return new LayeredIcon(icon, Icons.EXCLUDED_FROM_COMPILE_ICON);
    }

    return icon;
  }

  private static Icon replaceIcon(VirtualFile file, int flags, Project project, Icon baseIcon) {
    FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(file);
    if (fileType == StdFileTypes.JAVA && !FileIndexUtil.isJavaSourceFile(project, file)) {
      return Icons.JAVA_OUTSIDE_SOURCE_ICON;
    }

    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (psiFile instanceof PsiClassOwner) {
      PsiClass[] classes = ((PsiClassOwner)psiFile).getClasses();
      if (classes.length > 0) {
        // prefer icon of the class named after file
        final String fileName = file.getNameWithoutExtension();
        for (PsiClass aClass : classes) {
          if (Comparing.strEqual(aClass.getName(), fileName)) {
            return aClass.getIcon(flags);
          }
        }
        final PsiClass lastClass = classes[classes.length - 1];
        if (!(lastClass instanceof SyntheticElement)) {
          return lastClass.getIcon(flags);
        }
      }
    }
    return baseIcon;
  }
}
