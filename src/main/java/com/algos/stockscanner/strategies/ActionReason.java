package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Reasons;

public class ActionReason {
    Actions action;
    Reasons reason;

    public ActionReason(Actions action, Reasons reason) {
        this.action = action;
        this.reason = reason;
    }

    public Actions getAction() {
        return action;
    }

    public void setAction(Actions action) {
        this.action = action;
    }

    public Reasons getReason() {
        return reason;
    }

    public void setReason(Reasons reason) {
        this.reason = reason;
    }
}
