package eclipseplugin.views;

import org.nuc.revedere.core.User;

public interface ViewStack {
    public void changeToNoConnectionOrSession();

    public void changeToUsersView();

    public void changeToMessageView(User user);

    public void layout();
}
