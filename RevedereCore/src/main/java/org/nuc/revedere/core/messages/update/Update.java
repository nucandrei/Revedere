package org.nuc.revedere.core.messages.update;

import java.io.Serializable;

public class Update implements Serializable{
    private static final long serialVersionUID = 2107945718083986566L;
    private final boolean isDeltaUpdate;
    public Update(boolean isDeltaUpdate) {
        this.isDeltaUpdate = isDeltaUpdate;
    }
    
    public boolean isDeltaUpdate() {
        return this.isDeltaUpdate;
    }
}
