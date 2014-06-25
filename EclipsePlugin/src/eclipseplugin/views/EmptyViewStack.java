package eclipseplugin.views;

public class EmptyViewStack implements ViewStack {
    private static final EmptyViewStack instance = new EmptyViewStack();

    public static EmptyViewStack getInstance() {
        return instance;
    }

    @Override
    public void changeToNoConnectionOrSession() {
        // Do nothing
    }

    @Override
    public void changeToUsersView() {
        // Do nothing
    }
}
