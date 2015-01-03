package com.thoughtworks.gauge.rename;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenameHandler;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CustomRenameHandler implements RenameHandler {

    public boolean isAvailableOnDataContext(DataContext dataContext) {
        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        return CommonDataKeys.PROJECT.getData(dataContext) != null && element != null && (element.toString().equals("PsiAnnotation") || element.getClass().equals(ConceptStepImpl.class));
    }

    public boolean isRenaming(DataContext dataContext) {
        return isAvailableOnDataContext(dataContext);
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (element == null) return;
        String text = element.toString();
        if (element.toString().equals("PsiAnnotation"))
            text = element.getChildren()[2].getChildren()[1].getText().substring(1, element.getChildren()[2].getChildren()[1].getText().length() - 1);
        Messages.showInputDialog(project,
                String.format("Refactoring \"%s\" to : ", text),
                "Refactor",
                Messages.getInformationIcon(),
                text,
                new MyInputValidator(project,text));
    }

    public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
        invoke(project, null, null, dataContext);
    }

    private static class MyInputValidator implements InputValidator {
        private final Project project;
        private String text;

        public MyInputValidator(final Project project, String text) {
            this.project = project;
            this.text = text;
        }

        public boolean checkInput(String inputString) {
            return true;
        }

        public boolean canClose(final String inputString) {
            return doRename(inputString);
        }

        private boolean doRename(final String inputString) {
            String[] commands = new String[4];
            commands[0] = "gauge";
            commands[1] = "--refactor";
            commands[2] = text;
            commands[3] = inputString;
            try {
                Process exec = Runtime.getRuntime().exec(commands, null, new File(project.getBaseDir().getPath()));
                exec.waitFor();
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }
}
