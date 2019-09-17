
package com.thoughtworks.gauge.intention;

import com.google.gson.GsonBuilder;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;
import com.thoughtworks.gauge.execution.runner.event.ExecutionResult;
import com.thoughtworks.gauge.execution.runner.event.GaugeNotification;
import com.thoughtworks.gauge.language.psi.ConceptArg;
import com.thoughtworks.gauge.language.psi.ConceptStaticArg;
import com.thoughtworks.gauge.language.psi.SpecArg;
import com.thoughtworks.gauge.language.psi.SpecStaticArg;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

abstract class EncryptDecryptArgIntentionBase extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement arg = PsiTreeUtil.getParentOfType(element, SpecArg.class);
        if (arg == null)
            arg = PsiTreeUtil.getParentOfType(element, ConceptArg.class);
        if (arg == null)
            return;
        String text = arg.getText();
        String paramText = StringUtils.substring(text, 1, text.length() - 1);
        String newText = getReplacementString(paramText, project);
        int startOffset = arg.getTextOffset();
        int endOffset = startOffset + arg.getTextLength();
        editor.getDocument().replaceString(startOffset, endOffset, newText);
    }

    @NotNull
    abstract String getReplacementString(String paramText, Project project);

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!element.isWritable() || (PsiTreeUtil.getParentOfType(element, SpecStaticArg.class) == null && PsiTreeUtil.getParentOfType(element, ConceptStaticArg.class) == null))
            return false;
        return getArg(element) != null;
    }


    PsiElement getArg(PsiElement element) {
        PsiElement arg = PsiTreeUtil.getParentOfType(element, SpecArg.class);
        if (arg == null)
            arg = PsiTreeUtil.getParentOfType(element, ConceptArg.class);
        return arg;
    }

    String runCommandAndGetOutput(String subCommand, String paramText) throws GaugeNotFoundException, IOException {
        GaugeSettingsModel settings = GaugeUtil.getGaugeSettings();
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(settings.gaugePath, subCommand, "-t", paramText, "-m");
        String response = IOUtils.toString(builder.start().getInputStream()).trim();
        ExecutionEvent r = new GsonBuilder().create().fromJson(response, ExecutionEvent.class);
        return r.message;
    }
}


