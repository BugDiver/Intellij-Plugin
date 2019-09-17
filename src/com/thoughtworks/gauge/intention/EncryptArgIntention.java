package com.thoughtworks.gauge.intention;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class EncryptArgIntention extends EncryptDecryptArgIntentionBase {
    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Encrypt Parameter";
    }

    @NotNull
    @Override
    protected String getReplacementString(String paramText, Project project) {
        try {
            paramText = this.runCommandAndGetOutput("encrypt", paramText);
        } catch (Exception e) {
            Notification notification = new Notification("Error", "Failed to encrypt param", "", NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return paramText;
        }
        return "\"" + paramText + "\"";
    }


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!super.isAvailable(project, editor, element)) return false;
        String text = this.getArg(element).getText();
        String paramText = StringUtils.substring(text, 1, text.length() - 1);
        return !paramText.startsWith("gcrypt:");
    }

}


