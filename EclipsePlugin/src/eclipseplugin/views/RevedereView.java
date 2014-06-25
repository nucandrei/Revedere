package eclipseplugin.views;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;

import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.views.composites.NoSessionComposite;
import eclipseplugin.views.composites.UsersComposite;

public class RevedereView extends ViewPart implements ViewStack {
    public static final String ID = "eclipseplugin.views.RevedereView";
    private RevedereManager revedereManager = RevedereManager.getInstance();
    private final StackLayout stackLayout = new StackLayout();
    private Composite noSessionComposite;
    private UsersComposite usersComposite;
    private Composite messageComposite;
    private Composite reviewComposite;

    private Composite parent;

    public void createPartControl(Composite parent) {
        this.parent = parent;
        this.parent.setLayout(stackLayout);
        noSessionComposite = new NoSessionComposite(parent);
        usersComposite = new UsersComposite(parent, this);
        revedereManager.setViewStack(this);
        stackLayout.topControl = noSessionComposite;
        this.parent.layout();
        this.parent.pack();
    }

    @Override
    public void setFocus() {

    }

    @Override
    public void changeToNoConnectionOrSession() {
        stackLayout.topControl = noSessionComposite;
        this.parent.layout();
        this.parent.pack();
    }

    @Override
    public void changeToUsersView() {
        usersComposite.setSession(revedereManager.getCurrentSession());
        stackLayout.topControl = usersComposite;
        this.parent.layout();
        this.parent.pack();
    }

}